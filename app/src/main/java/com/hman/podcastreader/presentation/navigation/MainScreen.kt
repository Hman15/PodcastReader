package com.hman.podcastreader.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hman.podcastreader.R
import com.hman.podcastreader.presentation.audioplayer.GlobalAudioPlayerState
import com.hman.podcastreader.presentation.audioplayer.PersistentAudioPlayer

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // Define bottom nav items
    val bottomNavItems =
            listOf(
                    BottomNavItem(
                            route = Screen.Home.route,
                            label = "Home",
                            icon = R.drawable.ic_home
                    ),
                    BottomNavItem(
                            route = Screen.Audio.route,
                            label = "Audio",
                            icon = R.drawable.ic_audio
                    )
            )

    // Check if current destination should show bottom bar
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    // Observe player state OUTSIDE of Box to ensure proper recomposition
    val playerState by GlobalAudioPlayerState.playerState.collectAsStateWithLifecycle()

    // Debug logging
    androidx.compose.runtime.LaunchedEffect(playerState) {
        android.util.Log.d(
                "MainScreen",
                "Player state changed: isVisible=${playerState.isVisible}, title=${playerState.currentTitle}"
        )
    }

    Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                    icon = {
                                        Icon(
                                                painter = painterResource(item.icon),
                                                contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) },
                                    selected =
                                            currentDestination?.hierarchy?.any {
                                                it.route == item.route
                                            } == true,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // reselecting the same item
                                            launchSingleTop = true
                                            // Restore state when reselecting a previously selected
                                            // item
                                            restoreState = true
                                        }
                                    }
                            )
                        }
                    }
                }
            }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            NavGraph(
                    navController = navController,
                    startDestination = Screen.Home.route,
            )

            // Persistent Audio Player - shown across all screens
            PersistentAudioPlayer(
                    isVisible = playerState.isVisible,
                    audioUrl = playerState.currentAudioUrl,
                    title = playerState.currentTitle,
                    onCollapse = {
                        android.util.Log.d("MainScreen", "Collapse clicked")
                        GlobalAudioPlayerState.minimizePlayer()
                    },
                    onClose = {
                        android.util.Log.d("MainScreen", "Close clicked")
                        GlobalAudioPlayerState.closePlayer()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: Int)
