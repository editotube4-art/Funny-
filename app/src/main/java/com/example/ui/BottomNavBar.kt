package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FunnyDoseViewModel

@Composable
fun BottomNavBar(viewModel: FunnyDoseViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Do not show navigation bar if logged out
    if (currentUser == null) return

    val barBg = Color(0xFF07000B)

    NavigationBar(
        containerColor = barBg,
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(68.dp)
            .testTag("global_bottom_nav_bar")
    ) {
        // --- Tab 1: Home Feed ---
        NavigationBarItem(
            selected = currentScreen == AppScreen.FEED,
            onClick = { viewModel.navigateTo(AppScreen.FEED) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.FEED) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Feed"
                )
            },
            label = { Text("Feed", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFE2C55), // NeonPink
                selectedTextColor = Color(0xFFFE2C55),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.White.copy(alpha = 0.08f)
            )
        )

        // --- Tab 2: Create ---
        NavigationBarItem(
            selected = currentScreen == AppScreen.CREATE,
            onClick = { viewModel.navigateTo(AppScreen.CREATE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.CREATE) Icons.Default.AddBox else Icons.Outlined.AddBox,
                    contentDescription = "Create",
                    tint = Color(0xFF25F4EE) // NeonCyan punch
                )
            },
            label = { Text("Create", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF25F4EE),
                selectedTextColor = Color(0xFF25F4EE),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.White.copy(alpha = 0.08f)
            )
        )

        // --- Tab 3: Alerts/Notifications ---
        NavigationBarItem(
            selected = currentScreen == AppScreen.NOTIFICATIONS,
            onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.NOTIFICATIONS) Icons.Default.Notifications else Icons.Outlined.Notifications,
                    contentDescription = "Alerts"
                )
            },
            label = { Text("Alerts", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFE2C55),
                selectedTextColor = Color(0xFFFE2C55),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.White.copy(alpha = 0.08f)
            )
        )

        // --- Tab 4: Profile ---
        NavigationBarItem(
            selected = currentScreen == AppScreen.PROFILE,
            onClick = { viewModel.navigateTo(AppScreen.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.PROFILE) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFE2C55),
                selectedTextColor = Color(0xFFFE2C55),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.White.copy(alpha = 0.08f)
            )
        )

        // --- Tab 5: Admin Panel ---
        NavigationBarItem(
            selected = currentScreen == AppScreen.ADMIN,
            onClick = { viewModel.navigateTo(AppScreen.ADMIN) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == AppScreen.ADMIN) Icons.Default.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                    contentDescription = "Admin"
                )
            },
            label = { Text("Admin", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFFCC00), //的金
                selectedTextColor = Color(0xFFFFCC00),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = Color.White.copy(alpha = 0.08f)
            )
        )
    }
}
