package com.example.sentinel.risk

import com.example.sentinel.model.AppPermission
import com.example.sentinel.model.PermissionCategory

object ContextMismatchHeuristics {

    data class AppContextRule(
        val categoryKeywords: List<String>,
        val unexpectedCategories: Set<PermissionCategory>,
        val rationale: String
    )

    private val RULES = listOf(
        AppContextRule(
            categoryKeywords = listOf("flashlight", "torch"),
            unexpectedCategories = setOf(
                PermissionCategory.SMS,
                PermissionCategory.CONTACTS_CALL_LOGS,
                PermissionCategory.LOCATION,
                PermissionCategory.CAMERA_MICROPHONE
            ),
            rationale = "Flashlight utilities only require camera flash hardware control. SMS, contacts, call logs, or GPS location are outside standard illumination needs."
        ),
        AppContextRule(
            categoryKeywords = listOf("calculator", "converter", "unit"),
            unexpectedCategories = setOf(
                PermissionCategory.SMS,
                PermissionCategory.CONTACTS_CALL_LOGS,
                PermissionCategory.LOCATION,
                PermissionCategory.CAMERA_MICROPHONE
            ),
            rationale = "Mathematical calculators process local arithmetic. Access to text messages, contacts, location, or microphone is unusual for a calculation tool."
        ),
        AppContextRule(
            categoryKeywords = listOf("wallpaper", "theme", "icon pack", "launcher background"),
            unexpectedCategories = setOf(
                PermissionCategory.LOCATION,
                PermissionCategory.SMS,
                PermissionCategory.CONTACTS_CALL_LOGS,
                PermissionCategory.CAMERA_MICROPHONE
            ),
            rationale = "Wallpaper applications display static or live images. Continuous background GPS location or messaging access is disproportionate for aesthetic personalization."
        ),
        AppContextRule(
            categoryKeywords = listOf("compass", "level", "ruler", "speedometer"),
            unexpectedCategories = setOf(
                PermissionCategory.SMS,
                PermissionCategory.CONTACTS_CALL_LOGS
            ),
            rationale = "Measurement sensors rely on internal magnetometers and accelerometers. SMS message access and phone call logs are unrelated to physical orientation."
        ),
        AppContextRule(
            categoryKeywords = listOf("notepad", "memo", "simple notes", "sticky note"),
            unexpectedCategories = setOf(
                PermissionCategory.SMS,
                PermissionCategory.LOCATION
            ),
            rationale = "Simple offline notepads record user typed text. Live background geographic tracking and cellular SMS interception are unexpected for note storage."
        )
    )

    fun evaluate(
        appLabel: String,
        grantedPermissions: List<AppPermission>
    ): Pair<Boolean, String?> {
        val labelLower = appLabel.lowercase()

        for (rule in RULES) {
            val matchesKeyword = rule.categoryKeywords.any { keyword ->
                labelLower.contains(keyword)
            }
            if (matchesKeyword) {
                val offending = grantedPermissions.filter { it.category in rule.unexpectedCategories }
                if (offending.isNotEmpty()) {
                    val catNames = offending.map { it.category.title }.distinct().joinToString(", ")
                    val explanation = "Context Mismatch Warning: This application appears to be a '${rule.categoryKeywords.first()}', but holds granted permissions in sensitive categories ($catNames). ${rule.rationale}"
                    return true to explanation
                }
            }
        }
        return false to null
    }
}
