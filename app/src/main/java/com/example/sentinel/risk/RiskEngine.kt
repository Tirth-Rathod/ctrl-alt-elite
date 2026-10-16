package com.example.sentinel.risk

import com.example.sentinel.model.AppPermission
import com.example.sentinel.model.LocationPrecision
import com.example.sentinel.model.PermissionCategory
import com.example.sentinel.model.RiskFinding
import com.example.sentinel.model.RiskLevel
import com.example.sentinel.model.RiskScoreBreakdown
import com.example.sentinel.model.SpecialAccessStatus
import com.example.sentinel.model.SpecialAccessType
import kotlin.math.roundToInt

class RiskEngine {

    data class EvaluationResult(
        val finalScore: Int,
        val riskLevel: RiskLevel,
        val scoreBreakdown: RiskScoreBreakdown,
        val findings: List<RiskFinding>,
        val contextMismatchWarning: String?
    )

    fun evaluate(
        appLabel: String,
        packageName: String,
        installerPackageName: String?,
        isSystemApp: Boolean,
        permissions: List<AppPermission>,
        specialAccesses: Map<SpecialAccessType, SpecialAccessStatus>
    ): EvaluationResult {
        // Prevent duplicate counting of the same permission name
        val distinctPermissions = permissions.distinctBy { it.permissionName }
        val grantedPerms = distinctPermissions.filter { it.isGranted }
        val declaredOnlyPerms = distinctPermissions.filter { !it.isGranted }

        // 1. Calculate Category Contributions
        val categoryScores = mutableMapOf<PermissionCategory, Int>()
        val categoryMaxWeights = mutableMapOf<PermissionCategory, Int>()

        PermissionCategory.entries.forEach { cat ->
            categoryMaxWeights[cat] = cat.baseWeight
        }

        // Special Access (Category 1, max 30)
        var specialAccessPoints = 0.0
        specialAccesses.forEach { (type, status) ->
            val points = when (type) {
                SpecialAccessType.ACCESSIBILITY_SERVICE -> if (status == SpecialAccessStatus.ENABLED_OR_GRANTED) 18.0 else if (status == SpecialAccessStatus.DECLARED_ONLY) 5.0 else 0.0
                SpecialAccessType.DEVICE_ADMIN -> if (status == SpecialAccessStatus.ENABLED_OR_GRANTED) 18.0 else if (status == SpecialAccessStatus.DECLARED_ONLY) 5.0 else 0.0
                SpecialAccessType.NOTIFICATION_LISTENER -> if (status == SpecialAccessStatus.ENABLED_OR_GRANTED) 14.0 else if (status == SpecialAccessStatus.DECLARED_ONLY) 4.0 else 0.0
                SpecialAccessType.SYSTEM_ALERT_WINDOW -> if (status == SpecialAccessStatus.ENABLED_OR_GRANTED) 12.0 else if (status == SpecialAccessStatus.DECLARED_ONLY) 4.0 else 0.0
                SpecialAccessType.PACKAGE_INSTALL -> if (status == SpecialAccessStatus.ENABLED_OR_GRANTED) 8.0 else if (status == SpecialAccessStatus.DECLARED_ONLY) 2.0 else 0.0
                SpecialAccessType.USAGE_STATS -> if (status == SpecialAccessStatus.ENABLED_OR_GRANTED) 8.0 else if (status == SpecialAccessStatus.DECLARED_ONLY) 2.0 else 0.0
            }
            specialAccessPoints += points
        }
        categoryScores[PermissionCategory.SPECIAL_ACCESS] = specialAccessPoints.coerceAtMost(30.0).roundToInt()

        // SMS (Category 2, max 20)
        var smsPoints = 0.0
        distinctPermissions.filter { it.category == PermissionCategory.SMS }.forEach { perm ->
            val weight = when (perm.simpleName) {
                "READ_SMS" -> 10.0
                "RECEIVE_SMS" -> 8.0
                "SEND_SMS" -> 8.0
                else -> 4.0
            }
            smsPoints += if (perm.isGranted) weight else (weight * 0.25)
        }
        categoryScores[PermissionCategory.SMS] = smsPoints.coerceAtMost(20.0).roundToInt()

        // Contacts & Call Logs (Category 3, max 12)
        var contactPoints = 0.0
        distinctPermissions.filter { it.category == PermissionCategory.CONTACTS_CALL_LOGS }.forEach { perm ->
            val weight = when (perm.simpleName) {
                "READ_CALL_LOG" -> 6.0
                "READ_CONTACTS" -> 5.0
                "WRITE_CALL_LOG" -> 4.0
                "WRITE_CONTACTS" -> 3.0
                else -> 2.0
            }
            contactPoints += if (perm.isGranted) weight else (weight * 0.25)
        }
        categoryScores[PermissionCategory.CONTACTS_CALL_LOGS] = contactPoints.coerceAtMost(12.0).roundToInt()

        // Camera & Microphone (Category 4, max 10)
        var camMicPoints = 0.0
        distinctPermissions.filter { it.category == PermissionCategory.CAMERA_MICROPHONE }.forEach { perm ->
            val weight = when (perm.simpleName) {
                "CAMERA" -> 6.0
                "RECORD_AUDIO" -> 6.0
                else -> 3.0
            }
            camMicPoints += if (perm.isGranted) weight else (weight * 0.25)
        }
        categoryScores[PermissionCategory.CAMERA_MICROPHONE] = camMicPoints.coerceAtMost(10.0).roundToInt()

        // Location (Category 5, max 10)
        var locPoints = 0.0
        distinctPermissions.filter { it.category == PermissionCategory.LOCATION }.forEach { perm ->
            val weight = when (perm.locationPrecision) {
                LocationPrecision.BACKGROUND -> 7.0
                LocationPrecision.FINE -> 5.0
                LocationPrecision.COARSE -> 3.0
                else -> 2.0
            }
            locPoints += if (perm.isGranted) weight else (weight * 0.25)
        }
        categoryScores[PermissionCategory.LOCATION] = locPoints.coerceAtMost(10.0).roundToInt()

        // Phone & Identity (Category 6, max 8)
        var phonePoints = 0.0
        distinctPermissions.filter { it.category == PermissionCategory.PHONE_IDENTITY }.forEach { perm ->
            val weight = when (perm.simpleName) {
                "CALL_PHONE" -> 4.0
                "READ_PHONE_NUMBERS" -> 4.0
                "READ_PHONE_STATE" -> 3.0
                else -> 2.0
            }
            phonePoints += if (perm.isGranted) weight else (weight * 0.25)
        }
        categoryScores[PermissionCategory.PHONE_IDENTITY] = phonePoints.coerceAtMost(8.0).roundToInt()

        // Storage & Media (Category 7, max 8)
        var storagePoints = 0.0
        distinctPermissions.filter { it.category == PermissionCategory.STORAGE_MEDIA }.forEach { perm ->
            val weight = when (perm.simpleName) {
                "MANAGE_EXTERNAL_STORAGE" -> 8.0
                "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE" -> 4.0
                "READ_MEDIA_IMAGES", "READ_MEDIA_VIDEO" -> 3.0
                else -> 2.0
            }
            storagePoints += if (perm.isGranted) weight else (weight * 0.25)
        }
        categoryScores[PermissionCategory.STORAGE_MEDIA] = storagePoints.coerceAtMost(8.0).roundToInt()

        // Background & Persistence (Category 8, max 6)
        var bgPoints = 0.0
        distinctPermissions.filter { it.category == PermissionCategory.BACKGROUND_PERSISTENCE }.forEach { perm ->
            val weight = when (perm.simpleName) {
                "RECEIVE_BOOT_COMPLETED" -> 3.0
                "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" -> 3.0
                "FOREGROUND_SERVICE" -> 2.0
                else -> 1.0
            }
            bgPoints += if (perm.isGranted) weight else (weight * 0.25)
        }
        categoryScores[PermissionCategory.BACKGROUND_PERSISTENCE] = bgPoints.coerceAtMost(6.0).roundToInt()

        // Network (Category 9, max 2)
        var netPoints = 0.0
        distinctPermissions.filter { it.category == PermissionCategory.NETWORK }.forEach { perm ->
            val weight = if (perm.simpleName == "INTERNET") 2.0 else 1.0
            netPoints += if (perm.isGranted) weight else (weight * 0.25)
        }
        categoryScores[PermissionCategory.NETWORK] = netPoints.coerceAtMost(2.0).roundToInt()

        val rawBaseScore = categoryScores.values.sum()
        // Base weights total: 30 + 20 + 12 + 10 + 10 + 8 + 8 + 6 + 2 = 106. Normalize to 0..100
        val baseNormalized = ((rawBaseScore.toDouble() / 106.0) * 100.0).roundToInt().coerceIn(0, 100)

        // 2. Combination Rules & Bonuses
        val matchedRules = mutableListOf<Pair<String, Int>>()
        val findings = mutableListOf<RiskFinding>()

        val hasGrantedSms = grantedPerms.any { it.category == PermissionCategory.SMS }
        val hasInternet = distinctPermissions.any { it.simpleName == "INTERNET" && it.isGranted }
        val hasBoot = distinctPermissions.any { it.simpleName == "RECEIVE_BOOT_COMPLETED" }

        val accessibilityStatus = specialAccesses[SpecialAccessType.ACCESSIBILITY_SERVICE] ?: SpecialAccessStatus.NOT_REQUESTED
        val overlayStatus = specialAccesses[SpecialAccessType.SYSTEM_ALERT_WINDOW] ?: SpecialAccessStatus.NOT_REQUESTED
        val deviceAdminStatus = specialAccesses[SpecialAccessType.DEVICE_ADMIN] ?: SpecialAccessStatus.NOT_REQUESTED
        val notifListenerStatus = specialAccesses[SpecialAccessType.NOTIFICATION_LISTENER] ?: SpecialAccessStatus.NOT_REQUESTED

        // Rule 1: SMS + Internet + Boot Persistence (+15)
        if (hasGrantedSms && hasInternet && hasBoot) {
            matchedRules.add("SMS Access + Internet + Boot Persistence" to 15)
            findings.add(
                RiskFinding(
                    id = "rule_sms_net_boot",
                    title = "SMS Exfiltration & Persistence Vector",
                    capabilityOrCombination = "SMS Access + Internet Connectivity + Autostart on Boot",
                    evidence = "Granted SMS permissions paired with active INTERNET access and RECEIVE_BOOT_COMPLETED broadcast receiver.",
                    whyItMatters = "Allows the application to run automatically upon device startup, read incoming text messages/OTPs, and transmit data outward across the internet.",
                    recommendation = "Verify if this application has a legitimate operational need to inspect text messages and autostart in the background. Consider revoking SMS access in system settings.",
                    severity = RiskLevel.HIGH,
                    category = PermissionCategory.SMS
                )
            )
        }

        // Rule 2: Accessibility + Overlay (+18)
        val hasAccessibilityCap = accessibilityStatus != SpecialAccessStatus.NOT_REQUESTED
        val hasOverlayCap = overlayStatus != SpecialAccessStatus.NOT_REQUESTED
        if (hasAccessibilityCap && hasOverlayCap) {
            val isBothActive = accessibilityStatus == SpecialAccessStatus.ENABLED_OR_GRANTED && overlayStatus == SpecialAccessStatus.ENABLED_OR_GRANTED
            val bonus = if (isBothActive) 22 else 10
            matchedRules.add("Accessibility Capability + Overlay Access" to bonus)
            findings.add(
                RiskFinding(
                    id = "rule_access_overlay",
                    title = "Interface Cloaking & Tapjacking Potential",
                    capabilityOrCombination = "Accessibility Service + Display Over Other Apps (Overlay)",
                    evidence = "App declares or possesses Accessibility Service authority alongside System Alert Window overlay capability (Accessibility: ${accessibilityStatus.label}, Overlay: ${overlayStatus.label}).",
                    whyItMatters = "This combination allows displaying deceptive visual overlays on top of other screens while observing user keystrokes and injecting automated clicks.",
                    recommendation = "Check Accessibility settings in Android System Settings. Ensure this service was enabled intentionally by you, and revoke overlay permissions if not strictly required.",
                    severity = if (isBothActive) RiskLevel.CRITICAL else RiskLevel.HIGH,
                    category = PermissionCategory.SPECIAL_ACCESS
                )
            )
        }

        // Rule 3: Active Device Administrator (+20)
        if (deviceAdminStatus == SpecialAccessStatus.ENABLED_OR_GRANTED) {
            matchedRules.add("Active Device Administrator Privilege" to 20)
            findings.add(
                RiskFinding(
                    id = "rule_device_admin",
                    title = "Elevated Device Administrator Authority Active",
                    capabilityOrCombination = "Device Administrator Binding",
                    evidence = "This application is registered as an active Device Administrator in Android DevicePolicyManager.",
                    whyItMatters = "Device Administrators hold authority to set lock screen rules, enforce passwords, restrict camera usage, wipe storage, and prevent standard user uninstallation.",
                    recommendation = "Review Device Admin apps in System Security settings. Unless this is your designated MDM or enterprise security tool, deactivate Device Administrator access.",
                    severity = RiskLevel.CRITICAL,
                    category = PermissionCategory.SPECIAL_ACCESS
                )
            )
        }

        // Rule 4: Context Mismatch Heuristic (+12)
        val (hasMismatch, mismatchExplanation) = ContextMismatchHeuristics.evaluate(appLabel, grantedPerms)
        var contextWarning: String? = null
        if (hasMismatch && mismatchExplanation != null) {
            contextWarning = mismatchExplanation
            matchedRules.add("Application Context Mismatch" to 12)
            findings.add(
                RiskFinding(
                    id = "rule_context_mismatch",
                    title = "Disproportionate Permissions for App Purpose",
                    capabilityOrCombination = "Utility App Context Mismatch",
                    evidence = mismatchExplanation,
                    whyItMatters = "Simple utility applications (such as flashlights, calculators, or wallpapers) rarely require access to SMS, contacts, or location to provide their stated function.",
                    recommendation = "Audit the app's permissions in Android App Details and revoke any access that exceeds its advertised functional scope.",
                    severity = RiskLevel.HIGH,
                    category = PermissionCategory.CONTACTS_CALL_LOGS
                )
            )
        }

        // Rule 5: Sideloaded Installer with Sensitive Capabilities (+10)
        val isSideloaded = installerPackageName == null ||
            installerPackageName.contains("packageinstaller", ignoreCase = true) ||
            (!installerPackageName.contains("vending", ignoreCase = true) &&
                !installerPackageName.contains("amazon", ignoreCase = true) &&
                !installerPackageName.contains("huawei", ignoreCase = true))

        val sensitiveCategoriesGranted = grantedPerms.map { it.category }.distinct()
            .intersect(setOf(PermissionCategory.SMS, PermissionCategory.CONTACTS_CALL_LOGS, PermissionCategory.LOCATION, PermissionCategory.CAMERA_MICROPHONE))

        if (isSideloaded && sensitiveCategoriesGranted.size >= 2 && !isSystemApp) {
            matchedRules.add("Sideloaded Source with Multiple Sensitive Permissions" to 10)
            findings.add(
                RiskFinding(
                    id = "rule_sideload_sensitive",
                    title = "Sideloaded App with Privileged Capabilities",
                    capabilityOrCombination = "Unverified Installation Source + Sensitive Permissions",
                    evidence = "Installed via outside APK package installer (${installerPackageName ?: "unknown source"}) with granted access to ${sensitiveCategoriesGranted.joinToString { it.title }}.",
                    whyItMatters = "Sideloaded packages bypass Google Play Protect developer identity verification and app store review policies, increasing exposure to modified or trojanized software.",
                    recommendation = "Confirm that you downloaded this APK directly from an authentic, trusted vendor website.",
                    severity = RiskLevel.MEDIUM,
                    category = null
                )
            )
        }

        // Rule 6: Background Location + Other Sensitive Capabilities (+8)
        val hasBgLocation = grantedPerms.any { it.locationPrecision == LocationPrecision.BACKGROUND }
        val hasOtherData = grantedPerms.any {
            it.category == PermissionCategory.CAMERA_MICROPHONE ||
                it.category == PermissionCategory.CONTACTS_CALL_LOGS ||
                it.category == PermissionCategory.SMS
        }
        if (hasBgLocation && hasOtherData) {
            matchedRules.add("Persistent Background Location with Sensor/Personal Data" to 8)
            findings.add(
                RiskFinding(
                    id = "rule_bg_loc_data",
                    title = "Persistent Background Geolocation Tracking",
                    capabilityOrCombination = "ACCESS_BACKGROUND_LOCATION + Personal Data",
                    evidence = "Holds granted ACCESS_BACKGROUND_LOCATION alongside microphone, camera, contacts, or SMS capabilities.",
                    whyItMatters = "Allows the application to record physical geographic whereabouts continuously even when minimized, correlating movements with personal data.",
                    recommendation = "Adjust Location access to 'Allow only while using the app' in System App Info.",
                    severity = RiskLevel.HIGH,
                    category = PermissionCategory.LOCATION
                )
            )
        }

        // Rule 7: Notification Listener Access (+12)
        if (notifListenerStatus == SpecialAccessStatus.ENABLED_OR_GRANTED && (hasInternet || hasGrantedSms || grantedPerms.any { it.category == PermissionCategory.CONTACTS_CALL_LOGS })) {
            matchedRules.add("Active Notification Listener Access Pipeline" to 12)
            findings.add(
                RiskFinding(
                    id = "rule_notif_listener",
                    title = "Notification Interception Access Active",
                    capabilityOrCombination = "Notification Listener Service + Network/Telephony",
                    evidence = "Notification Listener special access is actively enabled for this package.",
                    whyItMatters = "Active Notification Listeners can inspect incoming notifications from WhatsApp, Signal, SMS, and banking apps, including private messages and OTP verification codes.",
                    recommendation = "Verify whether this app requires notification interaction (e.g. smartwatch companion). Disable in Special App Access > Device & App Notifications if unsure.",
                    severity = RiskLevel.HIGH,
                    category = PermissionCategory.SPECIAL_ACCESS
                )
            )
        }

        // 3. Category-level findings to ensure every HIGH / CRITICAL app has at least 3 distinct explanations
        if (hasGrantedSms && findings.none { it.category == PermissionCategory.SMS }) {
            val smsList = grantedPerms.filter { it.category == PermissionCategory.SMS }.joinToString { it.simpleName }
            findings.add(
                RiskFinding(
                    id = "cat_sms_access",
                    title = "SMS Message Inspection Access Granted",
                    capabilityOrCombination = "SMS Telephony Permissions",
                    evidence = "Holds granted permissions: $smsList.",
                    whyItMatters = "SMS access allows reading or receiving incoming text messages, which often carry bank transaction verifications and multi-factor authentication codes.",
                    recommendation = "Review if SMS permissions are necessary for this app. You can revoke them in Android System Settings.",
                    severity = RiskLevel.HIGH,
                    category = PermissionCategory.SMS
                )
            )
        }

        val contactsCallLogsGranted = grantedPerms.filter { it.category == PermissionCategory.CONTACTS_CALL_LOGS }
        if (contactsCallLogsGranted.isNotEmpty() && findings.none { it.category == PermissionCategory.CONTACTS_CALL_LOGS }) {
            val permList = contactsCallLogsGranted.joinToString { it.simpleName }
            findings.add(
                RiskFinding(
                    id = "cat_contacts_access",
                    title = "Address Book & Call History Access",
                    capabilityOrCombination = "Contacts / Call Log Inspection",
                    evidence = "Holds granted permissions: $permList.",
                    whyItMatters = "Provides programmatic access to your entire personal contact list and detailed phone call logs (numbers dialed, missed, timestamps, durations).",
                    recommendation = "Inspect whether this app legitimately requires access to your private phone contacts or call history.",
                    severity = RiskLevel.MEDIUM,
                    category = PermissionCategory.CONTACTS_CALL_LOGS
                )
            )
        }

        val camMicGranted = grantedPerms.filter { it.category == PermissionCategory.CAMERA_MICROPHONE }
        if (camMicGranted.isNotEmpty() && findings.none { it.category == PermissionCategory.CAMERA_MICROPHONE }) {
            val permList = camMicGranted.joinToString { it.simpleName }
            findings.add(
                RiskFinding(
                    id = "cat_cam_mic_access",
                    title = "Camera & Audio Hardware Access",
                    capabilityOrCombination = "Live Camera & Microphone Sensors",
                    evidence = "Holds granted permissions: $permList.",
                    whyItMatters = "Grants the application access to capture still photos, live video, and ambient microphone audio recordings.",
                    recommendation = "Ensure camera and microphone permissions are only granted to trusted recording, photography, or communication apps.",
                    severity = RiskLevel.MEDIUM,
                    category = PermissionCategory.CAMERA_MICROPHONE
                )
            )
        }

        val locationGranted = grantedPerms.filter { it.category == PermissionCategory.LOCATION }
        if (locationGranted.isNotEmpty() && findings.none { it.category == PermissionCategory.LOCATION }) {
            val permList = locationGranted.joinToString { it.simpleName }
            findings.add(
                RiskFinding(
                    id = "cat_location_access",
                    title = "Geographic Location Tracking",
                    capabilityOrCombination = "GPS and Network Location Sensors",
                    evidence = "Holds granted permissions: $permList.",
                    whyItMatters = "Enables calculating your real-world geographic coordinates, travel history, and frequently visited locations.",
                    recommendation = "Consider downgrading permission to 'Approximate Location' or 'While Using App' in Android Settings.",
                    severity = RiskLevel.MEDIUM,
                    category = PermissionCategory.LOCATION
                )
            )
        }

        // Overlay findings if not already covered
        if (overlayStatus == SpecialAccessStatus.ENABLED_OR_GRANTED && findings.none { it.id == "rule_access_overlay" }) {
            findings.add(
                RiskFinding(
                    id = "cat_overlay_active",
                    title = "Screen Overlay (Display Over Other Apps) Granted",
                    capabilityOrCombination = "SYSTEM_ALERT_WINDOW Special Access",
                    evidence = "App has granted authority to draw floating overlay windows on top of any active screen.",
                    whyItMatters = "Can be abused to render floating banners that intercept touches or spoof user interface elements of other applications.",
                    recommendation = "Verify in Special App Access > Display Over Other Apps. Revoke if floating windows are not actively needed.",
                    severity = RiskLevel.HIGH,
                    category = PermissionCategory.SPECIAL_ACCESS
                )
            )
        }

        // Baseline finding for Low Risk apps with minimal network access
        if (findings.isEmpty()) {
            if (hasInternet) {
                findings.add(
                    RiskFinding(
                        id = "cat_network_only",
                        title = "Standard Network Access",
                        capabilityOrCombination = "Standard Internet Capability",
                        evidence = "Application declares standard INTERNET access without sensitive sensor, messaging, or special access permissions.",
                        whyItMatters = "Standard web communication for synchronizing application content, downloading updates, or displaying web pages.",
                        recommendation = "No adverse action needed. The app operates within standard non-privileged boundaries.",
                        severity = RiskLevel.LOW,
                        category = PermissionCategory.NETWORK
                    )
                )
            } else {
                findings.add(
                    RiskFinding(
                        id = "cat_isolated_app",
                        title = "Minimal Sandbox Footprint",
                        capabilityOrCombination = "Fully Sandboxed App",
                        evidence = "No sensitive permissions, special access, or background persistence capabilities detected.",
                        whyItMatters = "This application runs within Android's standard isolated security sandbox without elevated privileges.",
                        recommendation = "No security concerns identified.",
                        severity = RiskLevel.LOW,
                        category = null
                    )
                )
            }
        }

        // 4. Calculate Final Score
        val totalCombinationBonus = matchedRules.sumOf { it.second }
        val combinedScore = (baseNormalized + totalCombinationBonus).coerceIn(0, 100)
        val finalRiskLevel = RiskLevel.fromScore(combinedScore)

        val breakdown = RiskScoreBreakdown(
            categoryScores = categoryScores,
            categoryMaxWeights = categoryMaxWeights,
            matchedRules = matchedRules,
            combinationBonus = totalCombinationBonus,
            rawBaseScore = rawBaseScore,
            finalScore = combinedScore
        )

        return EvaluationResult(
            finalScore = combinedScore,
            riskLevel = finalRiskLevel,
            scoreBreakdown = breakdown,
            findings = findings,
            contextMismatchWarning = contextWarning
        )
    }
}
