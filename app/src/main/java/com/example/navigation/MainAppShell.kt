package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.presentation.screens.DashboardScreen
import com.example.presentation.screens.PerformanceScreen
import com.example.presentation.screens.PermissionScreen
import com.example.presentation.screens.ProcessesScreen
import com.example.presentation.screens.SettingsScreen
import com.example.presentation.viewmodel.SystemPulseViewModel

@Composable
fun MainAppShell(
    viewModel: SystemPulseViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val isGranted by viewModel.permissionsGranted.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val accentIndex by viewModel.accentColorIndex.collectAsState()
    val accentColors = listOf(
        Color(0xFF00E5FF), // Cyan
        Color(0xFF00E676), // Green
        Color(0xFF2979FF), // Blue
        Color(0xFFFF9100), // Orange
        Color(0xFFFF1744)  // Red
    )
    val themeAccent = accentColors.getOrElse(accentIndex) { Color(0xFF00E5FF) }

    // Start screen logic: if permissions granted, jump straight to Dashboard; otherwise onboarding
    val startDestination = if (isGranted) Screen.Dashboard.route else Screen.Onboarding.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Only draw bottom navigation if we are NOT on the onboarding screen
            if (currentRoute != Screen.Onboarding.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val items = listOf(Screen.Dashboard, Screen.Performance, Screen.Processes, Screen.Settings)
                    items.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = themeAccent,
                                selectedTextColor = themeAccent,
                                indicatorColor = themeAccent.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                PermissionScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = viewModel)
            }

            composable(Screen.Performance.route) {
                PerformanceScreen(viewModel = viewModel)
            }

            composable(Screen.Processes.route) {
                ProcessesScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
