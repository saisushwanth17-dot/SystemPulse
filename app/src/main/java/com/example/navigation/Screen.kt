package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Onboarding : Screen("onboarding", "Permissions", Icons.Outlined.Shield)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Outlined.Dashboard)
    object Performance : Screen("performance", "Hardware", Icons.Outlined.Memory)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
}
