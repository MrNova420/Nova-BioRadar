package com.bioradar.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bioradar.ui.screens.AdvancedModesScreen
import com.bioradar.ui.screens.GuardScreen
import com.bioradar.ui.screens.MeshScreen
import com.bioradar.ui.screens.RadarScreen
import com.bioradar.ui.screens.SettingsScreen

/**
 * Navigation routes
 */
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Radar : Screen(
        route = "radar",
        title = "Radar",
        selectedIcon = Icons.Filled.Radar,
        unselectedIcon = Icons.Outlined.Radar
    )
    
    data object Guard : Screen(
        route = "guard",
        title = "Guard",
        selectedIcon = Icons.Filled.Security,
        unselectedIcon = Icons.Outlined.Security
    )
    
    data object Mesh : Screen(
        route = "mesh",
        title = "Mesh",
        selectedIcon = Icons.Filled.Wifi,
        unselectedIcon = Icons.Outlined.Wifi
    )
    
    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

val bottomNavItems = listOf(
    Screen.Radar,
    Screen.Guard,
    Screen.Mesh,
    Screen.Settings
)

/**
 * Main BioRadar App Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioRadarApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Radar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Radar.route) { RadarScreen() }
            composable(Screen.Guard.route) { GuardScreen() }
            composable(Screen.Mesh.route) { MeshScreen() }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    onNavigateToAdvancedModes = {
                        navController.navigate("advanced_modes")
                    }
                )
            }
            composable("advanced_modes") {
                AdvancedModesScreen(
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}
