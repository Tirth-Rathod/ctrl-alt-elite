package com.example.sentinel.model

enum class PermissionCategory(
    val title: String,
    val baseWeight: Int,
    val description: String,
    val iconKey: String
) {
    SPECIAL_ACCESS(
        title = "Special Access",
        baseWeight = 30,
        description = "Elevated system privileges such as Accessibility, Device Admin, Screen Overlay, and Notification Listener.",
        iconKey = "security"
    ),
    SMS(
        title = "SMS & Messaging",
        baseWeight = 20,
        description = "Read, receive, or send SMS text messages, including two-factor verification codes (OTP).",
        iconKey = "sms"
    ),
    CONTACTS_CALL_LOGS(
        title = "Contacts & Call Logs",
        baseWeight = 12,
        description = "Access to the user's address book, contact profiles, call history, and telephony records.",
        iconKey = "contacts"
    ),
    CAMERA_MICROPHONE(
        title = "Camera & Audio",
        baseWeight = 10,
        description = "Direct hardware capture of live video imagery and ambient room microphone audio.",
        iconKey = "mic"
    ),
    LOCATION(
        title = "Location Tracking",
        baseWeight = 10,
        description = "Precise GPS coordinates, coarse cell/Wi-Fi positioning, and persistent background location.",
        iconKey = "location"
    ),
    PHONE_IDENTITY(
        title = "Phone & Identity",
        baseWeight = 8,
        description = "Device telephony state, phone number extraction, and programmatic outgoing call initiation.",
        iconKey = "phone"
    ),
    STORAGE_MEDIA(
        title = "Storage & Media",
        baseWeight = 8,
        description = "Reading or modifying shared device storage, photos, videos, audio, and all-files management.",
        iconKey = "storage"
    ),
    BACKGROUND_PERSISTENCE(
        title = "Background & Persistence",
        baseWeight = 6,
        description = "Autostart on device boot, foreground service execution, and battery optimization exemptions.",
        iconKey = "sync"
    ),
    NETWORK(
        title = "Network Communication",
        baseWeight = 2,
        description = "Internet access and network state monitoring (becomes higher risk when paired with sensitive data).",
        iconKey = "network"
    )
}
