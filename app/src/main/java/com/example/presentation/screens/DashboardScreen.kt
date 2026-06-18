package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.CircularPulseGauge
import com.example.presentation.components.RealTimeSparkline
import com.example.presentation.components.SegmentedProgressBar
import com.example.presentation.viewmodel.SystemPulseViewModel
import com.example.util.Formatters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: SystemPulseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val rawSystemState by viewModel.systemState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val accentIndex by viewModel.accentColorIndex.collectAsState()

    // Sliding window of CPU usage for the real-time sparkline graph
    val cpuHistory = remember { mutableStateListOf<Float>() }

    // Map selected accent colors
    val accentColors = listOf(
        Color(0xFF00E5FF), // Cyan
        Color(0xFF00E676), // Green
        Color(0xFF2979FF), // Blue
        Color(0xFFFF9100), // Orange
        Color(0xFFFF1744)  // Red
    )
    val themeAccent = accentColors.getOrElse(accentIndex) { Color(0xFF00E5FF) }

    // Quick boost dialog state
    var showBoostAnimation by remember { mutableStateOf(false) }
    var boostedBytesFreed by remember { mutableStateOf(0L) }

    // Append CPU readings to list history dynamically
    LaunchedEffect(rawSystemState) {
        rawSystemState?.cpu?.totalUsagePercent?.let { usage ->
            cpuHistory.add(usage)
            if (cpuHistory.size > 50) {
                cpuHistory.removeAt(0)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val systemState = rawSystemState
            if (systemState == null) {
                // Centered Loader State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = themeAccent)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Polling kernel diagnostics...",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Title Bar / Uptime Hub Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "System Monitoring",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Day 3 Advanced Diagnostics Enabled",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeAccent
                            )
                        }

                        // Compact Quick Boost FAB-Style pill
                        Card(
                            onClick = {
                                if (!showBoostAnimation) {
                                    showBoostAnimation = true
                                    viewModel.executeQuickBoost { freed ->
                                        boostedBytesFreed = freed
                                        coroutineScope.launch {
                                            delay(1500) // Keep the animation showing for effect
                                            showBoostAnimation = false
                                            Toast.makeText(
                                                context,
                                                "RAM Optimized! Freed ${Formatters.formatBytes(freed)}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = themeAccent.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, themeAccent.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Speed,
                                    contentDescription = "Quick Boost",
                                    tint = themeAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Boost",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeAccent
                                )
                            }
                        }
                    }

                    // Progress Ring Gauges section (CPU, RAM, Battery)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CPU Core Indicator
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                CircularPulseGauge(
                                    value = systemState.cpu.totalUsagePercent,
                                    title = "CPU",
                                    metricText = "${systemState.cpu.totalUsagePercent.toInt()}%",
                                    accentColor = themeAccent,
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    text = "${systemState.cpu.coreCount} Cores active",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }

                        // RAM Core Indicator
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                CircularPulseGauge(
                                    value = systemState.ram.usagePercent,
                                    title = "RAM",
                                    metricText = "${systemState.ram.usagePercent.toInt()}%",
                                    accentColor = Color(0xFF00E676), // Standard green accent
                                    modifier = Modifier.size(80.dp)
                                )
                                Text(
                                    text = "${Formatters.formatBytes(systemState.ram.availableBytes)} Avail",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }

                        // Battery Indicator
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                CircularPulseGauge(
                                    value = systemState.battery.percentage.toFloat(),
                                    title = "BATTERY",
                                    metricText = "${systemState.battery.percentage}%",
                                    accentColor = if (systemState.battery.isCharging) Color(0xFFFF9100) else Color(0xFF2979FF),
                                    modifier = Modifier.size(80.dp)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (systemState.battery.isCharging) {
                                        Icon(
                                            imageVector = Icons.Filled.Bolt,
                                            contentDescription = "Charging",
                                            tint = Color(0xFFFF9100),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = if (systemState.battery.isCharging) "Charging" else "Discharging",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    // Real-Time CPU History Line Graph Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.QueryStats,
                                        contentDescription = null,
                                        tint = themeAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CPU Pulse Diagnostics",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Live Trend",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            RealTimeSparkline(
                                history = cpuHistory.toList(),
                                lineColor = themeAccent,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Network Bandwidth real-time dials
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.SettingsInputAntenna,
                                        contentDescription = null,
                                        tint = Color(0xFF2979FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Current Network Speed",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Badge(
                                    containerColor = Color(0xFF2979FF).copy(alpha = 0.15f),
                                    contentColor = Color(0xFF2979FF)
                                ) {
                                    Text(
                                        text = systemState.network.activeInterface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                // Download Speeds Dial
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.ArrowDownward,
                                            contentDescription = "Download link",
                                            tint = Color(0xFF00E676),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Download",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = Formatters.formatSpeed(systemState.network.rxBytesPerSec),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Separator
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(36.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                )

                                // Upload Speeds Dial
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.ArrowUpward,
                                            contentDescription = "Upload link",
                                            tint = Color(0xFF2979FF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Upload",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = Formatters.formatSpeed(systemState.network.txBytesPerSec),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Storage Partition Breakdown bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storage,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9100),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "System Disk Space",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${systemState.storage.usagePercent.toInt()}% Used",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF9100)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            SegmentedProgressBar(
                                value = systemState.storage.usagePercent,
                                segments = 12,
                                height = 8.dp,
                                lowColor = Color(0xFF00E676),
                                mediumColor = Color(0xFFFF9100),
                                highColor = Color(0xFFFF1744)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Used: ${Formatters.formatBytes(systemState.storage.usedBytes)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "Free: ${Formatters.formatBytes(systemState.storage.freeBytes)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Boost scanner modal overlay
            if (showBoostAnimation) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = themeAccent,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "COMPACTING GARBAGE COLLECTION...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeAccent,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Purging memory caches, optimizing OS buffers",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
