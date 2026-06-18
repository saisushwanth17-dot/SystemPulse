package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Onboarding : Screen("onboarding", "Permissions", Icons.Outlined.Shield)
    object Processes : Screen("processes", "Processes", Icons.Outlined.BarChart)
    object Performance : Screen("performance", "Performance", Icons.Outlined.ShowChart)
    object Details : Screen("details", "Details", Icons.Outlined.List)
    object Services : Screen("services", "Services", Icons.Outlined.Settings)
}

