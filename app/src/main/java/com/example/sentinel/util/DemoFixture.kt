package com.example.sentinel.util

import com.example.sentinel.model.AppPermission
import com.example.sentinel.model.InstalledApp
import com.example.sentinel.model.LocationPrecision
import com.example.sentinel.model.PermissionCategory
import com.example.sentinel.model.SpecialAccessStatus
import com.example.sentinel.model.SpecialAccessType
import com.example.sentinel.risk.RiskEngine

object DemoFixture {

    fun generateDemoApps(riskEngine: RiskEngine = RiskEngine()): List<InstalledApp> {
        val now = System.currentTimeMillis()

        // 1. StealthTracker Pro (SMS + Boot + Internet + Sideloaded)
        val spyPerms = listOf(
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.RECEIVE_SMS", "RECEIVE_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.SEND_SMS", "SEND_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.INTERNET", "INTERNET", PermissionCategory.NETWORK, isGranted = true),
            AppPermission("android.permission.RECEIVE_BOOT_COMPLETED", "RECEIVE_BOOT_COMPLETED", PermissionCategory.BACKGROUND_PERSISTENCE, isGranted = true),
            AppPermission("android.permission.READ_PHONE_STATE", "READ_PHONE_STATE", PermissionCategory.PHONE_IDENTITY, isGranted = true, isDangerous = true)
        )
        val spySpecials = mapOf(
            SpecialAccessType.ACCESSIBILITY_SERVICE to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.SYSTEM_ALERT_WINDOW to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.DEVICE_ADMIN to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.NOTIFICATION_LISTENER to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.PACKAGE_INSTALL to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.USAGE_STATS to SpecialAccessStatus.NOT_REQUESTED
        )
        val spyEval = riskEngine.evaluate(
            "StealthTracker Pro [DEMO FIXTURE]",
            "com.stealth.tracker.demo",
            installerPackageName = "com.google.android.packageinstaller",
            isSystemApp = false,
            permissions = spyPerms,
            specialAccesses = spySpecials
        )
        val spyApp = InstalledApp(
            packageName = "com.stealth.tracker.demo",
            appLabel = "StealthTracker Pro [DEMO FIXTURE]",
            versionName = "2.4.1",
            versionCode = 241,
            isSystemApp = false,
            firstInstallTime = now - 86400000L * 14,
            lastUpdateTime = now - 86400000L * 3,
            installerPackageName = "com.google.android.packageinstaller",
            targetSdk = 34,
            permissions = spyPerms,
            specialAccesses = spySpecials,
            riskScore = spyEval.finalScore,
            riskLevel = spyEval.riskLevel,
            findings = spyEval.findings,
            scoreBreakdown = spyEval.scoreBreakdown,
            contextMismatchWarning = spyEval.contextMismatchWarning
        )

        // 2. QuickFlash Torch (Utility Context Mismatch)
        val flashPerms = listOf(
            AppPermission("android.permission.CAMERA", "CAMERA", PermissionCategory.CAMERA_MICROPHONE, isGranted = true, isDangerous = true),
            AppPermission("android.permission.READ_CONTACTS", "READ_CONTACTS", PermissionCategory.CONTACTS_CALL_LOGS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.READ_CALL_LOG", "READ_CALL_LOG", PermissionCategory.CONTACTS_CALL_LOGS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.ACCESS_FINE_LOCATION", "ACCESS_FINE_LOCATION", PermissionCategory.LOCATION, isGranted = true, isDangerous = true, locationPrecision = LocationPrecision.FINE),
            AppPermission("android.permission.INTERNET", "INTERNET", PermissionCategory.NETWORK, isGranted = true)
        )
        val flashSpecials = mapOf(
            SpecialAccessType.ACCESSIBILITY_SERVICE to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.SYSTEM_ALERT_WINDOW to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.DEVICE_ADMIN to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.NOTIFICATION_LISTENER to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.PACKAGE_INSTALL to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.USAGE_STATS to SpecialAccessStatus.NOT_REQUESTED
        )
        val flashEval = riskEngine.evaluate(
            "Flashlight Super Bright [DEMO FIXTURE]",
            "com.bright.torch.demo",
            installerPackageName = "com.android.vending",
            isSystemApp = false,
            permissions = flashPerms,
            specialAccesses = flashSpecials
        )
        val flashApp = InstalledApp(
            packageName = "com.bright.torch.demo",
            appLabel = "Flashlight Super Bright [DEMO FIXTURE]",
            versionName = "1.0.8",
            versionCode = 108,
            isSystemApp = false,
            firstInstallTime = now - 86400000L * 25,
            lastUpdateTime = now - 86400000L * 5,
            installerPackageName = "com.android.vending",
            targetSdk = 34,
            permissions = flashPerms,
            specialAccesses = flashSpecials,
            riskScore = flashEval.finalScore,
            riskLevel = flashEval.riskLevel,
            findings = flashEval.findings,
            scoreBreakdown = flashEval.scoreBreakdown,
            contextMismatchWarning = flashEval.contextMismatchWarning
        )

        // 3. ScreenHelper Plus (Accessibility + Overlay Special Access)
        val screenPerms = listOf(
            AppPermission("android.permission.SYSTEM_ALERT_WINDOW", "SYSTEM_ALERT_WINDOW", PermissionCategory.SPECIAL_ACCESS, isGranted = true),
            AppPermission("android.permission.BIND_ACCESSIBILITY_SERVICE", "BIND_ACCESSIBILITY_SERVICE", PermissionCategory.SPECIAL_ACCESS, isGranted = true),
            AppPermission("android.permission.INTERNET", "INTERNET", PermissionCategory.NETWORK, isGranted = true)
        )
        val screenSpecials = mapOf(
            SpecialAccessType.ACCESSIBILITY_SERVICE to SpecialAccessStatus.ENABLED_OR_GRANTED,
            SpecialAccessType.SYSTEM_ALERT_WINDOW to SpecialAccessStatus.ENABLED_OR_GRANTED,
            SpecialAccessType.DEVICE_ADMIN to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.NOTIFICATION_LISTENER to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.PACKAGE_INSTALL to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.USAGE_STATS to SpecialAccessStatus.NOT_REQUESTED
        )
        val screenEval = riskEngine.evaluate(
            "ScreenHelper Plus [DEMO FIXTURE]",
            "com.screen.helper.demo",
            installerPackageName = "com.google.android.packageinstaller",
            isSystemApp = false,
            permissions = screenPerms,
            specialAccesses = screenSpecials
        )
        val screenApp = InstalledApp(
            packageName = "com.screen.helper.demo",
            appLabel = "ScreenHelper Plus [DEMO FIXTURE]",
            versionName = "3.2.0",
            versionCode = 320,
            isSystemApp = false,
            firstInstallTime = now - 86400000L * 60,
            lastUpdateTime = now - 86400000L * 10,
            installerPackageName = "com.google.android.packageinstaller",
            targetSdk = 34,
            permissions = screenPerms,
            specialAccesses = screenSpecials,
            riskScore = screenEval.finalScore,
            riskLevel = screenEval.riskLevel,
            findings = screenEval.findings,
            scoreBreakdown = screenEval.scoreBreakdown,
            contextMismatchWarning = screenEval.contextMismatchWarning
        )

        // 4. Secure Calc Pro (Safe Low Risk Utility)
        val calcPerms = listOf(
            AppPermission("android.permission.VIBRATE", "VIBRATE", PermissionCategory.BACKGROUND_PERSISTENCE, isGranted = true)
        )
        val calcSpecials = mapOf(
            SpecialAccessType.ACCESSIBILITY_SERVICE to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.SYSTEM_ALERT_WINDOW to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.DEVICE_ADMIN to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.NOTIFICATION_LISTENER to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.PACKAGE_INSTALL to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.USAGE_STATS to SpecialAccessStatus.NOT_REQUESTED
        )
        val calcEval = riskEngine.evaluate(
            "Secure Calc Pro [DEMO FIXTURE]",
            "com.secure.calc.demo",
            installerPackageName = "com.android.vending",
            isSystemApp = false,
            permissions = calcPerms,
            specialAccesses = calcSpecials
        )
        val calcApp = InstalledApp(
            packageName = "com.secure.calc.demo",
            appLabel = "Secure Calc Pro [DEMO FIXTURE]",
            versionName = "1.2.0",
            versionCode = 12,
            isSystemApp = false,
            firstInstallTime = now - 86400000L * 100,
            lastUpdateTime = now - 86400000L * 30,
            installerPackageName = "com.android.vending",
            targetSdk = 34,
            permissions = calcPerms,
            specialAccesses = calcSpecials,
            riskScore = calcEval.finalScore,
            riskLevel = calcEval.riskLevel,
            findings = calcEval.findings,
            scoreBreakdown = calcEval.scoreBreakdown,
            contextMismatchWarning = null
        )

        return listOf(screenApp, spyApp, flashApp, calcApp)
    }
}
