package com.example.sentinel.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinel.model.InstalledApp
import com.example.sentinel.model.RiskLevel
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.RiskBgCritical
import com.example.ui.theme.RiskBgHigh
import com.example.ui.theme.RiskBgLow
import com.example.ui.theme.RiskBgMedium
import com.example.ui.theme.RiskColorCritical
import com.example.ui.theme.RiskColorHigh
import com.example.ui.theme.RiskColorLow
import com.example.ui.theme.RiskColorMedium
import com.example.ui.theme.SentinelCardBorder
import com.example.ui.theme.SentinelCardDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletNeon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: SentinelUiState,
    onScanClick: () -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onToggleDemo: (Boolean) -> Unit,
    onViewAllAppsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = uiState.auditSummary

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DashboardHeader(
                isScanning = uiState.isScanning,
                hasThreats = (summary?.criticalCount ?: 0) + (summary?.highCount ?: 0) > 0,
                lastScanTime = uiState.lastScanTime,
                isDemoMode = uiState.isDemoMode,
                onToggleDemo = onToggleDemo
            )
        }

        // Scanning Status or Scan Now Action
        item {
            if (uiState.isScanning) {
                ScanningProgressCard(
                    progress = uiState.scanProgress,
                    currentApp = uiState.currentAppScanning
                )
            } else {
                ScanActionCard(
                    totalApps = summary?.totalScanned ?: 0,
                    onScanClick = onScanClick
                )
            }
        }

        // Risk Stats Cards
        if (summary != null) {
            item {
                Text(
                    text = "AUDIT INVENTORY",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                RiskMetricsGrid(summary = summary)
            }

            // Visual Risk Distribution Bar
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
                    border = BorderStroke(1.dp, SentinelCardBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Risk Level Distribution",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        RiskDistributionBar(
                            low = summary.lowCount,
                            medium = summary.mediumCount,
                            high = summary.highCount,
                            critical = summary.criticalCount
                        )
                    }
                }
            }

            // Top Risky Apps
            if (summary.topRiskyApps.isNotEmpty()) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "HIGHEST RISK APPLICATIONS",
                            color = CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Button(
                            onClick = onViewAllAppsClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = CyanNeon
                            )
                        ) {
                            Text("View All (${summary.totalScanned})", fontSize = 12.sp)
                        }
                    }
                }

                items(summary.topRiskyApps) { app ->
                    AppCard(
                        app = app,
                        onClick = { onAppClick(app) }
                    )
                }
            }

            // Recent Findings Highlights
            if (summary.recentFindings.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SECURITY FINDINGS HIGHLIGHTS",
                        color = VioletNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                items(summary.recentFindings.take(3)) { (_, finding) ->
                    FindingCard(finding = finding)
                }
            }
        } else if (!uiState.isScanning) {
            item {
                EmptyStateCard(onScanClick = onScanClick)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    isScanning: Boolean,
    hasThreats: Boolean,
    lastScanTime: Long,
    isDemoMode: Boolean,
    onToggleDemo: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(1.dp, SentinelCardBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF003840))
                            .border(1.dp, CyanNeon, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Sentinel Shield",
                            tint = CyanNeon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SENTINEL",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "On-Device App Risk Auditor",
                            color = CyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status chip
                val statusColor = if (hasThreats) RiskColorHigh else RiskColorLow
                val statusText = if (hasThreats) "ATTENTION" else "HEALTHY"
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                val timeStr = if (lastScanTime > 0) {
                    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    "Audited: " + sdf.format(Date(lastScanTime))
                } else {
                    "Scan pending"
                }

                Text(
                    text = "$timeStr • 100% Offline Analysis",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Demo fixture switch chip
                FilterChip(
                    selected = isDemoMode,
                    onClick = { onToggleDemo(!isDemoMode) },
                    label = {
                        Text(
                            text = if (isDemoMode) "Demo Fixtures ON" else "Demo Fixtures",
                            fontSize = 10.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VioletNeon.copy(alpha = 0.2f),
                        selectedLabelColor = VioletNeon
                    ),
                    modifier = Modifier.testTag("toggle_demo_fixtures")
                )
            }
        }
    }
}

@Composable
private fun ScanActionCard(
    totalApps: Int,
    onScanClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E38)),
        border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Device Permission Audit",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (totalApps > 0) "$totalApps packages analyzed locally" else "Analyze capabilities and special access",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("scan_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Scan",
                    tint = Color(0xFF003840),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Scan Now",
                    color = Color(0xFF003840),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScanningProgressCard(
    progress: Pair<Int, Int>?,
    currentApp: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E38)),
        border = BorderStroke(1.dp, CyanNeon),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Auditing",
                    tint = CyanNeon,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Auditing Installed Applications…",
                        color = CyanNeon,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentApp.ifEmpty { "Inspecting declared permissions..." },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val current = progress?.first ?: 0
            val total = (progress?.second ?: 1).coerceAtLeast(1)
            val frac = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)

            LinearProgressIndicator(
                progress = { frac },
                color = CyanNeon,
                trackColor = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Processing $current of $total packages",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${(frac * 100).toInt()}%",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RiskMetricsGrid(summary: com.example.sentinel.model.AuditSummary) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        MetricBox("Total", summary.totalScanned.toString(), TextPrimary, Color(0xFF1E293B), Modifier.weight(1f))
        MetricBox("Low", summary.lowCount.toString(), RiskColorLow, RiskBgLow, Modifier.weight(1f))
        MetricBox("Med", summary.mediumCount.toString(), RiskColorMedium, RiskBgMedium, Modifier.weight(1f))
        MetricBox("High", summary.highCount.toString(), RiskColorHigh, RiskBgHigh, Modifier.weight(1f))
        MetricBox("Crit", summary.criticalCount.toString(), RiskColorCritical, RiskBgCritical, Modifier.weight(1f))
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(
                text = value,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyStateCard(onScanClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(1.dp, SentinelCardBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = CyanNeon,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "No Application Audit Completed",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sentinel audits on-device permissions, special access capabilities, and dangerous permission combinations.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
            )
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Start Full Device Scan",
                    color = Color(0xFF003840),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
