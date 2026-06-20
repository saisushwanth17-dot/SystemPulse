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
}
