package com.example.sentinel.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object AppSettingsNavigator {

    sealed class NavigationResult {
        object Success : NavigationResult()
        data class Failure(val userMessage: String) : NavigationResult()
    }

    fun openAppDetails(context: Context, packageName: String): NavigationResult {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            NavigationResult.Success
        } catch (e: ActivityNotFoundException) {
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
                Toast.makeText(context, "Navigated to general settings. Please locate $packageName manually.", Toast.LENGTH_LONG).show()
                NavigationResult.Success
            } catch (ex: Exception) {
                NavigationResult.Failure("Unable to open device settings for $packageName.")
            }
        }
    }

    fun openOverlaySettings(context: Context, packageName: String): NavigationResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                NavigationResult.Success
            } else {
                openAppDetails(context, packageName)
            }
        } catch (_: Exception) {
            // Fallback to general overlay screen without package URI
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallback)
                    NavigationResult.Success
                } else {
                    openAppDetails(context, packageName)
                }
            } catch (ex: Exception) {
                NavigationResult.Failure("Overlay permission settings are not supported or available on this device.")
            }
        }
    }

    fun openAccessibilitySettings(context: Context): NavigationResult {
        return try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            NavigationResult.Success
        } catch (e: Exception) {
            NavigationResult.Failure("Accessibility settings page could not be opened.")
        }
    }

    fun openNotificationListenerSettings(context: Context): NavigationResult {
        return try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
            NavigationResult.Success
        } catch (e: Exception) {
            NavigationResult.Failure("Notification Listener settings page could not be opened.")
        }
    }

    fun openUsageAccessSettings(context: Context): NavigationResult {
        return try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            NavigationResult.Success
        } catch (e: Exception) {
            NavigationResult.Failure("Usage access settings page could not be opened.")
        }
    }

    fun openSecuritySettings(context: Context): NavigationResult {
        return try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            NavigationResult.Success
        } catch (e: Exception) {
            NavigationResult.Failure("Security settings page could not be opened.")
        }
    }

    fun requestUninstall(context: Context, packageName: String): NavigationResult {
        return try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            NavigationResult.Success
        } catch (e: Exception) {
            NavigationResult.Failure("Uninstall request dialog could not be launched for $packageName.")
        }
    }
}
