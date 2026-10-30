package de.exit.ridetrackr.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.exit.ridetrackr.ui.components.M3FloatingToolbar
import de.exit.ridetrackr.ui.screens.attractions.AttractionsScreen
import de.exit.ridetrackr.ui.screens.attractions.AttractionsViewModel
import de.exit.ridetrackr.ui.screens.home.HomeScreen
import de.exit.ridetrackr.ui.screens.home.HomeViewModel
import de.exit.ridetrackr.ui.screens.search.SearchScreen
import de.exit.ridetrackr.ui.screens.search.SearchViewModel
import de.exit.ridetrackr.ui.screens.settings.SettingsScreen
import de.exit.ridetrackr.ui.screens.settings.SettingsViewModel

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = currentRoute?.contains("Attractions") != true

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 220)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 220)) },
                popEnterTransition = { fadeIn(animationSpec = tween(durationMillis = 220)) },
                popExitTransition = { fadeOut(animationSpec = tween(durationMillis = 220)) }
            ) {
            composable<Screen.Home> {
                val viewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPark = { parkId, parkName ->
                        navController.navigate(Screen.Attractions(parkId = parkId, parkName = parkName))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable<Screen.Search> {
                val viewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateToPark = { parkId, parkName ->
                        navController.navigate(Screen.Attractions(parkId = parkId, parkName = parkName))
                    },
                    onNavigateBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.Home) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable<Screen.Settings> {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = viewModel)
            }

            composable<Screen.Attractions> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.Attractions>()
                val viewModel: AttractionsViewModel = hiltViewModel()
                AttractionsScreen(
                    parkId = args.parkId,
                    parkName = args.parkName,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Official M3 Expressive Floating Toolbar with standalone Search Button
        if (isTopLevelDestination) {
            M3FloatingToolbar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        }
    }
}
