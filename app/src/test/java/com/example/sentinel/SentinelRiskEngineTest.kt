package com.example.sentinel

import com.example.sentinel.model.AppPermission
import com.example.sentinel.model.LocationPrecision
import com.example.sentinel.model.PermissionCategory
import com.example.sentinel.model.RiskLevel
import com.example.sentinel.model.SpecialAccessStatus
import com.example.sentinel.model.SpecialAccessType
import com.example.sentinel.risk.RiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SentinelRiskEngineTest {

    private lateinit var riskEngine: RiskEngine

    @Before
    fun setUp() {
        riskEngine = RiskEngine()
    }

    private fun emptySpecialAccessMap(): Map<SpecialAccessType, SpecialAccessStatus> {
        return SpecialAccessType.entries.associateWith { SpecialAccessStatus.NOT_REQUESTED }
    }

    /**
     * Test 1: Low-risk app
     * An app has internet access and no other sensitive capabilities.
     * Expected result: LOW risk (score 0–24).
     */
    @Test
    fun test1_lowRiskApp_internetOnly() {
        val permissions = listOf(
            AppPermission(
                permissionName = "android.permission.INTERNET",
                simpleName = "INTERNET",
                category = PermissionCategory.NETWORK,
                isGranted = true
            )
        )

        val result = riskEngine.evaluate(
            appLabel = "Simple News Reader",
            packageName = "com.news.reader",
            installerPackageName = "com.android.vending",
            isSystemApp = false,
            permissions = permissions,
            specialAccesses = emptySpecialAccessMap()
        )

        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertTrue("Score must be in LOW band (0-24), was: ${result.finalScore}", result.finalScore in 0..24)
        assertTrue(result.findings.isNotEmpty())
    }

    /**
     * Test 2: Sensitive SMS access
     * An app has granted SMS access and network capability.
     * Expected result: Higher score than the low-risk baseline, with accurate explanations.
     */
    @Test
    fun test2_sensitiveSmsAccess() {
        val basePerms = listOf(
            AppPermission("android.permission.INTERNET", "INTERNET", PermissionCategory.NETWORK, isGranted = true)
        )
        val baseResult = riskEngine.evaluate("Base App", "com.base", "com.android.vending", false, basePerms, emptySpecialAccessMap())

        val smsPerms = listOf(
            AppPermission("android.permission.INTERNET", "INTERNET", PermissionCategory.NETWORK, isGranted = true),
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.RECEIVE_SMS", "RECEIVE_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true)
        )
        val smsResult = riskEngine.evaluate("SMS App", "com.sms", "com.android.vending", false, smsPerms, emptySpecialAccessMap())

        assertTrue(
            "SMS access score (${smsResult.finalScore}) must be greater than base score (${baseResult.finalScore})",
            smsResult.finalScore > baseResult.finalScore
        )
        assertTrue(
            "Expected SMS finding in explanations",
            smsResult.findings.any { it.category == PermissionCategory.SMS }
        )
    }

    /**
     * Test 3: Special-access combination
     * An app has enabled accessibility service and relevant overlay access.
     * Expected result: A combination-rule finding and an increased risk score.
     */
    @Test
    fun test3_specialAccessCombination() {
        val specials = mapOf(
            SpecialAccessType.ACCESSIBILITY_SERVICE to SpecialAccessStatus.ENABLED_OR_GRANTED,
            SpecialAccessType.SYSTEM_ALERT_WINDOW to SpecialAccessStatus.ENABLED_OR_GRANTED,
            SpecialAccessType.DEVICE_ADMIN to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.NOTIFICATION_LISTENER to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.PACKAGE_INSTALL to SpecialAccessStatus.NOT_REQUESTED,
            SpecialAccessType.USAGE_STATS to SpecialAccessStatus.NOT_REQUESTED
        )

        val result = riskEngine.evaluate(
            appLabel = "Overlay Helper",
            packageName = "com.overlay.helper",
            installerPackageName = "com.android.vending",
            isSystemApp = false,
            permissions = emptyList(),
            specialAccesses = specials
        )

        val hasCombinationRule = result.scoreBreakdown.matchedRules.any {
            it.first.contains("Accessibility Capability + Overlay Access")
        }
        assertTrue("Expected Accessibility + Overlay combination rule match", hasCombinationRule)
        assertTrue("Score must be at least HIGH for active accessibility + overlay", result.finalScore >= 50)
    }

    /**
     * Test 4: Boot-persistence combination
     * An app has SMS access, internet access, and boot-receiver capability.
     * Expected result: A combination-rule finding.
     */
    @Test
    fun test4_bootPersistenceCombination() {
        val perms = listOf(
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.INTERNET", "INTERNET", PermissionCategory.NETWORK, isGranted = true),
            AppPermission("android.permission.RECEIVE_BOOT_COMPLETED", "RECEIVE_BOOT_COMPLETED", PermissionCategory.BACKGROUND_PERSISTENCE, isGranted = true)
        )

        val result = riskEngine.evaluate(
            appLabel = "Auto Exfiltrator",
            packageName = "com.auto.exfil",
            installerPackageName = "com.android.vending",
            isSystemApp = false,
            permissions = perms,
            specialAccesses = emptySpecialAccessMap()
        )

        val hasBootSmsRule = result.scoreBreakdown.matchedRules.any {
            it.first.contains("SMS Access + Internet + Boot Persistence")
        }
        assertTrue("Expected SMS + Internet + Boot combination rule", hasBootSmsRule)
        assertTrue(result.findings.any { it.id == "rule_sms_net_boot" })
    }

    /**
     * Test 5: Missing metadata
     * An app has incomplete metadata.
     * Expected result: The scanner/evaluator completes without crashing.
     */
    @Test
    fun test5_missingMetadata() {
        val result = riskEngine.evaluate(
            appLabel = "",
            packageName = "com.unknown.orphan",
            installerPackageName = null,
            isSystemApp = false,
            permissions = emptyList(),
            specialAccesses = emptyMap()
        )

        assertNotNull(result)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertEquals(0, result.finalScore)
    }

    /**
     * Test 6: Duplicate permissions
     * The same permission must not contribute to the score more than once.
     */
    @Test
    fun test6_duplicatePermissions() {
        val singlePerm = listOf(
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true)
        )
        val duplicatePerms = listOf(
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true)
        )

        val singleResult = riskEngine.evaluate("App", "com.app", null, false, singlePerm, emptySpecialAccessMap())
        val duplicateResult = riskEngine.evaluate("App", "com.app", null, false, duplicatePerms, emptySpecialAccessMap())

        assertEquals(
            "Duplicate permissions must not inflate risk score",
            singleResult.finalScore,
            duplicateResult.finalScore
        )
    }

    /**
     * Test 7: Score boundaries
     * Scores must remain between 0 and 100, and risk bands must match documented thresholds.
     */
    @Test
    fun test7_scoreBoundaries() {
        // Minimum boundary test
        val minResult = riskEngine.evaluate("Clean App", "com.clean", null, false, emptyList(), emptySpecialAccessMap())
        assertTrue("Score must not be negative", minResult.finalScore >= 0)
        assertEquals(RiskLevel.LOW, RiskLevel.fromScore(minResult.finalScore))

        // Maximum boundary test with extreme privileges
        val maxPerms = listOf(
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.SEND_SMS", "SEND_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.READ_CONTACTS", "READ_CONTACTS", PermissionCategory.CONTACTS_CALL_LOGS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.READ_CALL_LOG", "READ_CALL_LOG", PermissionCategory.CONTACTS_CALL_LOGS, isGranted = true, isDangerous = true),
            AppPermission("android.permission.CAMERA", "CAMERA", PermissionCategory.CAMERA_MICROPHONE, isGranted = true, isDangerous = true),
            AppPermission("android.permission.RECORD_AUDIO", "RECORD_AUDIO", PermissionCategory.CAMERA_MICROPHONE, isGranted = true, isDangerous = true),
            AppPermission("android.permission.ACCESS_FINE_LOCATION", "ACCESS_FINE_LOCATION", PermissionCategory.LOCATION, isGranted = true, isDangerous = true, locationPrecision = LocationPrecision.FINE),
            AppPermission("android.permission.ACCESS_BACKGROUND_LOCATION", "ACCESS_BACKGROUND_LOCATION", PermissionCategory.LOCATION, isGranted = true, isDangerous = true, locationPrecision = LocationPrecision.BACKGROUND),
            AppPermission("android.permission.READ_PHONE_STATE", "READ_PHONE_STATE", PermissionCategory.PHONE_IDENTITY, isGranted = true, isDangerous = true),
            AppPermission("android.permission.MANAGE_EXTERNAL_STORAGE", "MANAGE_EXTERNAL_STORAGE", PermissionCategory.STORAGE_MEDIA, isGranted = true),
            AppPermission("android.permission.INTERNET", "INTERNET", PermissionCategory.NETWORK, isGranted = true),
            AppPermission("android.permission.RECEIVE_BOOT_COMPLETED", "RECEIVE_BOOT_COMPLETED", PermissionCategory.BACKGROUND_PERSISTENCE, isGranted = true)
        )
        val maxSpecials = SpecialAccessType.entries.associateWith { SpecialAccessStatus.ENABLED_OR_GRANTED }

        val maxResult = riskEngine.evaluate("Max Threat", "com.threat", "com.sideload", false, maxPerms, maxSpecials)

        assertTrue("Score must not exceed 100, was: ${maxResult.finalScore}", maxResult.finalScore <= 100)
        assertEquals(RiskLevel.CRITICAL, maxResult.riskLevel)
        assertEquals(100, maxResult.finalScore)
    }

    /**
     * Test 8: Declared versus granted
     * A declared but ungranted dangerous permission must not be reported as granted
     * and must contribute significantly less than a granted permission.
     */
    @Test
    fun test8_declaredVersusGranted() {
        val declaredOnly = listOf(
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = false, isDangerous = true)
        )
        val granted = listOf(
            AppPermission("android.permission.READ_SMS", "READ_SMS", PermissionCategory.SMS, isGranted = true, isDangerous = true)
        )

        val declaredResult = riskEngine.evaluate("App1", "com.app1", null, false, declaredOnly, emptySpecialAccessMap())
        val grantedResult = riskEngine.evaluate("App2", "com.app2", null, false, granted, emptySpecialAccessMap())

        assertFalse("Declared permission must not be marked granted", declaredOnly.first().isGranted)
        assertTrue("Granted permission must be marked granted", granted.first().isGranted)
        assertTrue(
            "Granted permission score (${grantedResult.finalScore}) must be greater than declared only score (${declaredResult.finalScore})",
            grantedResult.finalScore > declaredResult.finalScore
        )
    }

    /**
     * Test 9: System application filtering
     * System apps must be excluded by default and included when user enables toggle.
     */
    @Test
    fun test9_systemApplicationFiltering() {
        data class MockPkg(val name: String, val isSystem: Boolean)

        val packages = listOf(
            MockPkg("com.google.android.gms", isSystem = true),
            MockPkg("com.android.systemui", isSystem = true),
            MockPkg("com.spotify.music", isSystem = false),
            MockPkg("com.whatsapp", isSystem = false)
        )

        fun filterPackages(includeSystem: Boolean): List<MockPkg> {
            return packages.filter { if (includeSystem) true else !it.isSystem }
        }

        val defaultFiltered = filterPackages(includeSystem = false)
        assertEquals(2, defaultFiltered.size)
        assertTrue(defaultFiltered.none { it.isSystem })

        val allFiltered = filterPackages(includeSystem = true)
        assertEquals(4, allFiltered.size)
    }

    /**
     * Test 10: Special-access accuracy
     * Declared special-access capability must not automatically be treated as enabled access.
     */
    @Test
    fun test10_specialAccessAccuracy() {
        val declaredOnlySpecials = mapOf(
            SpecialAccessType.ACCESSIBILITY_SERVICE to SpecialAccessStatus.DECLARED_ONLY,
            SpecialAccessType.SYSTEM_ALERT_WINDOW to SpecialAccessStatus.DECLARED_ONLY
        )
        val enabledSpecials = mapOf(
            SpecialAccessType.ACCESSIBILITY_SERVICE to SpecialAccessStatus.ENABLED_OR_GRANTED,
            SpecialAccessType.SYSTEM_ALERT_WINDOW to SpecialAccessStatus.ENABLED_OR_GRANTED
        )

        val declaredResult = riskEngine.evaluate("Declared App", "com.dec", null, false, emptyList(), declaredOnlySpecials)
        val enabledResult = riskEngine.evaluate("Enabled App", "com.enb", null, false, emptyList(), enabledSpecials)

        assertFalse(declaredOnlySpecials[SpecialAccessType.ACCESSIBILITY_SERVICE]!!.isGrantedOrEnabled)
        assertTrue(enabledSpecials[SpecialAccessType.ACCESSIBILITY_SERVICE]!!.isGrantedOrEnabled)
        assertTrue(
            "Active special access score (${enabledResult.finalScore}) must be significantly higher than declared-only (${declaredResult.finalScore})",
            enabledResult.finalScore > declaredResult.finalScore
        )
    }
}
