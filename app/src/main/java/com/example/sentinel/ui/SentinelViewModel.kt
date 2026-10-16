package com.example.sentinel.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentinel.model.AuditSummary
import com.example.sentinel.model.InstalledApp
import com.example.sentinel.model.PermissionCategory
import com.example.sentinel.model.RiskLevel
import com.example.sentinel.scanner.AppScanner
import com.example.sentinel.util.DemoFixture
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    HIGHEST_RISK("Highest Risk"),
    LOWEST_RISK("Lowest Risk"),
    ALPHABETICAL("Alphabetical (A-Z)"),
    MOST_PERMISSIONS("Most Dangerous Perms")
}

data class SentinelUiState(
    val isScanning: Boolean = false,
    val scanProgress: Pair<Int, Int>? = null,
    val currentAppScanning: String = "",
    val installedApps: List<InstalledApp> = emptyList(),
    val auditSummary: AuditSummary? = null,
    val selectedApp: InstalledApp? = null,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val filterRiskLevel: RiskLevel? = null,
    val filterCategory: PermissionCategory? = null,
    val sortOption: SortOption = SortOption.HIGHEST_RISK,
    val includeSystemApps: Boolean = false,
    val isDemoMode: Boolean = false,
    val lastScanTime: Long = 0L,
    val errorMessage: String? = null
)

class SentinelViewModel(application: Application) : AndroidViewModel(application) {

    private val appScanner = AppScanner(application)

    private val _uiState = MutableStateFlow(SentinelUiState())
    val uiState: StateFlow<SentinelUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        // Kick off initial real scan on startup
        startScan()
    }

    fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    scanProgress = 0 to 1,
                    currentAppScanning = "Initializing package scanner...",
                    errorMessage = null
                )
            }

            try {
                val realApps = appScanner.scanInstalledApps(
                    includeSystemApps = _uiState.value.includeSystemApps
                ) { current, total, name ->
                    _uiState.update {
                        it.copy(
                            scanProgress = current to total,
                            currentAppScanning = name
                        )
                    }
                }

                // If Demo Mode is enabled, merge in the demo test fixtures
                val combinedApps = if (_uiState.value.isDemoMode) {
                    val demoApps = DemoFixture.generateDemoApps()
                    (demoApps + realApps).distinctBy { it.packageName }
                } else {
                    realApps
                }

                val sorted = sortApps(combinedApps, _uiState.value.sortOption)
                val summary = buildSummary(sorted)

                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = null,
                        currentAppScanning = "",
                        installedApps = sorted,
                        auditSummary = summary,
                        lastScanTime = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = null,
                        currentAppScanning = "",
                        errorMessage = "Scan interrupted: ${e.message}"
                    )
                }
            }
        }
    }

    private fun buildSummary(apps: List<InstalledApp>): AuditSummary {
        val total = apps.size
        val low = apps.count { it.riskLevel == RiskLevel.LOW }
        val med = apps.count { it.riskLevel == RiskLevel.MEDIUM }
        val high = apps.count { it.riskLevel == RiskLevel.HIGH }
        val crit = apps.count { it.riskLevel == RiskLevel.CRITICAL }
        val avg = if (total > 0) apps.map { it.riskScore }.average() else 0.0

        val topRisky = apps.sortedByDescending { it.riskScore }.take(5)

        val recentFindings = apps
            .filter { it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.CRITICAL }
            .flatMap { app -> app.findings.map { app.appLabel to it } }
            .take(6)

        return AuditSummary(
            totalScanned = total,
            userAppsCount = apps.count { !it.isSystemApp },
            systemAppsCount = apps.count { it.isSystemApp },
            lowCount = low,
            mediumCount = med,
            highCount = high,
            criticalCount = crit,
            averageScore = avg,
            scanTimestamp = System.currentTimeMillis(),
            topRiskyApps = topRisky,
            recentFindings = recentFindings
        )
    }

    private fun sortApps(apps: List<InstalledApp>, sortOption: SortOption): List<InstalledApp> {
        return when (sortOption) {
            SortOption.HIGHEST_RISK -> apps.sortedWith(compareByDescending<InstalledApp> { it.riskScore }.thenBy { it.appLabel.lowercase() })
            SortOption.LOWEST_RISK -> apps.sortedWith(compareBy<InstalledApp> { it.riskScore }.thenBy { it.appLabel.lowercase() })
            SortOption.ALPHABETICAL -> apps.sortedBy { it.appLabel.lowercase() }
            SortOption.MOST_PERMISSIONS -> apps.sortedWith(compareByDescending<InstalledApp> { it.dangerousGrantedCount }.thenByDescending { it.riskScore })
        }
    }

    fun selectApp(app: InstalledApp?) {
        _uiState.update { it.copy(selectedApp = app) }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilterRiskLevel(level: RiskLevel?) {
        _uiState.update { it.copy(filterRiskLevel = level) }
    }

    fun setFilterCategory(category: PermissionCategory?) {
        _uiState.update { it.copy(filterCategory = category) }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update {
            val reSorted = sortApps(it.installedApps, option)
            it.copy(sortOption = option, installedApps = reSorted)
        }
    }

    fun toggleSystemApps(include: Boolean) {
        _uiState.update { it.copy(includeSystemApps = include) }
        startScan()
    }

    fun toggleDemoMode(enable: Boolean) {
        _uiState.update { it.copy(isDemoMode = enable) }
        startScan()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                filterRiskLevel = null,
                filterCategory = null
            )
        }
    }
}
