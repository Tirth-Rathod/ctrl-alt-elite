package com.example.sentinel.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build
import com.example.sentinel.model.AppPermission
import com.example.sentinel.model.InstalledApp
import com.example.sentinel.risk.RiskEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppScanner(
    private val context: Context,
    private val specialAccessScanner: SpecialAccessScanner = SpecialAccessScanner(context),
    private val riskEngine: RiskEngine = RiskEngine()
) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun scanInstalledApps(
        includeSystemApps: Boolean,
        onProgress: ((current: Int, total: Int, appName: String) -> Unit)? = null
    ): List<InstalledApp> = withContext(Dispatchers.IO) {
        specialAccessScanner.refreshSystemCaches()

        val packageList: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(
                        (PackageManager.GET_PERMISSIONS or
                            PackageManager.GET_SERVICES or
                            PackageManager.GET_RECEIVERS).toLong()
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(
                    PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_RECEIVERS
                )
            }
        } catch (_: Exception) {
            emptyList()
        }

        val filteredPackages = packageList.filter { pkg ->
            val isSystem = (pkg.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
            if (includeSystemApps) true else !isSystem
        }

        val total = filteredPackages.size
        val scannedApps = mutableListOf<InstalledApp>()

        filteredPackages.forEachIndexed { index, pkg ->
            try {
                val app = processPackage(pkg)
                if (app != null) {
                    scannedApps.add(app)
                }
                onProgress?.invoke(index + 1, total, app?.appLabel ?: pkg.packageName)
            } catch (_: Exception) {
                // If single package reading fails (e.g. uninstalled mid-scan), continue gracefully
            }
        }

        // Sort by highest risk score first, then alphabetically
        scannedApps.sortedWith(compareByDescending<InstalledApp> { it.riskScore }.thenBy { it.appLabel.lowercase() })
    }

    private fun processPackage(pkg: PackageInfo): InstalledApp? {
        val appInfo = pkg.applicationInfo ?: return null
        val packageName = pkg.packageName

        val appLabel = try {
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }

        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val installer = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
        } catch (_: Exception) {
            null
        }

        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkg.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pkg.versionCode.toLong()
        }

        val versionName = pkg.versionName ?: "Unknown"
        val targetSdk = appInfo.targetSdkVersion

        // Extract permissions
        val declaredPermissions = mutableListOf<AppPermission>()
        val declaredNames = pkg.requestedPermissions ?: emptyArray()
        val declaredFlags = pkg.requestedPermissionsFlags ?: IntArray(0)

        declaredNames.forEachIndexed { i, permName ->
            val isGranted = if (i < declaredFlags.size) {
                (declaredFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            } else {
                false
            }

            var protectionString = "NORMAL"
            try {
                val permInfo = packageManager.getPermissionInfo(permName, 0)
                protectionString = when {
                    (permInfo.protectionLevel and PermissionInfo.PROTECTION_DANGEROUS) != 0 -> "DANGEROUS"
                    (permInfo.protectionLevel and PermissionInfo.PROTECTION_SIGNATURE) != 0 -> "SIGNATURE"
                    else -> "NORMAL"
                }
            } catch (_: Exception) {}

            val mapped = PermissionMapper.mapPermission(permName, isGranted, protectionString)
            declaredPermissions.add(mapped)
        }

        // Scan special access status
        val declaredNamesSet = declaredNames.toSet()
        val specialAccesses = specialAccessScanner.scanAppSpecialAccess(pkg, declaredNamesSet)

        // Evaluate risk
        val evaluation = riskEngine.evaluate(
            appLabel = appLabel,
            packageName = packageName,
            installerPackageName = installer,
            isSystemApp = isSystem,
            permissions = declaredPermissions,
            specialAccesses = specialAccesses
        )

        val icon = try {
            packageManager.getApplicationIcon(appInfo)
        } catch (_: Exception) {
            null
        }

        return InstalledApp(
            packageName = packageName,
            appLabel = appLabel,
            versionName = versionName,
            versionCode = versionCode,
            isSystemApp = isSystem,
            firstInstallTime = pkg.firstInstallTime,
            lastUpdateTime = pkg.lastUpdateTime,
            installerPackageName = installer,
            targetSdk = targetSdk,
            permissions = declaredPermissions,
            specialAccesses = specialAccesses,
            riskScore = evaluation.finalScore,
            riskLevel = evaluation.riskLevel,
            findings = evaluation.findings,
            scoreBreakdown = evaluation.scoreBreakdown,
            contextMismatchWarning = evaluation.contextMismatchWarning,
            iconDrawable = icon
        )
    }
}
