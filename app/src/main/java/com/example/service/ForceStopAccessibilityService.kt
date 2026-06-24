package com.example.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ForceStopAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.packageName == "com.android.settings") {
            val rootNode = rootInActiveWindow ?: return

            // 1. Look for "Force stop" or "Force Stop" button and click it to initiate termination
            val forceStopNodes = rootNode.findAccessibilityNodeInfosByText("Force stop")
                .plus(rootNode.findAccessibilityNodeInfosByText("Force Stop"))

            for (node in forceStopNodes) {
                if (node.isClickable && node.isEnabled) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return // Wait for the next event (the confirmation dialog)
                }
            }

            // 2. Look for "OK" or "Force stop" button in the confirmation warning dialog and confirm it
            val okNodes = rootNode.findAccessibilityNodeInfosByText("OK")
                .plus(rootNode.findAccessibilityNodeInfosByText("Force stop"))
                .plus(rootNode.findAccessibilityNodeInfosByText("Force Stop"))

            for (node in okNodes) {
                if (node.isClickable && node.className?.toString()?.contains("Button") == true) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    
                    // Optionally, trigger a back navigation or home navigation to return the user
                    // to the System Pulse app once the task is killed.
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    break
                }
            }
        }
    }

    override fun onInterrupt() {
        // Required override, no action needed
    }
}
