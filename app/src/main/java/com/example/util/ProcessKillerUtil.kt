package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import java.io.DataOutputStream

object ProcessKillerUtil {

    /**
     * Attempts to terminate the process fully via Root (su) or fallback.
     * Returns true if root execution succeeded. Returns false if we fall back to Accessibility/Manual approach.
     */
    fun endTask(context: Context, packageName: String, appName: String): Boolean {
        if (tryRootForceStop(packageName)) {
            // Root method successfully executed `am force-stop`
            Toast.makeText(
                context,
                "Root Execution: Terminated $appName completely.",
                Toast.LENGTH_SHORT
            ).show()
            return true
        }

        // Fallback: Accessibility Service Automation / Manual Click
        openAppSettings(context, packageName, appName)
        return false
    }

    private fun tryRootForceStop(packageName: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("am force-stop $packageName\n")
            os.writeBytes("exit\n")
            os.flush()
            if (process.waitFor() == 0) {
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun openAppSettings(context: Context, packageName: String, appName: String) {
        try {
            Toast.makeText(
                context,
                "Root unavailable. Opening App Info for $appName to auto-click Force Stop via Accessibility Service...",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
