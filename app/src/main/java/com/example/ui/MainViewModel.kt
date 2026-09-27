package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ZXApp
import com.example.model.AppSettings
import com.example.model.DeviceHardwareInfo
import com.example.model.GameItem
import com.example.model.GameSession
import com.example.model.OptimizationPreset
import com.example.model.OptimizationResult
import com.example.model.ThemeMode
import com.example.repository.DeviceMonitorRepository
import com.example.repository.GameRepository
import com.example.repository.OptimizerRepository
import com.example.utils.AppLauncher
import com.example.utils.HapticFeedbackHelper
import com.example.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GamingLauncherState(val label: String, val colorHex: Long) {
    READY("READY", 0xFF00F0FF),
    BOOSTING("BOOSTING", 0xFFA855F7),
    PLAYING("PLAYING", 0xFF00FF88),
    SESSION_ENDED("SESSION ENDED", 0xFFFF7700)
}

enum class SortOption(val title: String) {
    NAME("Name (A-Z)"),
    RECENT("Recently Played"),
    MOST_PLAYED("Most Played"),
    SIZE("Storage Size")
}

enum class FilterOption(val title: String) {
    ALL("All Apps"),
    GAMES_ONLY("Games Only"),
    FAVORITES("Favorites")
}

enum class NavigationTab(val title: String) {
    HOME("HOME"),
    GAMES("GAMES"),
    OPTIMIZER("BOOSTER"),
    MONITOR("MONITOR"),
    SETTINGS("SETTINGS")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as ZXApp).database
    private val gameRepository = GameRepository(application, db.gameDao())
    private val deviceMonitorRepository = DeviceMonitorRepository(application)
    private val optimizerRepository = OptimizerRepository(application)

    // Navigation State
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // Real Gaming State Indicator
    private val _launcherState = MutableStateFlow(GamingLauncherState.READY)
    val launcherState: StateFlow<GamingLauncherState> = _launcherState.asStateFlow()

    // Settings State
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedSort = MutableStateFlow(SortOption.NAME)
    val selectedFilter = MutableStateFlow(FilterOption.ALL)

    // Raw Games from System and Database
    private val _rawGames = MutableStateFlow<List<GameItem>>(emptyList())

    // Filtered & Sorted Games for Library View
    val filteredGames: StateFlow<List<GameItem>> = combine(
        _rawGames,
        searchQuery,
        selectedSort,
        selectedFilter
    ) { games, query, sort, filter ->
        var list = games

        // 1. Filter by category / favorites
        list = when (filter) {
            FilterOption.ALL -> list
            FilterOption.GAMES_ONLY -> list.filter { it.isGame }
            FilterOption.FAVORITES -> list.filter { it.isFavorite }
        }

        // 2. Filter by search query (name or package)
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }

        // 3. Sort
        when (sort) {
            SortOption.NAME -> list.sortedBy { it.appName.lowercase() }
            SortOption.RECENT -> list.sortedByDescending { it.lastPlayedTimestamp }
            SortOption.MOST_PLAYED -> list.sortedByDescending { it.launchCount }
            SortOption.SIZE -> list.sortedByDescending { it.apkSizeBytes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Favorite Games
    val favoriteGames: StateFlow<List<GameItem>> = _rawGames.combine(searchQuery) { games, _ ->
        games.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Recent Games
    val recentGames: StateFlow<List<GameItem>> = _rawGames.combine(searchQuery) { games, _ ->
        games.filter { it.launchCount > 0 }.sortedByDescending { it.lastPlayedTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Device Hardware Monitor Real-Time Flow
    val hardwareInfo: StateFlow<DeviceHardwareInfo> = deviceMonitorRepository
        .getDeviceHardwareInfoFlow(pollIntervalMs = 2000L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), deviceMonitorRepository.readCurrentHardwareMetrics())

    // Optimization State
    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    private val _lastOptimizationResult = MutableStateFlow<OptimizationResult?>(null)
    val lastOptimizationResult: StateFlow<OptimizationResult?> = _lastOptimizationResult.asStateFlow()

    private val _selectedPreset = MutableStateFlow(OptimizationPreset.BALANCED)
    val selectedPreset: StateFlow<OptimizationPreset> = _selectedPreset.asStateFlow()

    // Game Sessions History
    val gameSessions: StateFlow<List<GameSession>> = gameRepository.getAllSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Game Detail Dialog / Sheet Selection
    private val _selectedGameForDetail = MutableStateFlow<GameItem?>(null)
    val selectedGameForDetail: StateFlow<GameItem?> = _selectedGameForDetail.asStateFlow()

    // Ongoing Active Game Session
    private var activeSessionStartTime: Long = 0L
    private var activeSessionGame: GameItem? = null
    private var sessionTimerJob: Job? = null

    private val _activeSessionElapsedSeconds = MutableStateFlow(0L)
    val activeSessionElapsedSeconds: StateFlow<Long> = _activeSessionElapsedSeconds.asStateFlow()

    // Currently Highlighted / Selected Game for Gaming HUD
    private val _currentSelectedGame = MutableStateFlow<GameItem?>(null)
    val currentSelectedGame: StateFlow<GameItem?> = _currentSelectedGame.asStateFlow()

    // Status Message for Snackbars / HUD Notifications
    private val _hudMessage = MutableStateFlow<String?>(null)
    val hudMessage: StateFlow<String?> = _hudMessage.asStateFlow()

    init {
        scanGames()
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
        HapticFeedbackHelper.performClickHaptic(getApplication(), _settings.value.isHapticEnabled)
    }

    fun scanGames() {
        viewModelScope.launch {
            val list = gameRepository.scanInstalledApps(autoCleanUninstalled = true)
            _rawGames.value = list

            // Update current highlighted game if not set
            if (_currentSelectedGame.value == null && list.isNotEmpty()) {
                val ffGame = list.find { it.packageName == "com.dts.freefireth" || it.packageName == "com.dts.freefiremax" }
                _currentSelectedGame.value = ffGame ?: list.find { it.isFavorite } ?: list.firstOrNull()
            }
        }
    }

    fun setCurrentSelectedGame(game: GameItem) {
        _currentSelectedGame.value = game
        HapticFeedbackHelper.performClickHaptic(getApplication(), _settings.value.isHapticEnabled)
    }

    fun toggleFavorite(game: GameItem) {
        viewModelScope.launch {
            gameRepository.toggleFavorite(game.packageName, game.isFavorite)
            HapticFeedbackHelper.performClickHaptic(getApplication(), _settings.value.isHapticEnabled)
            // Update local state directly for immediate UI feedback
            _rawGames.update { list ->
                list.map {
                    if (it.packageName == game.packageName) it.copy(isFavorite = !game.isFavorite) else it
                }
            }
            if (_selectedGameForDetail.value?.packageName == game.packageName) {
                _selectedGameForDetail.value = _selectedGameForDetail.value?.copy(isFavorite = !game.isFavorite)
            }
            if (_currentSelectedGame.value?.packageName == game.packageName) {
                _currentSelectedGame.value = _currentSelectedGame.value?.copy(isFavorite = !game.isFavorite)
            }
        }
    }

    fun updateGamePreset(game: GameItem, preset: OptimizationPreset) {
        viewModelScope.launch {
            gameRepository.updateGamePreset(game.packageName, preset)
            _rawGames.update { list ->
                list.map {
                    if (it.packageName == game.packageName) it.copy(preset = preset) else it
                }
            }
            if (_selectedGameForDetail.value?.packageName == game.packageName) {
                _selectedGameForDetail.value = _selectedGameForDetail.value?.copy(preset = preset)
            }
            if (_currentSelectedGame.value?.packageName == game.packageName) {
                _currentSelectedGame.value = _currentSelectedGame.value?.copy(preset = preset)
            }
        }
    }

    fun setSelectedPreset(preset: OptimizationPreset) {
        _selectedPreset.value = preset
        HapticFeedbackHelper.performClickHaptic(getApplication(), _settings.value.isHapticEnabled)
    }

    fun openGameDetail(game: GameItem) {
        _selectedGameForDetail.value = game
        HapticFeedbackHelper.performClickHaptic(getApplication(), _settings.value.isHapticEnabled)
    }

    fun closeGameDetail() {
        _selectedGameForDetail.value = null
    }

    /**
     * BOOST & PLAY:
     * 1. Release launcher memory and unused view caches
     * 2. Initialize gaming session in database
     * 3. Launch target app via official Android Intent
     * 4. Transition state to PLAYING
     */
    fun boostAndPlay(
        context: Context,
        game: GameItem,
        onCloseActivityRequested: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _launcherState.value = GamingLauncherState.BOOSTING
            _isOptimizing.value = true
            HapticFeedbackHelper.performOptimizePattern(context, _settings.value.isHapticEnabled)

            // Real launcher optimization execution
            val result = optimizerRepository.executeOptimization(
                preset = game.preset,
                customAggressiveTrim = _settings.value.isAggressiveTrimEnabled
            )
            _lastOptimizationResult.value = result
            _isOptimizing.value = false

            // Short stabilization delay for smooth UX
            delay(400L)

            // Launch target application
            launchGameInternal(
                context = context,
                game = game,
                onCloseActivityRequested = onCloseActivityRequested
            )
        }
    }

    fun launchGame(
        context: Context,
        game: GameItem,
        onCloseActivityRequested: () -> Unit = {}
    ) {
        launchGameInternal(context, game, onCloseActivityRequested)
    }

    private fun launchGameInternal(
        context: Context,
        game: GameItem,
        onCloseActivityRequested: () -> Unit
    ) {
        HapticFeedbackHelper.performHeavyHaptic(context, _settings.value.isHapticEnabled)
        val now = System.currentTimeMillis()

        // 1. Prepare session recording
        activeSessionStartTime = now
        activeSessionGame = game
        _currentSelectedGame.value = game
        _launcherState.value = GamingLauncherState.PLAYING

        // Start in-memory live timer
        sessionTimerJob?.cancel()
        _activeSessionElapsedSeconds.value = 0L
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _activeSessionElapsedSeconds.value = ((System.currentTimeMillis() - now) / 1000L).coerceAtLeast(0L)
            }
        }

        // 2. Send notification if enabled
        if (_settings.value.isNotificationsEnabled) {
            NotificationHelper.sendSessionStartNotification(context, game.appName, game.preset)
        }

        // 3. Launch App using official Android Intent
        val success = AppLauncher.launchApp(
            context = context,
            packageName = game.packageName,
            onSuccess = {
                viewModelScope.launch {
                    gameRepository.recordGameLaunch(game.packageName, now)
                    _rawGames.update { list ->
                        list.map {
                            if (it.packageName == game.packageName) {
                                it.copy(
                                    launchCount = it.launchCount + 1,
                                    lastPlayedTimestamp = now
                                )
                            } else it
                        }
                    }
                }
                if (_settings.value.isCloseLauncherOnLaunch) {
                    onCloseActivityRequested()
                }
            },
            onError = { error ->
                _hudMessage.value = error
                _launcherState.value = GamingLauncherState.READY
            }
        )

        if (!success) {
            activeSessionStartTime = 0L
            activeSessionGame = null
            sessionTimerJob?.cancel()
            _launcherState.value = GamingLauncherState.READY
        }
    }

    /**
     * Free Fire Specific One-Tap Boost & Play
     */
    fun boostAndPlayFreeFire(
        context: Context,
        onCloseActivityRequested: () -> Unit = {}
    ) {
        val ffGame = _rawGames.value.find {
            it.packageName == "com.dts.freefireth" || it.packageName == "com.dts.freefiremax"
        }
        if (ffGame != null) {
            boostAndPlay(context, ffGame, onCloseActivityRequested)
        } else {
            _hudMessage.value = "Free Fire tidak ditemukan di perangkat."
        }
    }

    fun playFreeFire(
        context: Context,
        onCloseActivityRequested: () -> Unit = {}
    ) {
        val ffGame = _rawGames.value.find {
            it.packageName == "com.dts.freefireth" || it.packageName == "com.dts.freefiremax"
        }
        if (ffGame != null) {
            launchGame(context, ffGame, onCloseActivityRequested)
        } else {
            _hudMessage.value = "Free Fire tidak ditemukan di perangkat."
        }
    }

    fun onAppResumed() {
        sessionTimerJob?.cancel()

        // Check if returning from a launched game session
        if (activeSessionStartTime > 0L && activeSessionGame != null) {
            val game = activeSessionGame!!
            val endTime = System.currentTimeMillis()
            val startTime = activeSessionStartTime
            val duration = endTime - startTime

            // Reset active markers
            activeSessionStartTime = 0L
            activeSessionGame = null
            _launcherState.value = GamingLauncherState.SESSION_ENDED

            // Record session if it lasted more than 3 seconds
            if (duration >= 3000L) {
                viewModelScope.launch {
                    gameRepository.recordSession(
                        packageName = game.packageName,
                        appName = game.appName,
                        startTime = startTime,
                        endTime = endTime,
                        preset = game.preset
                    )
                    val formatted = GameSession(
                        packageName = game.packageName,
                        appName = game.appName,
                        startTimeMillis = startTime,
                        endTimeMillis = endTime,
                        durationMillis = duration
                    ).durationFormatted

                    if (_settings.value.isNotificationsEnabled) {
                        NotificationHelper.sendSessionEndNotification(getApplication(), game.appName, formatted)
                    }
                    _hudMessage.value = "🎮 Session recorded for ${game.appName} ($formatted)"
                }
            }
        } else if (_launcherState.value == GamingLauncherState.PLAYING) {
            _launcherState.value = GamingLauncherState.READY
        }

        if (_settings.value.isAutoScanEnabled) {
            scanGames()
        }
    }

    fun resetLauncherStateToReady() {
        _launcherState.value = GamingLauncherState.READY
    }

    fun runQuickOptimization() {
        if (_isOptimizing.value) return

        viewModelScope.launch {
            _launcherState.value = GamingLauncherState.BOOSTING
            _isOptimizing.value = true
            HapticFeedbackHelper.performOptimizePattern(getApplication(), _settings.value.isHapticEnabled)

            val result = optimizerRepository.executeOptimization(
                preset = _selectedPreset.value,
                customAggressiveTrim = _settings.value.isAggressiveTrimEnabled
            )

            _lastOptimizationResult.value = result
            _isOptimizing.value = false
            _launcherState.value = GamingLauncherState.READY

            if (_settings.value.isNotificationsEnabled) {
                NotificationHelper.sendOptimizationNotification(getApplication(), result)
            }
            _hudMessage.value = result.summaryMessage
        }
    }

    // App Settings Toggles
    fun setThemeMode(mode: ThemeMode) {
        _settings.update { it.copy(themeMode = mode) }
    }

    fun toggleHaptic(enabled: Boolean) {
        _settings.update { it.copy(isHapticEnabled = enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _settings.update { it.copy(isNotificationsEnabled = enabled) }
    }

    fun toggleAutoScan(enabled: Boolean) {
        _settings.update { it.copy(isAutoScanEnabled = enabled) }
    }

    fun toggleCloseOnLaunch(enabled: Boolean) {
        _settings.update { it.copy(isCloseLauncherOnLaunch = enabled) }
    }

    fun toggleAggressiveTrim(enabled: Boolean) {
        _settings.update { it.copy(isAggressiveTrimEnabled = enabled) }
    }

    fun toggleAnimation(enabled: Boolean) {
        _settings.update { it.copy(isAnimationEnabled = enabled) }
    }

    fun clearSessionHistory() {
        viewModelScope.launch {
            gameRepository.clearAllSessions()
            _hudMessage.value = "Session history cleared"
        }
    }

    fun resetAllAppData() {
        viewModelScope.launch {
            gameRepository.resetAllData()
            _hudMessage.value = "App database reset successfully"
        }
    }

    fun clearHudMessage() {
        _hudMessage.value = null
    }
}

