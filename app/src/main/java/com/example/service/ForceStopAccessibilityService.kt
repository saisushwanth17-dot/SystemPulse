package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ForceStopAccessibilityService : AccessibilityService() {

    companion object {
        const val STATE_NONE = 0
        const val STATE_WAITING_FOR_APP_INFO = 1
        const val STATE_CLICKED_FORCE_STOP = 2
        const val STATE_COMPLETED = 3

        @Volatile
        var targetPackage: String? = null
        
        @Volatile
        var currentState: Int = STATE_NONE
        
        @Volatile
        var lastActionTime: Long = 0L
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val currentTarget = targetPackage ?: return

        // 10-second safety timeout to avoid any stuck states
        if (System.currentTimeMillis() - lastActionTime > 10000) {
            resetState()
            return
        }

        val rootNode = rootInActiveWindow ?: return

        when (currentState) {
            STATE_WAITING_FOR_APP_INFO -> {
                // Find "Force Stop" button on App Info page
                val forceStopNodes = mutableListOf<AccessibilityNodeInfo>()
                findNodesByTexts(rootNode, listOf("Force stop", "Force Stop", "FORCE STOP"), forceStopNodes)

                // Select the first clickable, enabled "Force Stop" button
                val nodeToClick = forceStopNodes.firstOrNull { it.isClickable && it.isEnabled }
                if (nodeToClick != null) {
                    currentState = STATE_CLICKED_FORCE_STOP
                    lastActionTime = System.currentTimeMillis()
                    nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("ForceStopService", "Clicked Force Stop button for $currentTarget")
                }
            }

            STATE_CLICKED_FORCE_STOP -> {
                // Find confirmation buttons on the warning popup
                val okNodes = mutableListOf<AccessibilityNodeInfo>()
                findNodesByTexts(rootNode, listOf("OK", "Confirm", "OKAY", "Yes", "Force stop", "Force Stop", "FORCE STOP"), okNodes)

                // Prioritize explicit OK/Confirm dialog options to avoid re-triggering the main button
                val dialogButton = okNodes.find {
                    val textStr = it.text?.toString()?.lowercase() ?: ""
                    textStr == "ok" || textStr == "confirm" || textStr == "okay" || textStr == "yes"
                } ?: okNodes.firstOrNull { it.isClickable && it.isEnabled }

                if (dialogButton != null) {
                    currentState = STATE_COMPLETED
                    dialogButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d("ForceStopService", "Confirmed Force Stop dialog for $currentTarget")

                    // Perform exactly one Back action to return the user to System Pulse
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    resetState()
                }
            }
        }
    }

    private fun findNodesByTexts(
        node: AccessibilityNodeInfo?,
        targetTexts: List<String>,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (node == null) return

        val textVal = node.text?.toString()?.trim()
        if (textVal != null) {
            if (targetTexts.any { it.equals(textVal, ignoreCase = true) }) {
                result.add(node)
            }
        }

        val descVal = node.contentDescription?.toString()?.trim()
        if (descVal != null) {
            if (targetTexts.any { it.equals(descVal, ignoreCase = true) }) {
                result.add(node)
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            findNodesByTexts(child, targetTexts, result)
        }
    }

    private fun resetState() {
        targetPackage = null
        currentState = STATE_NONE
        lastActionTime = 0L
    }

    override fun onInterrupt() {
        resetState()
    }
}
