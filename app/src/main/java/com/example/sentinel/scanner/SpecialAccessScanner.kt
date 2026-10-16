package com.example.sentinel.scanner

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.example.sentinel.model.SpecialAccessStatus
import com.example.sentinel.model.SpecialAccessType

class SpecialAccessScanner(private val context: Context) {

    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    private val appOpsManager =
        context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager

    // Cached system-wide enabled services sets
    private var enabledAccessibilityPackages: Set<String> = emptySet()
    private var enabledNotificationListenerPackages: Set<String> = emptySet()
    private var activeDeviceAdminPackages: Set<String> = emptySet()

    fun refreshSystemCaches() {
        enabledAccessibilityPackages = getEnabledAccessibilityPackages()
        enabledNotificationListenerPackages = getEnabledNotificationListenerPackages()
        activeDeviceAdminPackages = getActiveDeviceAdminPackages()
    }

    private fun getEnabledAccessibilityPackages(): Set<String> {
        val result = mutableSetOf<String>()
        try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            // Format: "com.example.app/com.example.app.MyService:com.other.app/..."
            enabledServices.split(":").forEach { entry ->
                val pkg = entry.substringBefore("/").trim()
                if (pkg.isNotEmpty()) {
                    result.add(pkg)
                }
            }
        } catch (_: Exception) {}
        return result
    }

    private fun getEnabledNotificationListenerPackages(): Set<String> {
        val result = mutableSetOf<String>()
        try {
            result.addAll(NotificationManagerCompat.getEnabledListenerPackages(context))
        } catch (_: Exception) {
            try {
                val flat = Settings.Secure.getString(
                    context.contentResolver,
                    "enabled_notification_listeners"
                ) ?: ""
                flat.split(":").forEach { entry ->
                    val pkg = entry.substringBefore("/").trim()
                    if (pkg.isNotEmpty()) result.add(pkg)
                }
            } catch (_: Exception) {}
        }
        return result
    }

    private fun getActiveDeviceAdminPackages(): Set<String> {
        val result = mutableSetOf<String>()
        try {
            val admins = devicePolicyManager?.activeAdmins
            admins?.forEach { admin ->
                result.add(admin.packageName)
            }
        } catch (_: Exception) {}
        return result
    }

    fun scanAppSpecialAccess(
        packageInfo: PackageInfo,
        declaredPermissions: Set<String>
    ): Map<SpecialAccessType, SpecialAccessStatus> {
        val packageName = packageInfo.packageName
        val uid = packageInfo.applicationInfo?.uid ?: Process.myUid()
        val result = mutableMapOf<SpecialAccessType, SpecialAccessStatus>()

        // 1. Accessibility Service
        val declaresAccessibility = declaredPermissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE") ||
            (packageInfo.services?.any { it.permission == "android.permission.BIND_ACCESSIBILITY_SERVICE" } == true)
        val isAccessibilityEnabled = enabledAccessibilityPackages.contains(packageName)
        result[SpecialAccessType.ACCESSIBILITY_SERVICE] = when {
            isAccessibilityEnabled -> SpecialAccessStatus.ENABLED_OR_GRANTED
            declaresAccessibility -> SpecialAccessStatus.DECLARED_ONLY
            else -> SpecialAccessStatus.NOT_REQUESTED
        }

        // 2. Overlay / System Alert Window
        val declaresOverlay = declaredPermissions.contains("android.permission.SYSTEM_ALERT_WINDOW")
        val isOverlayGranted = checkAppOp(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, uid, packageName)
        result[SpecialAccessType.SYSTEM_ALERT_WINDOW] = when {
            isOverlayGranted -> SpecialAccessStatus.ENABLED_OR_GRANTED
            declaresOverlay -> SpecialAccessStatus.DECLARED_ONLY
            else -> SpecialAccessStatus.NOT_REQUESTED
        }

        // 3. Device Administrator
        val declaresDeviceAdmin = declaredPermissions.contains("android.permission.BIND_DEVICE_ADMIN") ||
            (packageInfo.receivers?.any { it.permission == "android.permission.BIND_DEVICE_ADMIN" } == true)
        val isAdminActive = activeDeviceAdminPackages.contains(packageName)
        result[SpecialAccessType.DEVICE_ADMIN] = when {
            isAdminActive -> SpecialAccessStatus.ENABLED_OR_GRANTED
            declaresDeviceAdmin -> SpecialAccessStatus.DECLARED_ONLY
            else -> SpecialAccessStatus.NOT_REQUESTED
        }

        // 4. Notification Listener
        val declaresNotificationListener = declaredPermissions.contains("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE") ||
            (packageInfo.services?.any { it.permission == "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" } == true)
        val isNotificationListenerEnabled = enabledNotificationListenerPackages.contains(packageName)
        result[SpecialAccessType.NOTIFICATION_LISTENER] = when {
            isNotificationListenerEnabled -> SpecialAccessStatus.ENABLED_OR_GRANTED
            declaresNotificationListener -> SpecialAccessStatus.DECLARED_ONLY
            else -> SpecialAccessStatus.NOT_REQUESTED
        }

        // 5. Package Install
        val declaresInstallPackages = declaredPermissions.contains("android.permission.REQUEST_INSTALL_PACKAGES")
        val isInstallAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkAppOp("android:request_install_packages", uid, packageName)
        } else {
            false
        }
        result[SpecialAccessType.PACKAGE_INSTALL] = when {
            isInstallAllowed -> SpecialAccessStatus.ENABLED_OR_GRANTED
            declaresInstallPackages -> SpecialAccessStatus.DECLARED_ONLY
            else -> SpecialAccessStatus.NOT_REQUESTED
        }

        // 6. Usage Stats
        val declaresUsageStats = declaredPermissions.contains("android.permission.PACKAGE_USAGE_STATS")
        val isUsageStatsAllowed = checkAppOp(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, packageName)
        result[SpecialAccessType.USAGE_STATS] = when {
            isUsageStatsAllowed -> SpecialAccessStatus.ENABLED_OR_GRANTED
            declaresUsageStats -> SpecialAccessStatus.DECLARED_ONLY
            else -> SpecialAccessStatus.NOT_REQUESTED
        }

        return result
    }

    private fun checkAppOp(op: String, uid: Int, packageName: String): Boolean {
        if (appOpsManager == null) return false
        return try {
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(op, uid, packageName)
            } else {
                @Suppress("DEPRECATION")
                appOpsManager.checkOpNoThrow(op, uid, packageName)
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }
}
