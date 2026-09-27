package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Game
import com.example.model.OptimizationPreset
import com.example.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Options for sorting the game library list.
 */
enum class LibrarySortOption(val title: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    RECENTLY_PLAYED("Recently Played"),
    MOST_PLAYED("Most Played"),
    SIZE_DESC("Largest Size"),
    INSTALL_TIME("Recently Installed")
}

/**
 * Options for filtering games and apps in the library.
 */
enum class LibraryFilterOption(val title: String) {
    ALL("All Apps"),
    GAMES_ONLY("Games Only"),
    FAVORITES_ONLY("Favorites Only")
}

/**
 * ViewModel responsible for managing and exposing game library state to the UI.
 * Leverages [GameRepository] to combine device package information with local Room metadata
 * and provides sorting, filtering, searching, and game profile management.
 */
class GameLibraryViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(LibrarySortOption.NAME_ASC)
    val sortOption: StateFlow<LibrarySortOption> = _sortOption.asStateFlow()

    private val _filterOption = MutableStateFlow(LibraryFilterOption.ALL)
    val filterOption: StateFlow<LibraryFilterOption> = _filterOption.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * Continuous flow of games merged from PackageManager and Room database metadata.
     */
    private val baseGamesFlow = gameRepository.getGamesFlow()

    /**
     * Primary StateFlow of [Game] items exposed for the UI, dynamically
     * filtered, searched, and sorted.
     */
    val games: StateFlow<List<Game>> = combine(
        baseGamesFlow,
        _searchQuery,
        _sortOption,
        _filterOption
    ) { allGames, query, sort, filter ->
        var result = allGames

        // 1. Filter by category / favorites
        result = when (filter) {
            LibraryFilterOption.ALL -> result
            LibraryFilterOption.GAMES_ONLY -> result.filter { it.isGame }
            LibraryFilterOption.FAVORITES_ONLY -> result.filter { it.isFavorite }
        }

        // 2. Filter by search query across label and package
        if (query.isNotBlank()) {
            val normalizedQuery = query.trim().lowercase()
            result = result.filter { game ->
                game.appName.lowercase().contains(normalizedQuery) ||
                game.packageName.lowercase().contains(normalizedQuery)
            }
        }

        // 3. Sort according to selected sort criteria
        when (sort) {
            LibrarySortOption.NAME_ASC -> result.sortedBy { it.appName.lowercase() }
            LibrarySortOption.NAME_DESC -> result.sortedByDescending { it.appName.lowercase() }
            LibrarySortOption.RECENTLY_PLAYED -> result.sortedByDescending { it.lastPlayedTimestamp }
            LibrarySortOption.MOST_PLAYED -> result.sortedByDescending { it.launchCount }
            LibrarySortOption.SIZE_DESC -> result.sortedByDescending { it.apkSizeBytes }
            LibrarySortOption.INSTALL_TIME -> result.sortedByDescending { it.installTime }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    /**
     * StateFlow exposing only favorited games.
     */
    val favoriteGames: StateFlow<List<Game>> = gameRepository.getFavoriteGamesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * StateFlow exposing recently played games.
     */
    val recentGames: StateFlow<List<Game>> = gameRepository.getRecentGamesFlow(limit = 10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        refreshGames()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: LibrarySortOption) {
        _sortOption.value = sort
    }

    fun setFilterOption(filter: LibraryFilterOption) {
        _filterOption.value = filter
    }

    /**
     * Triggers a scan and sync across device packages and the Room database.
     */
    fun refreshGames() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            try {
                gameRepository.scanInstalledApps(autoCleanUninstalled = true)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Toggles the favorite status of a game in Room database.
     */
    fun toggleFavorite(game: Game) {
        viewModelScope.launch(Dispatchers.IO) {
            gameRepository.toggleFavorite(game.packageName, game.isFavorite)
        }
    }

    /**
     * Updates optimization preset assigned to a game in Room database.
     */
    fun updatePreset(game: Game, preset: OptimizationPreset) {
        viewModelScope.launch(Dispatchers.IO) {
            gameRepository.updateGamePreset(game.packageName, preset)
        }
    }

    /**
     * Records game launch timestamp and increments launch counter.
     */
    fun recordLaunch(game: Game) {
        viewModelScope.launch(Dispatchers.IO) {
            gameRepository.recordGameLaunch(game.packageName)
        }
    }
}
