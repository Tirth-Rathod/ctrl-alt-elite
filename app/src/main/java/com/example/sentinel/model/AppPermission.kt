package com.example.sentinel.model

enum class LocationPrecision {
    NONE,
    COARSE,
    FINE,
    BACKGROUND
}

data class AppPermission(
    val permissionName: String,
    val simpleName: String,
    val category: PermissionCategory,
    val isGranted: Boolean,
    val isDeclared: Boolean = true,
    val isDangerous: Boolean = false,
    val protectionLevel: String = "NORMAL",
    val description: String = "",
    val securityImplication: String = "",
    val locationPrecision: LocationPrecision = LocationPrecision.NONE
)
