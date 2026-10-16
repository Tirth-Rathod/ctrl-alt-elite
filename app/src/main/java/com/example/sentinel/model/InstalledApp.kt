package com.example.sentinel.model

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val appLabel: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val installerPackageName: String?,
    val targetSdk: Int,
    val permissions: List<AppPermission>,
    val specialAccesses: Map<SpecialAccessType, SpecialAccessStatus>,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val findings: List<RiskFinding>,
    val scoreBreakdown: RiskScoreBreakdown,
    val contextMismatchWarning: String? = null,
    val iconDrawable: Drawable? = null
) {
    val declaredPermissionsCount: Int get() = permissions.size
    val grantedPermissionsCount: Int get() = permissions.count { it.isGranted }
    val dangerousGrantedCount: Int get() = permissions.count { it.isGranted && it.isDangerous }
    val activeSpecialAccessCount: Int get() = specialAccesses.values.count { it == SpecialAccessStatus.ENABLED_OR_GRANTED }
    val declaredSpecialAccessCount: Int get() = specialAccesses.values.count { it != SpecialAccessStatus.NOT_REQUESTED }
}

data class AuditSummary(
    val totalScanned: Int,
    val userAppsCount: Int,
    val systemAppsCount: Int,
    val lowCount: Int,
    val mediumCount: Int,
    val highCount: Int,
    val criticalCount: Int,
    val averageScore: Double,
    val scanTimestamp: Long,
    val topRiskyApps: List<InstalledApp>,
    val recentFindings: List<Pair<String, RiskFinding>>
)
