package com.example.sentinel.scanner

import com.example.sentinel.model.AppPermission
import com.example.sentinel.model.LocationPrecision
import com.example.sentinel.model.PermissionCategory

object PermissionMapper {

    data class PermissionMetadata(
        val category: PermissionCategory,
        val isDangerous: Boolean,
        val description: String,
        val securityImplication: String,
        val locationPrecision: LocationPrecision = LocationPrecision.NONE
    )

    private val PERMISSION_REGISTRY = mapOf(
        // Category 2: SMS
        "android.permission.READ_SMS" to PermissionMetadata(
            PermissionCategory.SMS,
            isDangerous = true,
            description = "Allows the application to read all SMS messages stored on the device or SIM card.",
            securityImplication = "Exposes confidential communications, one-time passwords (OTP), 2FA authentication tokens, and banking alerts."
        ),
        "android.permission.RECEIVE_SMS" to PermissionMetadata(
            PermissionCategory.SMS,
            isDangerous = true,
            description = "Allows the application to intercept and process incoming SMS text messages as they arrive.",
            securityImplication = "Could silently capture real-time 2FA codes or transaction authorizations before the user sees them."
        ),
        "android.permission.SEND_SMS" to PermissionMetadata(
            PermissionCategory.SMS,
            isDangerous = true,
            description = "Allows the application to dispatch outgoing SMS messages without user interaction.",
            securityImplication = "Could incur carrier toll fees (premium rate SMS) or participate in smishing relay operations."
        ),
        "android.permission.RECEIVE_MMS" to PermissionMetadata(
            PermissionCategory.SMS,
            isDangerous = true,
            description = "Allows the application to monitor incoming multimedia messages (MMS).",
            securityImplication = "Exposes multimedia content and carrier network signaling data."
        ),
        "android.permission.RECEIVE_WAP_PUSH" to PermissionMetadata(
            PermissionCategory.SMS,
            isDangerous = true,
            description = "Allows the application to monitor incoming WAP push messages.",
            securityImplication = "Historical protocol susceptible to carrier URL injection and signaling abuse."
        ),

        // Category 3: Contacts and Call Logs
        "android.permission.READ_CONTACTS" to PermissionMetadata(
            PermissionCategory.CONTACTS_CALL_LOGS,
            isDangerous = true,
            description = "Allows the application to read all saved contact profiles, names, numbers, and emails.",
            securityImplication = "Enables social-graph harvesting and unauthorized contact data scraping."
        ),
        "android.permission.WRITE_CONTACTS" to PermissionMetadata(
            PermissionCategory.CONTACTS_CALL_LOGS,
            isDangerous = true,
            description = "Allows the application to modify or delete contacts in the user's address book.",
            securityImplication = "Can manipulate or inject phone numbers and contact details."
        ),
        "android.permission.GET_ACCOUNTS" to PermissionMetadata(
            PermissionCategory.CONTACTS_CALL_LOGS,
            isDangerous = true,
            description = "Allows access to the list of synchronized accounts in the Account Service.",
            securityImplication = "Reveals user primary email addresses and active cloud account handles."
        ),
        "android.permission.READ_CALL_LOG" to PermissionMetadata(
            PermissionCategory.CONTACTS_CALL_LOGS,
            isDangerous = true,
            description = "Allows the application to read the full phone call history and missed calls.",
            securityImplication = "Exposes who you communicate with, call frequencies, durations, and timestamps."
        ),
        "android.permission.WRITE_CALL_LOG" to PermissionMetadata(
            PermissionCategory.CONTACTS_CALL_LOGS,
            isDangerous = true,
            description = "Allows the application to modify the system call log.",
            securityImplication = "Could erase evidence of outgoing stealth calls or manipulate call records."
        ),

        // Category 4: Camera and Microphone
        "android.permission.CAMERA" to PermissionMetadata(
            PermissionCategory.CAMERA_MICROPHONE,
            isDangerous = true,
            description = "Grants direct hardware access to capture still photos and live video streams.",
            securityImplication = "Could capture visual imagery of user surroundings, documents, or facial biometrics."
        ),
        "android.permission.RECORD_AUDIO" to PermissionMetadata(
            PermissionCategory.CAMERA_MICROPHONE,
            isDangerous = true,
            description = "Grants direct hardware access to the device microphone to capture ambient audio.",
            securityImplication = "Could eavesdrop on private voice conversations, meetings, or surrounding acoustic signatures."
        ),

        // Category 5: Location
        "android.permission.ACCESS_FINE_LOCATION" to PermissionMetadata(
            PermissionCategory.LOCATION,
            isDangerous = true,
            description = "Accesses precise geographic coordinates using satellite GPS and Wi-Fi/cellular triangulation.",
            securityImplication = "Provides meter-level real-time physical tracking of the user.",
            locationPrecision = LocationPrecision.FINE
        ),
        "android.permission.ACCESS_COARSE_LOCATION" to PermissionMetadata(
            PermissionCategory.LOCATION,
            isDangerous = true,
            description = "Accesses approximate geographic area based on cell towers and Wi-Fi networks.",
            securityImplication = "Determines the user's neighborhood, city, and general travel movements.",
            locationPrecision = LocationPrecision.COARSE
        ),
        "android.permission.ACCESS_BACKGROUND_LOCATION" to PermissionMetadata(
            PermissionCategory.LOCATION,
            isDangerous = true,
            description = "Allows access to device geographic location even when the app is minimized or closed.",
            securityImplication = "Facilitates persistent 24/7 geofencing and comprehensive physical lifestyle profiling.",
            locationPrecision = LocationPrecision.BACKGROUND
        ),
        "android.permission.ACCESS_MEDIA_LOCATION" to PermissionMetadata(
            PermissionCategory.LOCATION,
            isDangerous = true,
            description = "Allows reading geographic location tags (EXIF GPS) embedded inside photo files.",
            securityImplication = "Reveals where past photos were snapped without requiring live GPS."
        ),

        // Category 6: Phone and Identity
        "android.permission.READ_PHONE_STATE" to PermissionMetadata(
            PermissionCategory.PHONE_IDENTITY,
            isDangerous = true,
            description = "Allows access to cellular network state, carrier info, and current ongoing call status.",
            securityImplication = "Historical identifier vector (carrier info, SIM state) and telephony activity detection."
        ),
        "android.permission.READ_PHONE_NUMBERS" to PermissionMetadata(
            PermissionCategory.PHONE_IDENTITY,
            isDangerous = true,
            description = "Allows reading the device's assigned cellular phone number.",
            securityImplication = "Exposes personal mobile number without explicit manual entry."
        ),
        "android.permission.CALL_PHONE" to PermissionMetadata(
            PermissionCategory.PHONE_IDENTITY,
            isDangerous = true,
            description = "Allows initiating a phone call directly without going through the system dialer UI.",
            securityImplication = "Could dial toll numbers or establish covert outbound voice calls."
        ),
        "android.permission.ANSWER_PHONE_CALLS" to PermissionMetadata(
            PermissionCategory.PHONE_IDENTITY,
            isDangerous = true,
            description = "Allows answering incoming phone calls programmatically.",
            securityImplication = "Can manipulate phone call flow without physical user interaction."
        ),

        // Category 7: Storage and Media
        "android.permission.READ_EXTERNAL_STORAGE" to PermissionMetadata(
            PermissionCategory.STORAGE_MEDIA,
            isDangerous = true,
            description = "Allows reading files stored on shared device storage.",
            securityImplication = "Could access downloaded PDFs, documents, backups, and user media."
        ),
        "android.permission.WRITE_EXTERNAL_STORAGE" to PermissionMetadata(
            PermissionCategory.STORAGE_MEDIA,
            isDangerous = true,
            description = "Allows modifying or creating files on shared device storage.",
            securityImplication = "Could alter or corrupt shared documents and stored app data."
        ),
        "android.permission.MANAGE_EXTERNAL_STORAGE" to PermissionMetadata(
            PermissionCategory.STORAGE_MEDIA,
            isDangerous = false, // Signature/AppOps special access
            description = "All-Files Access: complete read and write access across all shared storage folders.",
            securityImplication = "Broadest file access on Android; can inspect almost all non-sandboxed user files."
        ),
        "android.permission.READ_MEDIA_IMAGES" to PermissionMetadata(
            PermissionCategory.STORAGE_MEDIA,
            isDangerous = true,
            description = "Allows reading photo and image files from shared media collections.",
            securityImplication = "Exposes personal photos, screenshots, and visual records."
        ),
        "android.permission.READ_MEDIA_VIDEO" to PermissionMetadata(
            PermissionCategory.STORAGE_MEDIA,
            isDangerous = true,
            description = "Allows reading video recordings from shared media collections.",
            securityImplication = "Exposes personal videos and recorded recordings."
        ),
        "android.permission.READ_MEDIA_AUDIO" to PermissionMetadata(
            PermissionCategory.STORAGE_MEDIA,
            isDangerous = true,
            description = "Allows reading music and voice recordings from shared audio collections.",
            securityImplication = "Exposes stored songs, podcasts, and recorded voice memos."
        ),

        // Category 8: Background and Persistence
        "android.permission.RECEIVE_BOOT_COMPLETED" to PermissionMetadata(
            PermissionCategory.BACKGROUND_PERSISTENCE,
            isDangerous = false, // Normal
            description = "Allows the application to start automatically as soon as the device finishes booting.",
            securityImplication = "Enables automatic persistence; the app runs services without being manually launched."
        ),
        "android.permission.FOREGROUND_SERVICE" to PermissionMetadata(
            PermissionCategory.BACKGROUND_PERSISTENCE,
            isDangerous = false,
            description = "Allows running foreground services with ongoing system notifications.",
            securityImplication = "Enables long-running continuous background execution."
        ),
        "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" to PermissionMetadata(
            PermissionCategory.BACKGROUND_PERSISTENCE,
            isDangerous = false,
            description = "Requests exemption from Android Doze mode and App Standby power management.",
            securityImplication = "Allows app processes to remain awake and active around the clock."
        ),
        "android.permission.WAKE_LOCK" to PermissionMetadata(
            PermissionCategory.BACKGROUND_PERSISTENCE,
            isDangerous = false,
            description = "Prevents the device processor from sleeping or the screen from dimming.",
            securityImplication = "Keeps CPU active in background; may accelerate battery drain."
        ),

        // Category 9: Network
        "android.permission.INTERNET" to PermissionMetadata(
            PermissionCategory.NETWORK,
            isDangerous = false, // Normal install-time
            description = "Allows creating network sockets and communicating across the internet.",
            securityImplication = "Standard capability for web services; becomes high-risk when combined with sensitive data access."
        ),
        "android.permission.ACCESS_NETWORK_STATE" to PermissionMetadata(
            PermissionCategory.NETWORK,
            isDangerous = false,
            description = "Allows querying network connection state (Wi-Fi, cellular, disconnected).",
            securityImplication = "Enables monitoring active connectivity and network transitions."
        ),
        "android.permission.ACCESS_WIFI_STATE" to PermissionMetadata(
            PermissionCategory.NETWORK,
            isDangerous = false,
            description = "Allows querying Wi-Fi network state and connected SSID info.",
            securityImplication = "Wi-Fi BSSID/SSID can be correlated with geographical locations."
        ),

        // Category 1: Special Access declarations in manifest
        "android.permission.SYSTEM_ALERT_WINDOW" to PermissionMetadata(
            PermissionCategory.SPECIAL_ACCESS,
            isDangerous = false,
            description = "Allows displaying overlay windows over other applications.",
            securityImplication = "Can draw misleading overlays or floating transparent click-capturing surfaces."
        ),
        "android.permission.REQUEST_INSTALL_PACKAGES" to PermissionMetadata(
            PermissionCategory.SPECIAL_ACCESS,
            isDangerous = false,
            description = "Allows requesting the installation of APK packages.",
            securityImplication = "Can prompt user to install secondary payloads or rogue updates."
        ),
        "android.permission.PACKAGE_USAGE_STATS" to PermissionMetadata(
            PermissionCategory.SPECIAL_ACCESS,
            isDangerous = false,
            description = "Allows querying component usage statistics across all installed apps.",
            securityImplication = "Monitors user behavior and which apps are being launched in real-time."
        ),
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to PermissionMetadata(
            PermissionCategory.SPECIAL_ACCESS,
            isDangerous = false,
            description = "Declares an Accessibility Service component requiring user authorization in system settings.",
            securityImplication = "Provides full window interaction, text inspection, and touch injection when enabled."
        ),
        "android.permission.BIND_DEVICE_ADMIN" to PermissionMetadata(
            PermissionCategory.SPECIAL_ACCESS,
            isDangerous = false,
            description = "Declares a Device Administrator receiver requiring user activation in security settings.",
            securityImplication = "Grants hardware policy management, lock screen overrides, or data wipe capabilities."
        ),
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" to PermissionMetadata(
            PermissionCategory.SPECIAL_ACCESS,
            isDangerous = false,
            description = "Declares a Notification Listener service requiring authorization in system settings.",
            securityImplication = "Can read, intercept, and dismiss notifications from all apps on the device."
        )
    )

    fun mapPermission(
        permissionName: String,
        isGranted: Boolean,
        protectionLevelString: String = "NORMAL"
    ): AppPermission {
        val simple = permissionName.substringAfterLast('.')
        val meta = PERMISSION_REGISTRY[permissionName]

        return if (meta != null) {
            AppPermission(
                permissionName = permissionName,
                simpleName = simple,
                category = meta.category,
                isGranted = isGranted,
                isDangerous = meta.isDangerous,
                protectionLevel = if (meta.isDangerous) "DANGEROUS" else protectionLevelString,
                description = meta.description,
                securityImplication = meta.securityImplication,
                locationPrecision = meta.locationPrecision
            )
        } else {
            // Heuristic categorization for vendor/custom permissions
            val category = when {
                permissionName.contains("SMS", ignoreCase = true) -> PermissionCategory.SMS
                permissionName.contains("CONTACT", ignoreCase = true) ||
                    permissionName.contains("CALL", ignoreCase = true) -> PermissionCategory.CONTACTS_CALL_LOGS
                permissionName.contains("CAMERA", ignoreCase = true) ||
                    permissionName.contains("AUDIO", ignoreCase = true) ||
                    permissionName.contains("RECORD", ignoreCase = true) -> PermissionCategory.CAMERA_MICROPHONE
                permissionName.contains("LOCATION", ignoreCase = true) ||
                    permissionName.contains("GPS", ignoreCase = true) -> PermissionCategory.LOCATION
                permissionName.contains("PHONE", ignoreCase = true) ||
                    permissionName.contains("TELEPHONY", ignoreCase = true) -> PermissionCategory.PHONE_IDENTITY
                permissionName.contains("STORAGE", ignoreCase = true) ||
                    permissionName.contains("MEDIA", ignoreCase = true) -> PermissionCategory.STORAGE_MEDIA
                permissionName.contains("BOOT", ignoreCase = true) ||
                    permissionName.contains("SERVICE", ignoreCase = true) ||
                    permissionName.contains("WAKE", ignoreCase = true) -> PermissionCategory.BACKGROUND_PERSISTENCE
                permissionName.contains("INTERNET", ignoreCase = true) ||
                    permissionName.contains("NETWORK", ignoreCase = true) -> PermissionCategory.NETWORK
                else -> PermissionCategory.NETWORK
            }

            AppPermission(
                permissionName = permissionName,
                simpleName = simple,
                category = category,
                isGranted = isGranted,
                isDangerous = protectionLevelString.contains("DANGEROUS", ignoreCase = true),
                protectionLevel = protectionLevelString,
                description = "System or application declared permission: $simple",
                securityImplication = "Custom or vendor-specific capability: review if this app requires this access."
            )
        }
    }
}
