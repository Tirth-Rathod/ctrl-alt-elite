package com.example.sentinel.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentinel.util.ReportExporter
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
import com.example.ui.theme.VioletNeon

@Composable
fun SettingsScreen(
    uiState: SentinelUiState,
    onToggleDemo: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRubricDetails by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = "SETTINGS & AUDIT POLICY",
                    color = CyanNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Privacy guarantees, scoring rules, and audit report generation",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Privacy Guarantee Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
                border = BorderStroke(1.dp, RiskColorLow.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Privacy",
                            tint = RiskColorLow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Offline-First Privacy Guarantee",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sentinel runs 100% on-device. All permission analysis, special access probing, risk scoring, and report generation take place within the local Android OS sandbox. Zero telemetry, package lists, or device identifiers are ever uploaded to cloud servers.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Export Report Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
                border = BorderStroke(1.dp, SentinelCardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Export Security Audit Report",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Generate on-device audit summary with findings",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            ReportExporter.shareAuditReport(
                                context = context,
                                summary = uiState.auditSummary,
                                apps = uiState.installedApps
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF003840),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate & Share Audit Report",
                            color = Color(0xFF003840),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Demo Mode for Hackathon Judges
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
                border = BorderStroke(1.dp, SentinelCardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = VioletNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hackathon Judging Fixtures",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Injects safe demo fixtures (SMS exfiltration, context mismatch, tapjacking) to demonstrate high-risk evaluations.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = uiState.isDemoMode,
                        onCheckedChange = onToggleDemo,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VioletNeon,
                            checkedTrackColor = Color(0xFF45008F)
                        ),
                        modifier = Modifier.testTag("toggle_demo_fixtures_settings")
                    )
                }
            }
        }

        // Scoring Rubric Viewer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
                border = BorderStroke(1.dp, SentinelCardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRubricDetails = !showRubricDetails }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Explainable Scoring Rubric",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = if (showRubricDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = showRubricDetails) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = "RISK BANDS (0–100 SCALE)",
                                color = CyanNeon,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RubricRow("0 – 24", "LOW RISK", RiskColorLow, "Standard or sandboxed permissions")
                            RubricRow("25 – 49", "MEDIUM RISK", RiskColorMedium, "Moderate sensitive permissions (Camera, Mic, Storage)")
                            RubricRow("50 – 74", "HIGH RISK", RiskColorHigh, "Privileged capabilities or multi-permission combinations")
                            RubricRow("75 – 100", "CRITICAL RISK", RiskColorCritical, "Active Special Access, SMS exfiltration, or tapjacking vectors")

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "CATEGORY BASE WEIGHTS (TOTAL: 106)",
                                color = VioletNeon,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RubricRow("30 pts", "Special Access", TextPrimary, "Accessibility, Device Admin, Overlay, Notification Listener")
                            RubricRow("20 pts", "SMS & Messaging", TextPrimary, "READ_SMS, RECEIVE_SMS, SEND_SMS (2FA / OTP exposure)")
                            RubricRow("12 pts", "Contacts & Call Logs", TextPrimary, "READ_CONTACTS, READ_CALL_LOG, call history")
                            RubricRow("10 pts", "Camera & Microphone", TextPrimary, "Live image/video capture, ambient room audio")
                            RubricRow("10 pts", "Location Tracking", TextPrimary, "GPS fine coordinates, background geofencing")
                            RubricRow("8 pts", "Phone & Identity", TextPrimary, "Telephony state, phone numbers, call dialer")
                            RubricRow("8 pts", "Storage & Media", TextPrimary, "Shared files, photos, videos, manage all files")
                            RubricRow("6 pts", "Background & Boot", TextPrimary, "Autostart on boot, wake locks, battery exemptions")
                            RubricRow("2 pts", "Network Communication", TextPrimary, "Internet connectivity (evaluated with data sources)")

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "COMBINATION BONUSES",
                                color = RiskColorHigh,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RubricRow("+15 pts", "SMS + Internet + Boot", RiskColorHigh, "Exfiltration pipeline & persistent startup")
                            RubricRow("+18 pts", "Accessibility + Overlay", RiskColorCritical, "Interface cloaking & tapjacking potential")
                            RubricRow("+20 pts", "Active Device Admin", RiskColorCritical, "Elevated system administrative control")
                            RubricRow("+12 pts", "Context Mismatch", RiskColorHigh, "Disproportionate permissions for utility app")
                            RubricRow("+10 pts", "Sideloaded Source", RiskColorMedium, "Outside package installer with sensitive permissions")
                            RubricRow("+8 pts", "Background Location", RiskColorHigh, "Continuous tracking with personal data")
                            RubricRow("+12 pts", "Notification Listener", RiskColorHigh, "Active notification & OTP interception pipeline")
                        }
                    }
                }
            }
        }

        // About & Version Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
                border = BorderStroke(1.dp, SentinelCardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sentinel — On-Device App Risk Auditor",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Built for Mobile Security Hackathon 2026\nPackage: com.aistudio.sentinel.auditor\nManifest Visibility: QUERY_ALL_PACKAGES active\nKotlin • Jetpack Compose • Material 3",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RubricRow(weight: String, title: String, titleColor: Color, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = weight,
            color = CyanNeon,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(60.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
