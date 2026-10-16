package com.example.sentinel.model

import androidx.compose.ui.graphics.Color

enum class RiskLevel(
    val label: String,
    val minScore: Int,
    val maxScore: Int,
    val color: Color,
    val description: String
) {
    LOW(
        label = "LOW",
        minScore = 0,
        maxScore = 24,
        color = Color(0xFF00E676), // Bright Green
        description = "Standard or minimal capabilities. Unlikely to present privacy or device-integrity threats."
    ),
    MEDIUM(
        label = "MEDIUM",
        minScore = 25,
        maxScore = 49,
        color = Color(0xFFFFD600), // Vibrant Amber
        description = "Moderate sensitivity permissions. Typical for communication or multimedia applications."
    ),
    HIGH(
        label = "HIGH",
        minScore = 50,
        maxScore = 74,
        color = Color(0xFFFF9100), // Deep Orange
        description = "Significant sensitive capabilities or multi-permission combinations requiring close user review."
    ),
    CRITICAL(
        label = "CRITICAL",
        minScore = 75,
        maxScore = 100,
        color = Color(0xFFFF3D00), // Crimson Red
        description = "Highly privileged capabilities such as active special access, SMS exfiltration vectors, or cloak overlay combinations."
    );

    companion object {
        fun fromScore(score: Int): RiskLevel = when {
            score >= 75 -> CRITICAL
            score >= 50 -> HIGH
            score >= 25 -> MEDIUM
            else -> LOW
        }
    }
}
