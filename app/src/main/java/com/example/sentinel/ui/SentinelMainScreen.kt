package com.example.sentinel.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SentinelBgDark
import com.example.ui.theme.SentinelCardDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SentinelMainScreen(
    viewModel: SentinelViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = SentinelBgDark,
        bottomBar = {
            if (uiState.selectedApp == null) {
                NavigationBar(
                    containerColor = SentinelCardDark,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = { Icon(imageVector = Icons.Default.Shield, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp, fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF003840),
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setTab(1) },
                        icon = { Icon(imageVector = Icons.Default.Apps, contentDescription = "Apps") },
                        label = { Text("Apps", fontSize = 11.sp, fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF003840),
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_apps")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 2,
                        onClick = { viewModel.setTab(2) },
                        icon = { Icon(imageVector = Icons.Default.Key, contentDescription = "Permissions") },
                        label = { Text("Permissions", fontSize = 11.sp, fontWeight = if (uiState.selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF003840),
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_permissions")
                    )

                    NavigationBarItem(
                        selected = uiState.selectedTab == 3,
                        onClick = { viewModel.setTab(3) },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = if (uiState.selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF003840),
                            selectedTextColor = CyanNeon,
                            indicatorColor = CyanNeon,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val selectedApp = uiState.selectedApp
            if (selectedApp != null) {
                AppDetailsScreen(
                    app = selectedApp,
                    onBackClick = { viewModel.selectApp(null) }
                )
            } else {
                when (uiState.selectedTab) {
                    0 -> DashboardScreen(
                        uiState = uiState,
                        onScanClick = { viewModel.startScan() },
                        onAppClick = { app -> viewModel.selectApp(app) },
                        onToggleDemo = { enable -> viewModel.toggleDemoMode(enable) },
                        onViewAllAppsClick = { viewModel.setTab(1) }
                    )
                    1 -> AppInventoryScreen(
                        uiState = uiState,
                        onAppClick = { app -> viewModel.selectApp(app) },
                        onSearchChange = { query -> viewModel.updateSearchQuery(query) },
                        onFilterRisk = { risk -> viewModel.setFilterRiskLevel(risk) },
                        onSortChange = { sort -> viewModel.setSortOption(sort) },
                        onToggleSystemApps = { toggle -> viewModel.toggleSystemApps(toggle) },
                        onRescanClick = { viewModel.startScan() }
                    )
                    2 -> PermissionsExplorerScreen(
                        uiState = uiState,
                        onAppClick = { app -> viewModel.selectApp(app) }
                    )
                    3 -> SettingsScreen(
                        uiState = uiState,
                        onToggleDemo = { enable -> viewModel.toggleDemoMode(enable) }
                    )
                }
            }
        }
    }
}
