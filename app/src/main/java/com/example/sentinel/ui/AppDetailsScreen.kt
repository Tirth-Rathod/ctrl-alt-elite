package com.example.sentinel.ui

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinel.model.AppPermission
import com.example.sentinel.model.InstalledApp
import com.example.sentinel.model.PermissionCategory
import com.example.sentinel.model.RiskLevel
import com.example.sentinel.model.SpecialAccessStatus
import com.example.sentinel.model.SpecialAccessType
import com.example.sentinel.util.AppSettingsNavigator
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppDetailsScreen(
    app: InstalledApp,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val context = LocalContext.current

    var permissionFilterGrantedOnly by remember { mutableStateOf(false) }

    val filteredPermissions = remember(app.permissions, permissionFilterGrantedOnly) {
        if (permissionFilterGrantedOnly) {
            app.permissions.filter { it.isGranted }
        } else {
            app.permissions
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("app_details_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyanNeon
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "APPLICATION AUDIT DETAILS",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // App Header Card
        item {
            AppHeaderCard(app = app)
        }

        // Action Buttons Row (Settings, Special Access, Uninstall)
        item {
            ActionButtonsRow(app = app, context = context)
        }

        // Context Mismatch Warning (if any)
        if (app.contextMismatchWarning != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RiskBgHigh),
                    border = BorderStroke(1.dp, RiskColorHigh),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Context Mismatch",
                            tint = RiskColorHigh,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Context Mismatch Detected",
                                color = RiskColorHigh,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.contextMismatchWarning,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        // Explainable Findings Section
        item {
            Text(
                text = "EXPLAINABLE RISK FINDINGS (${app.findings.size})",
                color = VioletNeon,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        items(app.findings, key = { it.id }) { finding ->
            FindingCard(finding = finding)
        }

        // Score Breakdown Section
        item {
            ScoreBreakdownCard(app = app)
        }

        // Special Access Matrix
        item {
            SpecialAccessMatrixCard(app = app, context = context)
        }

        // Permissions Section with Granted/Declared toggle
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DECLARED VS GRANTED PERMISSIONS",
                        color = CyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row {
                        FilterChip(
                            selected = !permissionFilterGrantedOnly,
                            onClick = { permissionFilterGrantedOnly = false },
                            label = { Text("All (${app.permissions.size})", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                                selectedLabelColor = CyanNeon
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = permissionFilterGrantedOnly,
                            onClick = { permissionFilterGrantedOnly = true },
                            label = { Text("Granted (${app.grantedPermissionsCount})", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RiskColorHigh.copy(alpha = 0.2f),
                                selectedLabelColor = RiskColorHigh
                            )
                        )
                    }
                }
            }
        }

        // Permissions List
        if (filteredPermissions.isNotEmpty()) {
            items(filteredPermissions, key = { it.permissionName }) { perm ->
                PermissionDetailCard(permission = perm)
            }
        } else {
            item {
                Text(
                    text = "No permissions in this filter.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AppHeaderCard(app: InstalledApp) {
    val (bgRisk, textRisk) = when (app.riskLevel) {
        RiskLevel.CRITICAL -> RiskBgCritical to RiskColorCritical
        RiskLevel.HIGH -> RiskBgHigh to RiskColorHigh
        RiskLevel.MEDIUM -> RiskBgMedium to RiskColorMedium
        RiskLevel.LOW -> RiskBgLow to RiskColorLow
    }

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(1.dp, SentinelCardBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppIconView(
                    drawable = app.iconDrawable,
                    label = app.appLabel,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appLabel,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = app.packageName,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Version: ${app.versionName} (${app.versionCode}) • SDK ${app.targetSdk}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Banner
            Surface(
                color = bgRisk,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, textRisk),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "SECURITY RISK SCORE",
                            color = textRisk,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${app.riskScore}",
                                color = textRisk,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = " / 100",
                                color = textRisk.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = textRisk,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = app.riskLevel.label,
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.riskLevel.description.substringBefore("."),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MetadataRow("Installer Source", app.installerPackageName ?: "Sideloaded / Unverified")
                MetadataRow("First Installed", if (app.firstInstallTime > 0) sdf.format(Date(app.firstInstallTime)) else "Pre-installed")
                MetadataRow("Last Updated", if (app.lastUpdateTime > 0) sdf.format(Date(app.lastUpdateTime)) else "Unknown")
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ActionButtonsRow(app: InstalledApp, context: Context) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // App Settings
        Button(
            onClick = { AppSettingsNavigator.openAppDetails(context, app.packageName) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2B46)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("action_open_app_settings")
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Settings", color = CyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Special Access Settings
        Button(
            onClick = { AppSettingsNavigator.openOverlaySettings(context, app.packageName) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2B46)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1.2f)
                .testTag("action_open_special_access")
        ) {
            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = VioletNeon, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Special Access", color = VioletNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Uninstall App
        OutlinedButton(
            onClick = { AppSettingsNavigator.requestUninstall(context, app.packageName) },
            border = BorderStroke(1.dp, RiskColorCritical.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("action_uninstall_app")
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = RiskColorCritical, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Uninstall", color = RiskColorCritical, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ScoreBreakdownCard(app: InstalledApp) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(1.dp, SentinelCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Risk Score Calibration Breakdown",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Show non-zero categories
            app.scoreBreakdown.categoryScores.forEach { (category, score) ->
                val maxWeight = app.scoreBreakdown.categoryMaxWeights[category] ?: category.baseWeight
                CategoryContributionBar(
                    category = category,
                    score = score,
                    maxWeight = maxWeight
                )
            }

            // Combination bonuses
            if (app.scoreBreakdown.matchedRules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "COMBINATION BONUSES APPLIED",
                    color = VioletNeon,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                app.scoreBreakdown.matchedRules.forEach { (ruleName, bonus) ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Text(text = ruleName, color = TextSecondary, fontSize = 11.sp)
                        Text(text = "+$bonus pts", color = RiskColorHigh, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialAccessMatrixCard(app: InstalledApp, context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(1.dp, SentinelCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Special Access Audit Matrix",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${app.activeSpecialAccessCount} Active",
                    color = if (app.activeSpecialAccessCount > 0) RiskColorCritical else RiskColorLow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            SpecialAccessType.entries.forEach { type ->
                val status = app.specialAccesses[type] ?: SpecialAccessStatus.NOT_REQUESTED
                SpecialAccessRow(type = type, status = status)
            }
        }
    }
}

@Composable
private fun SpecialAccessRow(type: SpecialAccessType, status: SpecialAccessStatus) {
    val (statusBg, statusText) = when (status) {
        SpecialAccessStatus.ENABLED_OR_GRANTED -> RiskBgCritical to RiskColorCritical
        SpecialAccessStatus.DECLARED_ONLY -> Color(0xFF1E293B) to Color(0xFFFFB74D)
        SpecialAccessStatus.NOT_REQUESTED -> Color(0xFF131C2E) to TextMuted
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = type.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = type.description, color = TextMuted, fontSize = 10.sp, maxLines = 1)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = statusBg,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(0.5.dp, statusText.copy(alpha = 0.5f))
        ) {
            Text(
                text = status.label,
                color = statusText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun PermissionDetailCard(permission: AppPermission) {
    val grantedColor = if (permission.isGranted) RiskColorLow else TextMuted
    val dangerColor = if (permission.isDangerous) RiskColorHigh else CyanNeon

    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(0.5.dp, SentinelCardBorder),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = permission.simpleName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )

                // Granted status badge
                Surface(
                    color = if (permission.isGranted) Color(0x3300E676) else Color(0x22FFFFFF),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (permission.isGranted) "GRANTED" else "DECLARED ONLY",
                        color = grantedColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Category & Protection level
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${permission.category.title} • ${permission.protectionLevel}",
                    color = dangerColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (permission.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = permission.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            if (permission.securityImplication.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Risk: ${permission.securityImplication}",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
