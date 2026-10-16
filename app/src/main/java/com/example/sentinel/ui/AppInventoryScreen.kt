package com.example.sentinel.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinel.model.InstalledApp
import com.example.sentinel.model.RiskLevel
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.RiskColorCritical
import com.example.ui.theme.RiskColorHigh
import com.example.ui.theme.RiskColorLow
import com.example.ui.theme.RiskColorMedium
import com.example.ui.theme.SentinelCardBorder
import com.example.ui.theme.SentinelCardDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppInventoryScreen(
    uiState: SentinelUiState,
    onAppClick: (InstalledApp) -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterRisk: (RiskLevel?) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onToggleSystemApps: (Boolean) -> Unit,
    onRescanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter apps based on search query, risk level filter, and category filter
    val filteredApps = remember(
        uiState.installedApps,
        uiState.searchQuery,
        uiState.filterRiskLevel,
        uiState.filterCategory
    ) {
        uiState.installedApps.filter { app ->
            val matchesQuery = uiState.searchQuery.isEmpty() ||
                app.appLabel.contains(uiState.searchQuery, ignoreCase = true) ||
                app.packageName.contains(uiState.searchQuery, ignoreCase = true)

            val matchesRisk = uiState.filterRiskLevel == null || app.riskLevel == uiState.filterRiskLevel

            val matchesCat = uiState.filterCategory == null ||
                app.permissions.any { it.category == uiState.filterCategory }

            matchesQuery && matchesRisk && matchesCat
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "INSTALLED APPLICATIONS",
                        color = CyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${filteredApps.size} applications match filters",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sort button with dropdown
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Sort Options",
                                tint = CyanNeon
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(SentinelCardDark)
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            color = if (uiState.sortOption == option) CyanNeon else TextPrimary,
                                            fontWeight = if (uiState.sortOption == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortChange(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Rescan button
                    IconButton(
                        onClick = onRescanClick,
                        modifier = Modifier.testTag("inventory_rescan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan",
                            tint = CyanNeon
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by app or package...", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = SentinelCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SentinelCardDark,
                    unfocusedContainerColor = SentinelCardDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_apps_input")
            )
        }

        // Filter Chips row & System App Toggle
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = uiState.filterRiskLevel == null,
                        onClick = { onFilterRisk(null) },
                        label = { Text("All (${uiState.installedApps.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                            selectedLabelColor = CyanNeon
                        )
                    )
                    FilterChip(
                        selected = uiState.filterRiskLevel == RiskLevel.CRITICAL,
                        onClick = { onFilterRisk(if (uiState.filterRiskLevel == RiskLevel.CRITICAL) null else RiskLevel.CRITICAL) },
                        label = { Text("Critical", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RiskColorCritical.copy(alpha = 0.2f),
                            selectedLabelColor = RiskColorCritical
                        )
                    )
                    FilterChip(
                        selected = uiState.filterRiskLevel == RiskLevel.HIGH,
                        onClick = { onFilterRisk(if (uiState.filterRiskLevel == RiskLevel.HIGH) null else RiskLevel.HIGH) },
                        label = { Text("High", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RiskColorHigh.copy(alpha = 0.2f),
                            selectedLabelColor = RiskColorHigh
                        )
                    )
                    FilterChip(
                        selected = uiState.filterRiskLevel == RiskLevel.MEDIUM,
                        onClick = { onFilterRisk(if (uiState.filterRiskLevel == RiskLevel.MEDIUM) null else RiskLevel.MEDIUM) },
                        label = { Text("Medium", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RiskColorMedium.copy(alpha = 0.2f),
                            selectedLabelColor = RiskColorMedium
                        )
                    )
                    FilterChip(
                        selected = uiState.filterRiskLevel == RiskLevel.LOW,
                        onClick = { onFilterRisk(if (uiState.filterRiskLevel == RiskLevel.LOW) null else RiskLevel.LOW) },
                        label = { Text("Low", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RiskColorLow.copy(alpha = 0.2f),
                            selectedLabelColor = RiskColorLow
                        )
                    )
                }

                // System Apps Toggle
                Card(
                    colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
                    border = BorderStroke(1.dp, SentinelCardBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = "Include System Applications",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Audits pre-installed vendor and OS framework packages",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = uiState.includeSystemApps,
                            onCheckedChange = onToggleSystemApps,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyanNeon,
                                checkedTrackColor = Color(0xFF004F58)
                            ),
                            modifier = Modifier.testTag("toggle_system_apps")
                        )
                    }
                }
            }
        }

        // Active Category Filter Indicator
        if (uiState.filterCategory != null) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Filtering by Category: ${uiState.filterCategory.title}",
                        color = CyanNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onFilterRisk(null) }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear category filter",
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // App list items
        if (filteredApps.isNotEmpty()) {
            items(filteredApps, key = { it.packageName }) { app ->
                AppCard(
                    app = app,
                    onClick = { onAppClick(app) }
                )
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
                    border = BorderStroke(1.dp, SentinelCardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "No Applications Found",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No applications match your active search or risk filters.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
