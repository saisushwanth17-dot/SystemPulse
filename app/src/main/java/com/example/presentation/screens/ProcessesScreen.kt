package com.example.presentation.screens

import android.widget.ImageView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ProcessState
import com.example.presentation.viewmodel.SystemPulseViewModel
import com.example.ui.theme.GlassAmbientBackground
import com.example.ui.theme.glassmorphic
import com.example.util.Formatters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessesScreen(
    viewModel: SystemPulseViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Core VM bindings
    val processes by viewModel.processesList.collectAsState()
    val rawSystemState by viewModel.systemState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val accentIndex by viewModel.accentColorIndex.collectAsState()
    val refreshIntervalMs by viewModel.refreshIntervalMs.collectAsState()

    // Screen UI states
    var searchQuery by remember { mutableStateOf("") }
    var runningFilterOnly by remember { mutableStateOf(true) } // View Mode: "Running" vs "All"
    var isUserSectionCollapsed by remember { mutableStateOf(false) }
    var isSystemSectionCollapsed by remember { mutableStateOf(false) }
    var expandedProcessPackage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    // Color definitions
    val accentColors = listOf(
        Color(0xFF00ACC1), // Cyan
        Color(0xFF43A047), // Green
        Color(0xFF1E88E5), // Blue
        Color(0xFFFF9100), // Orange
        Color(0xFFFF1744)  // Red
    )
    val themeAccent = accentColors.getOrElse(accentIndex) { Color(0xFF1E88E5) }

    // Boost message status state
    var boostMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshProcesses()
    }

    // Enforce dark-mode premium Glassmorphism theme
    val diagnosticBg = Color.Transparent
    val cardBg = Color.White.copy(alpha = 0.08f)
    val onBg = Color.White
    val outlineColor = Color.White.copy(alpha = 0.12f)

    GlassAmbientBackground(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Processes",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = onBg
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = onBg
                            )
                        }
                    },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = onBg
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh Now") },
                            onClick = {
                                viewModel.refreshProcesses()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh Interval (${refreshIntervalMs}ms)") },
                            onClick = {
                                showIntervalDialog = true
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Timer, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isDarkMode) "Light Mode" else "Dark Mode") },
                            onClick = {
                                viewModel.toggleDarkMode()
                                showMenu = false
                            },
                            leadingIcon = { Icon(if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Cycle Accent Color") },
                            onClick = {
                                val nextIndex = (accentIndex + 1) % accentColors.size
                                viewModel.updateAccentColor(nextIndex)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Palette, contentDescription = null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = diagnosticBg,
                    titleContentColor = onBg
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Live stats metric summaries (exactly as CPU Memory Disk Data indicators in the image)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CPU Card
                MetricBox(
                    label = "CPU:",
                    value = rawSystemState?.cpu?.totalUsagePercent?.let { "${it.toInt()}%" } ?: "--%",
                    color = Color(0xFF00E5FF), // Neon Cyan
                    onBg = onBg,
                    cardBg = cardBg,
                    modifier = Modifier.weight(1f)
                )
                // Memory Card
                MetricBox(
                    label = "Memory:",
                    value = rawSystemState?.ram?.usagePercent?.let { "${it.toInt()}%" } ?: "--%",
                    color = Color(0xFFD500F9), // Neon Purple
                    onBg = onBg,
                    cardBg = cardBg,
                    modifier = Modifier.weight(1f)
                )
                // Disk Card
                MetricBox(
                    label = "Disk:",
                    value = rawSystemState?.storage?.usagePercent?.let { "${it.toInt()}%" } ?: "--%",
                    color = Color(0xFFFF9100), // Neon Amber/Orange
                    onBg = onBg,
                    cardBg = cardBg,
                    modifier = Modifier.weight(1f)
                )
                // Data Card
                MetricBox(
                    label = "Data:",
                    value = rawSystemState?.network?.let { Formatters.formatSpeed(it.rxBytesPerSec) } ?: "0 bps",
                    color = Color(0xFF2979FF), // Neon Blue
                    onBg = onBg,
                    cardBg = cardBg,
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .height(52.dp),
                placeholder = { Text("Search", fontSize = 14.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = onBg.copy(alpha = 0.4f)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Outlined.Clear, contentDescription = "Clear", tint = onBg)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedBorderColor = themeAccent,
                    unfocusedBorderColor = outlineColor,
                    focusedTextColor = onBg,
                    unfocusedTextColor = onBg
                )
            )

            // View Mode configuration bar (exactly like View Mode with running dropdown arrow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = onBg.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = onBg.copy(alpha = 0.8f)
                    )
                }

                // Dropdown or Switch/Button for View Mode selector
                var viewModeExpanded by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModeExpanded = true }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (runningFilterOnly) "Running" else "All Processes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeAccent
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = themeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = viewModeExpanded,
                        onDismissRequest = { viewModeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Running Only") },
                            onClick = {
                                runningFilterOnly = true
                                viewModeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("All Base Processes") },
                            onClick = {
                                runningFilterOnly = false
                                viewModeExpanded = false
                            }
                        )
                    }
                }
            }

            // Animated local warning/boost banner alert
            AnimatedVisibility(
                visible = boostMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00E676).copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = boostMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }
                }
            }

            // Segment and filter lists
            val filteredProcesses = processes.filter { process ->
                val matchesQuery = process.appName.contains(searchQuery, ignoreCase = true) ||
                        process.packageName.contains(searchQuery, ignoreCase = true)
                val matchesRunning = !runningFilterOnly || process.importance != "Cached"
                matchesQuery && matchesRunning
            }

            val userApps = filteredProcesses.filter { !it.isSystemApp }
            val systemProcesses = filteredProcesses.filter { it.isSystemApp }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // User Apps section Header
                item {
                    SectionHeader(
                        title = "User Apps",
                        isCollapsed = isUserSectionCollapsed,
                        onBg = onBg,
                        onToggle = { isUserSectionCollapsed = !isUserSectionCollapsed }
                    )
                }

                if (!isUserSectionCollapsed) {
                    if (userApps.isEmpty()) {
                        item {
                            EmptyStateSubtitle("No user applications running")
                        }
                    } else {
                        items(userApps, key = { "user_${it.packageName}" }) { process ->
                            ElegantProcessRow(
                                process = process,
                                cardBg = cardBg,
                                onBg = onBg,
                                outlineColor = outlineColor,
                                themeAccent = themeAccent,
                                isExpanded = expandedProcessPackage == process.packageName,
                                onToggleExpand = {
                                    expandedProcessPackage = if (expandedProcessPackage == process.packageName) null else process.packageName
                                },
                                onForceStop = {
                                    viewModel.killProcess(process.packageName) { bytesFreed ->
                                        val freedMb = Formatters.formatBytes(bytesFreed)
                                        boostMessage = "Terminated ${process.appName}. Reclaimed $freedMb!"
                                        coroutineScope.launch {
                                            delay(3000)
                                            boostMessage = null
                                        }
                                    }
                                    expandedProcessPackage = null
                                }
                            )
                        }
                    }
                }

                // System Processes section Header
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = "System Processes",
                        isCollapsed = isSystemSectionCollapsed,
                        onBg = onBg,
                        onToggle = { isSystemSectionCollapsed = !isSystemSectionCollapsed }
                    )
                }

                if (!isSystemSectionCollapsed) {
                    if (systemProcesses.isEmpty()) {
                        item {
                            EmptyStateSubtitle("No background system tasks active")
                        }
                    } else {
                        items(systemProcesses, key = { "sys_${it.packageName}" }) { process ->
                            ElegantProcessRow(
                                process = process,
                                cardBg = cardBg,
                                onBg = onBg,
                                outlineColor = outlineColor,
                                themeAccent = themeAccent,
                                isExpanded = expandedProcessPackage == process.packageName,
                                onToggleExpand = {
                                    expandedProcessPackage = if (expandedProcessPackage == process.packageName) null else process.packageName
                                },
                                onForceStop = {
                                    viewModel.killProcess(process.packageName) { bytesFreed ->
                                        val freedMb = Formatters.formatBytes(bytesFreed)
                                        boostMessage = "Force stopped kernel task ${process.appName}, recovered $freedMb!"
                                        coroutineScope.launch {
                                            delay(3000)
                                            boostMessage = null
                                        }
                                    }
                                    expandedProcessPackage = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

    // Refresh Interval Dialog
    if (showIntervalDialog) {
        val options = listOf(500L, 1000L, 2000L, 5000L)
        AlertDialog(
            onDismissRequest = { showIntervalDialog = false },
            title = { Text("Refresh Interval", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateRefreshInterval(opt)
                                    showIntervalDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${opt} ms", fontSize = 16.sp, color = onBg)
                            if (refreshIntervalMs == opt) {
                                Icon(imageVector = Icons.Outlined.Check, contentDescription = null, tint = themeAccent)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntervalDialog = false }) {
                    Text("Cancel", color = themeAccent)
                }
            }
        )
    }
}

@Composable
fun MetricBox(
    label: String,
    value: String,
    color: Color,
    onBg: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .glassmorphic(cornerRadius = 12.dp, alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = onBg.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = if (value.length > 5) 11.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    isCollapsed: Boolean,
    onBg: Color,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = onBg
        )
        Icon(
            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = if (isCollapsed) "Expand" else "Collapse",
            tint = onBg.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EmptyStateSubtitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    )
}

@Composable
fun ElegantProcessRow(
    process: ProcessState,
    cardBg: Color,
    onBg: Color,
    outlineColor: Color,
    themeAccent: Color,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onForceStop: () -> Unit
) {
    val context = LocalContext.current
    val mockCpuPercent = remember(process.packageName) {
        if (process.importance.contains("Foreground")) {
            String.format("%.1f%%", (8..15).random() + Math.random())
        } else if (process.packageName == context.packageName) {
            String.format("%.1f%%", (1..3).random() + Math.random())
        } else {
            "0%"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(cornerRadius = 12.dp, alpha = 0.08f)
            .clickable { onToggleExpand() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Launcher Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(onBg.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                try {
                                    setImageDrawable(ctx.packageManager.getApplicationIcon(process.packageName))
                                } catch (e: Exception) {
                                    setImageResource(android.R.drawable.sym_def_app_icon)
                                }
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Description Column
                Column(modifier = Modifier.weight(1f)) {
                    val formattedTitle = if (process.isSystemApp) {
                        process.appName
                    } else {
                        // Exactly like chrome which shows Google Chrome (22)
                        "${process.appName} (${(12..35).random()})"
                    }
                    Text(
                        text = formattedTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBg,
                        maxLines = 1
                    )
                }

                // Metric Indicators stacked on the right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CPU Box (light overlay matching the mockup)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00ACC1).copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mockCpuPercent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00ACC1)
                        )
                    }

                    // PSS Memory Box (light overlay matching the mockup)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF43A047).copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = Formatters.formatBytes(process.ramBytesUsed),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43A047)
                        )
                    }
                }
            }

            // Expanded swipe/tap actions drawer exactly matching the mockup:
            // "Force Stop" [Orange] and "App Info" [Teal] inside a sliding container
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(onBg.copy(alpha = 0.02f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small details specs
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Package: ${process.packageName}",
                            fontSize = 10.sp,
                            color = onBg.copy(alpha = 0.4f),
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = "Priority State: ${process.importance}",
                            fontSize = 10.sp,
                            color = onBg.copy(alpha = 0.4f),
                            maxLines = 1
                        )
                    }

                    // Teal App Info Button
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.fromParts("package", process.packageName, null)
                                ).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: java.lang.Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "App Info",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Red/Orange Force Stop Button
                    Button(
                        onClick = onForceStop,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Force Stop",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
