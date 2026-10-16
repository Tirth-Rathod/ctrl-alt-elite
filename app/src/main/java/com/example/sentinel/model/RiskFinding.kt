package com.example.sentinel.model

data class RiskFinding(
    val id: String,
    val title: String,
    val capabilityOrCombination: String,
    val evidence: String,
    val whyItMatters: String,
    val recommendation: String,
    val severity: RiskLevel,
    val category: PermissionCategory? = null
)

data class RiskScoreBreakdown(
    val categoryScores: Map<PermissionCategory, Int>,
    val categoryMaxWeights: Map<PermissionCategory, Int>,
    val matchedRules: List<Pair<String, Int>>,
    val combinationBonus: Int,
    val rawBaseScore: Int,
    val finalScore: Int
)
