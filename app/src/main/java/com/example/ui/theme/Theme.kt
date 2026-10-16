package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SentinelDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = VioletNeon,
    onSecondary = Color(0xFF280058),
    secondaryContainer = Color(0xFF45008F),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = BlueNeon,
    background = SentinelBgDark,
    onBackground = TextPrimary,
    surface = SentinelSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SentinelCardDark,
    onSurfaceVariant = TextSecondary,
    outline = SentinelCardBorder,
    outlineVariant = Color(0xFF1E293B)
)

private val SentinelLightColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF6F3DBE),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEADBFF),
    onSecondaryContainer = Color(0xFF260058),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun SentinelTheme(
    darkTheme: Boolean = true, // Default to polished cybersecurity dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SentinelDarkColorScheme else SentinelLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SentinelTheme(darkTheme = darkTheme, content = content)
}
