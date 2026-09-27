package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.NavigationTab
import com.example.ui.MainViewModel
import com.example.ui.dialogs.GameDetailSheet
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonitorScreen
import com.example.ui.screens.OptimizerScreen
import com.example.ui.screens.SessionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = viewModel,
                    onCloseActivity = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    onCloseActivity: () -> Unit
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()
    val launcherState by viewModel.launcherState.collectAsState()
    val activeSessionSeconds by viewModel.activeSessionElapsedSeconds.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    val lastOptimizationResult by viewModel.lastOptimizationResult.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val filteredGames by viewModel.filteredGames.collectAsState()
    val favoriteGames by viewModel.favoriteGames.collectAsState()
    val recentGames by viewModel.recentGames.collectAsState()
    val gameSessions by viewModel.gameSessions.collectAsState()
    val selectedGameForDetail by viewModel.selectedGameForDetail.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val hudMessage by viewModel.hudMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(hudMessage) {
        hudMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearHudMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        containerColor = MatteBlack,
        bottomBar = {
            ZXBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 72.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContentTransition"
            ) { tab ->
                when (tab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            hardwareInfo = hardwareInfo,
                            launcherState = launcherState,
                            activeSessionSeconds = activeSessionSeconds,
                            selectedGame = selectedGameForDetail ?: favoriteGames.firstOrNull() ?: filteredGames.firstOrNull(),
                            favorites = favoriteGames,
                            recentGames = recentGames,
                            allGames = filteredGames,
                            isOptimizing = isOptimizing,
                            lastOptimizationResult = lastOptimizationResult,
                            onQuickOptimize = { viewModel.runQuickOptimization() },
                            onBoostAndPlayGame = { game ->
                                viewModel.boostAndPlay(
                                    context = context,
                                    game = game,
                                    onCloseActivityRequested = onCloseActivity
                                )
                            },
                            onLaunchGame = { game ->
                                viewModel.launchGame(
                                    context = context,
                                    game = game,
                                    onCloseActivityRequested = onCloseActivity
                                )
                            },
                            onBoostAndPlayFreeFire = {
                                viewModel.boostAndPlayFreeFire(
                                    context = context,
                                    onCloseActivityRequested = onCloseActivity
                                )
                            },
                            onPlayFreeFire = {
                                viewModel.playFreeFire(
                                    context = context,
                                    onCloseActivityRequested = onCloseActivity
                                )
                            },
                            onOpenGameDetail = { viewModel.openGameDetail(it) },
                            onNavigateToTab = { viewModel.selectTab(it) }
                        )
                    }
                    NavigationTab.GAMES -> {
                        GamesScreen(
                            games = filteredGames,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            selectedSort = selectedSort,
                            onSortChange = { viewModel.selectedSort.value = it },
                            selectedFilter = selectedFilter,
                            onFilterChange = { viewModel.selectedFilter.value = it },
                            onScanApps = { viewModel.scanGames() },
                            onLaunchGame = { game ->
                                viewModel.boostAndPlay(
                                    context = context,
                                    game = game,
                                    onCloseActivityRequested = onCloseActivity
                                )
                            },
                            onOpenDetail = { viewModel.openGameDetail(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                    NavigationTab.OPTIMIZER -> {
                        OptimizerScreen(
                            hardwareInfo = hardwareInfo,
                            isOptimizing = isOptimizing,
                            lastOptimizationResult = lastOptimizationResult,
                            selectedPreset = selectedPreset,
                            onSelectPreset = { viewModel.setSelectedPreset(it) },
                            onRunOptimize = { viewModel.runQuickOptimization() },
                            settings = settings,
                            onToggleCloseOnLaunch = { viewModel.toggleCloseOnLaunch(it) },
                            onToggleAggressiveTrim = { viewModel.toggleAggressiveTrim(it) }
                        )
                    }
                    NavigationTab.MONITOR -> {
                        MonitorScreen(
                            hardwareInfo = hardwareInfo
                        )
                    }
                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            settings = settings,
                            onToggleHaptic = { viewModel.toggleHaptic(it) },
                            onToggleNotifications = { viewModel.toggleNotifications(it) },
                            onToggleAutoScan = { viewModel.toggleAutoScan(it) },
                            onToggleCloseOnLaunch = { viewModel.toggleCloseOnLaunch(it) },
                            onToggleAggressiveTrim = { viewModel.toggleAggressiveTrim(it) },
                            onResetAllData = { viewModel.resetAllAppData() },
                            onClearSessions = { viewModel.clearSessionHistory() },
                            onNavigateToTab = { viewModel.selectTab(it) }
                        )
                    }
                }
            }

            // Game Detail Bottom Sheet Dialog
            selectedGameForDetail?.let { game ->
                GameDetailSheet(
                    game = game,
                    onDismiss = { viewModel.closeGameDetail() },
                    onLaunchGame = {
                        viewModel.boostAndPlay(
                            context = context,
                            game = game,
                            onCloseActivityRequested = onCloseActivity
                        )
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(game) },
                    onSelectPreset = { preset -> viewModel.updateGamePreset(game, preset) }
                )
            }
        }
    }
}

@Composable
fun ZXBottomNavigationBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        NavigationBar(
            containerColor = DarkSurface,
            contentColor = TextWhite,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            val tabs = listOf(
                Triple(NavigationTab.HOME, "Home", Icons.Default.Home),
                Triple(NavigationTab.GAMES, "Games", Icons.Default.SportsEsports),
                Triple(NavigationTab.OPTIMIZER, "Optimize", Icons.Default.Bolt),
                Triple(NavigationTab.MONITOR, "Monitor", Icons.Default.Memory),
                Triple(NavigationTab.SETTINGS, "Settings", Icons.Default.Settings)
            )

            tabs.forEach { (tab, label, icon) ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) NeonCyan else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) NeonCyan else TextMuted
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = DarkSurfaceElevated,
                        selectedIconColor = NeonCyan,
                        unselectedIconColor = TextMuted,
                        selectedTextColor = NeonCyan,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}
