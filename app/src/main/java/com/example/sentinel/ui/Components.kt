package com.example.sentinel.ui

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.sentinel.model.InstalledApp
import com.example.sentinel.model.PermissionCategory
import com.example.sentinel.model.RiskFinding
import com.example.sentinel.model.RiskLevel
import com.example.sentinel.model.SpecialAccessStatus
import com.example.sentinel.model.SpecialAccessType
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
fun RiskBadge(
    score: Int,
    level: RiskLevel,
    modifier: Modifier = Modifier
) {
    val (bg, textCol) = when (level) {
        RiskLevel.CRITICAL -> Color(0x33FF3D00) to RiskColorCritical
        RiskLevel.HIGH -> Color(0x33FF9100) to RiskColorHigh
        RiskLevel.MEDIUM -> Color(0x33FFD600) to RiskColorMedium
        RiskLevel.LOW -> Color(0x3300E676) to RiskColorLow
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, textCol.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textCol)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${level.label} $score",
                color = textCol,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun AppIconView(
    drawable: Drawable?,
    label: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(drawable) {
        try {
            drawable?.toBitmap(96, 96)
        } catch (_: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "$label icon",
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SentinelCardBorder, RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SentinelCardBorder)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = label,
                tint = CyanNeon,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppCard(
    app: InstalledApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(1.dp, SentinelCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("app_card_${app.packageName}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppIconView(
                    drawable = app.iconDrawable,
                    label = app.appLabel,
                    modifier = Modifier.size(46.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appLabel,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "v${app.versionName} • SDK ${app.targetSdk}" + if (app.isSystemApp) " • System" else "",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                RiskBadge(score = app.riskScore, level = app.riskLevel)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sensitive permissions badge
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${app.dangerousGrantedCount} Dangerous Perms",
                        color = if (app.dangerousGrantedCount > 0) Color(0xFFFFB74D) else TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Active special access badges
                app.specialAccesses.forEach { (type, status) ->
                    if (status == SpecialAccessStatus.ENABLED_OR_GRANTED) {
                        Surface(
                            color = Color(0x33FF3D00),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, RiskColorCritical.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Active ${type.title.substringBefore(" ")}",
                                color = RiskColorCritical,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Context Mismatch Indicator
                if (app.contextMismatchWarning != null) {
                    Surface(
                        color = Color(0x33FF9100),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, RiskColorHigh.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Context Mismatch",
                            color = RiskColorHigh,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FindingCard(
    finding: RiskFinding,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val accentColor = when (finding.severity) {
        RiskLevel.CRITICAL -> RiskColorCritical
        RiskLevel.HIGH -> RiskColorHigh
        RiskLevel.MEDIUM -> RiskColorMedium
        RiskLevel.LOW -> RiskColorLow
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SentinelCardDark),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("finding_card_${finding.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (finding.severity == RiskLevel.LOW) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = "Severity ${finding.severity.label}",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = finding.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = finding.capabilityOrCombination,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Evidence Section
                    Text(
                        text = "EVIDENCE DETECTED",
                        color = CyanNeon,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = finding.evidence,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Why it matters
                    Text(
                        text = "SECURITY IMPLICATIONS",
                        color = VioletNeon,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = finding.whyItMatters,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Recommendation
                    Text(
                        text = "PRACTICAL ACTION",
                        color = RiskColorLow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = finding.recommendation,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RiskDistributionBar(
    low: Int,
    medium: Int,
    high: Int,
    critical: Int,
    modifier: Modifier = Modifier
) {
    val total = (low + medium + high + critical).coerceAtLeast(1).toFloat()
    val lowWeight = (low / total).coerceAtLeast(0.001f)
    val medWeight = (medium / total).coerceAtLeast(0.001f)
    val highWeight = (high / total).coerceAtLeast(0.001f)
    val critWeight = (critical / total).coerceAtLeast(0.001f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF1E293B))
        ) {
            if (low > 0) Box(modifier = Modifier.weight(lowWeight).background(RiskColorLow))
            if (medium > 0) Box(modifier = Modifier.weight(medWeight).background(RiskColorMedium))
            if (high > 0) Box(modifier = Modifier.weight(highWeight).background(RiskColorHigh))
            if (critical > 0) Box(modifier = Modifier.weight(critWeight).background(RiskColorCritical))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            LegendItem("Low", low, RiskColorLow)
            LegendItem("Med", medium, RiskColorMedium)
            LegendItem("High", high, RiskColorHigh)
            LegendItem("Crit", critical, RiskColorCritical)
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: $count",
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun CategoryContributionBar(
    category: PermissionCategory,
    score: Int,
    maxWeight: Int,
    modifier: Modifier = Modifier
) {
    val progress = (score.toFloat() / maxWeight.toFloat()).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = category.title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$score / $maxWeight pts",
                color = if (score > 0) CyanNeon else TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = if (score >= maxWeight * 0.7) RiskColorHigh else CyanNeon,
            trackColor = Color(0xFF1E293B),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
