package com.example.sentinel.model

enum class SpecialAccessType(
    val title: String,
    val description: String,
    val riskImpact: String
) {
    ACCESSIBILITY_SERVICE(
        title = "Accessibility Service",
        description = "Can observe user interface interactions, read on-screen text across other apps, and dispatch virtual clicks.",
        riskImpact = "Can inspect sensitive screens (banking, passwords, 2FA) or automate malicious gestures if abused."
    ),
    SYSTEM_ALERT_WINDOW(
        title = "Display Over Other Apps (Overlay)",
        description = "Allows drawing floating windows or views on top of any active application screen.",
        riskImpact = "Can be abused for tapjacking, phishing fake login dialogs, or cloaking underlying permission prompts."
    ),
    DEVICE_ADMIN(
        title = "Device Administrator",
        description = "Grants enterprise-level device management authority such as screen lock enforcement or remote wipe.",
        riskImpact = "Can hinder application uninstallation and impose rigid security policies on personal hardware."
    ),
    NOTIFICATION_LISTENER(
        title = "Notification Listener",
        description = "Intercepts, reads, and dismisses all system notifications posted by any application.",
        riskImpact = "Can silently capture private incoming chat messages and 2FA authentication OTPs."
    ),
    PACKAGE_INSTALL(
        title = "Request Install Packages",
        description = "Can trigger arbitrary APK installation prompts without user having to visit an app store.",
        riskImpact = "Can be leveraged as a dropper to prompt installation of secondary unverified payloads."
    ),
    USAGE_STATS(
        title = "App Usage Statistics Access",
        description = "Monitors how frequently and for how long the user launches and interacts with other apps.",
        riskImpact = "Exposes behavioral activity profiling, app inventory, and user habits to third parties."
    )
}

enum class SpecialAccessStatus(
    val label: String,
    val isGrantedOrEnabled: Boolean
) {
    NOT_REQUESTED("Not Requested", false),
    DECLARED_ONLY("Declared in Manifest (Not Enabled)", false),
    ENABLED_OR_GRANTED("Active / Granted", true)
}
