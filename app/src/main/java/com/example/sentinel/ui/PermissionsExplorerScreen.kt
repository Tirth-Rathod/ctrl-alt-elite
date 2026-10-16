package com.example.sentinel.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import com.example.sentinel.model.PermissionCategory
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.RiskColorCritical
import com.example.ui.theme.RiskColorHigh
import com.example.ui.theme.SentinelCardBorder
import com.example.ui.theme.SentinelCardDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletNeon

@Composable
fun PermissionsExplorerScreen(
    uiState: SentinelUiState,
    onAppClick: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<PermissionCategory?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = "CAPABILITY EXPLORER",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (selectedCategory == null) "Audit apps by sensitive security capability" else "Apps holding ${selectedCategory?.title} access",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        if (selectedCategory == null) {
            // Display all 9 categories with stats
            items(PermissionCategory.entries) { category ->
                val appsWithCategory = uiState.installedApps.filter { app ->
                    app.permissions.any { it.category == category }
                }
                val grantedCount = appsWithCategory.count { app ->
                    app.permissions.any { it.category == category && it.isGranted }
                }

                CategoryCard(
                    category = category,
                    totalApps = appsWithCategory.size,
                    grantedApps = grantedCount,
                    onClick = { selectedCategory = category }
                )
            }
        } else {
            // Category Selected: Show category banner and list of apps
            val cat = selectedCategory!!
            val appsInCategory = uiState.installedApps.filter { app ->
                app.permissions.any { it.category == cat }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2B46)),
                    border = BorderStroke(1.dp, CyanNeon),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = cat.title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = CyanNeon.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Base Weight: ${cat.baseWeight} pts",
                                    color = CyanNeon,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.description,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "← Tap here to choose another category",
                            color = VioletNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { selectedCategory = null }
                        )
                    }
                }
            }

            items(appsInCategory, key = { it.packageName }) { app ->
                AppCard(
                    app = app,
                    onClick = { onAppClick(app) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CategoryCard(
    category: PermissionCategory,
    totalApps: Int,
    grantedApps: Int,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(1.dp, SentinelCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("category_card_${category.name}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = category.title,
                tint = if (grantedApps > 0 && category.baseWeight >= 15) RiskColorCritical else CyanNeon,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = category.description,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    text = "$grantedApps granted • $totalApps declared • Base: ${category.baseWeight} pts",
                    color = if (grantedApps > 0) RiskColorHigh else TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}
