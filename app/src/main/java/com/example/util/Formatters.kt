package com.example.util

import java.util.Locale

object Formatters {
    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes\u00A0B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val suffix = "KMGTPE"[exp - 1] + "B"
        return String.format(Locale.US, "%.2f\u00A0%s", bytes / Math.pow(1024.0, exp.toDouble()), suffix)
    }

    fun formatSpeed(bytesPerSec: Long): String {
        val bitsPerSec = bytesPerSec * 8
        if (bitsPerSec < 1024) return "$bitsPerSec\u00A0bps"
        val exp = (Math.log(bitsPerSec.toDouble()) / Math.log(1024.0)).toInt()
        val suffix = "KMGTPE"[exp - 1] + "bps"
        return String.format(Locale.US, "%.1f\u00A0%s", bitsPerSec / Math.pow(1024.0, exp.toDouble()), suffix)
    }

    fun formatTemperature(tempCelsius: Float?): String {
        if (tempCelsius == null || tempCelsius <= 0f) return "-- °C"
        return String.format(Locale.US, "%.1f°C", tempCelsius)
    }

    fun formatFrequency(mhz: Long): String {
        if (mhz <= 0) return "-- MHz"
        if (mhz >= 1000) {
            return String.format(Locale.US, "%.2f GHz", mhz / 1000f)
        }
        return "$mhz MHz"
    }

    fun getSimpleRoleName(packageName: String, appName: String): String {
        if (packageName.isBlank()) return "Active Task"
        val pkgLower = packageName.lowercase(Locale.US)
        return when {
            pkgLower.contains("youtube") -> "Video Streaming & Playback Engine"
            pkgLower.contains("chrome") -> "Web Browser Service & Rendering Engine"
            pkgLower.contains("maps") -> "GPS & Location Mapping Service"
            pkgLower.contains("gmail") -> "Email Messaging & Push Gateway"
            pkgLower.contains("vending") -> "Google Play App Store Core"
            pkgLower.contains("gservices") -> "Play Services System Framework"
            pkgLower.contains("contacts") -> "Contact Synchronization & Manager"
            pkgLower.contains("calendar") -> "Calendar Event Scheduler"
            pkgLower.contains("telephony") -> "Cellular Signal Coordinator"
            pkgLower.contains("phone") -> "Phone Calling Interface & Route"
            pkgLower.contains("bluetooth") -> "Bluetooth Signal Controller"
            pkgLower.contains("wifi") -> "Wireless Network Connectivity"
            pkgLower.contains("camera") -> "Hardware Camera & Lens Controller"
            pkgLower.contains("gallery") -> "Media Viewer Gallery"
            pkgLower.contains("keyboard") -> "On-Screen Input Method Suite"
            pkgLower.contains("download") -> "System Downloader Service"
            pkgLower.contains("settings") -> "System Diagnostic Preferences"
            pkgLower.contains("launcher") -> "Android Home screen Interface"
            pkgLower.contains("systemui") -> "Status Bar & OS Navigation Panels"
            pkgLower.contains("packageinstaller") -> "App Installer & Integrity Daemon"
            pkgLower.contains("gms") -> "Google Play Services Mainframe"
            pkgLower.contains("security") -> "OS Integrity & Antivirus Daemon"
            pkgLower.contains("media") -> "Media Controller Subsystem"
            pkgLower.contains("providers") -> "Core Content Database Provider"
            pkgLower.startsWith("com.android.") -> "Android OS System Component"
            pkgLower.startsWith("com.google.") -> "Google Cloud Services Support"
            else -> "User Application Activity ($appName)"
        }
    }

    fun getSimplePriorityName(importance: String): String {
        val impLower = importance.lowercase(Locale.US)
        return when {
            impLower.contains("foreground") || impLower.contains("active") -> "Active on Screen"
            impLower.contains("visible") -> "Running in Background / Visible"
            impLower.contains("service") -> "Active System Service"
            impLower.contains("perceptible") -> "Active Audio / Notification"
            impLower.contains("cached") || impLower.contains("sleeping") -> "Sleeping / Cached Memory"
            else -> "Optimized Background Thread"
        }
    }
}
