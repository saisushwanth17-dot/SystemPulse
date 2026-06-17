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
    val processes by viewModel.processesList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val accentIndex by viewModel.accentColorIndex.collectAsState()

    val context = LocalContext.current
    var selectedProcessForDetails by remember { mutableStateOf<ProcessState?>(null) }
    var showExplanationDialog by remember { mutableStateOf(false) }

    // Visual theme color index mapping
    val accentColors = listOf(
        Color(0xFF00E5FF), // Cyan
        Color(0xFF00E676), // Green
        Color(0xFF2979FF), // Blue
        Color(0xFFFF9100), // Orange
        Color(0xFFFF1744)  // Red
    )
    val themeAccent = accentColors.getOrElse(accentIndex) { Color(0xFF00E5FF) }

    // Boost message status state
    var boostMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshProcesses()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header with Refresh Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Process Manager",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Inspect and boost operating system priority queues",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showExplanationDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = themeAccent.copy(alpha = 0.15f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = "Interview Guide",
                            tint = themeAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.refreshProcesses() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh list",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Animated Boost Banner Alert
            AnimatedVisibility(
                visible = boostMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00E676).copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f)),
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

            // Search Filter Panel
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = { Text("Search by packagename or label...", fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeAccent) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Outlined.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Category Filter Row (All, User, System)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("All", "User", "System")
                categories.forEach { cat ->
                    val isSelected = filterType == cat
                    Card(
                        onClick = { viewModel.filterType.value = cat },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) themeAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Sort Dropdown Button
                var showSortMenu by remember { mutableStateOf(false) }
                Box {
                    AssistChip(
                        onClick = { showSortMenu = true },
                        label = { Text("Sort: $sortBy", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        trailingIcon = { Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("RAM Consumption") },
                            onClick = {
                                viewModel.sortBy.value = "RAM"
                                showSortMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Memory, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("App Name (A-Z)") },
                            onClick = {
                                viewModel.sortBy.value = "Name"
                                showSortMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.SortByAlpha, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Process ID (PID)") },
                            onClick = {
                                viewModel.sortBy.value = "PID"
                                showSortMenu = false
                            },
                            leadingIcon = { Icon(Icons.Outlined.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Real-time Active Processes List
            if (processes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FindInPage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching processes running",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(processes, key = { it.packageName }) { process ->
                        ProcessItemCard(
                            process = process,
                            themeAccent = themeAccent,
                            onKillAction = {
                                viewModel.killProcess(process.packageName) { bytesFreed ->
                                    val freedMb = Formatters.formatBytes(bytesFreed)
                                    boostMessage = "Boost successful: Halted `${process.appName}` process, reclaimed $freedMb memory!"
                                    // Auto clear banner after 3.5s
                                    coroutineScope.launch {
                                        delay(3500)
                                        if (boostMessage?.contains(process.appName) == true) {
                                            boostMessage = null
                                        }
                                    }
                                }
                            },
                            onInspectAction = {
                                selectedProcessForDetails = process
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Interview Details Guide
    if (selectedProcessForDetails != null) {
        val detail = selectedProcessForDetails!!
        AlertDialog(
            onDismissRequest = { selectedProcessForDetails = null },
            confirmButton = {
                TextButton(
                    onClick = { selectedProcessForDetails = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = themeAccent)
                ) {
                    Text("Dismiss Inspector")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(themeAccent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    try {
                                        setImageDrawable(ctx.packageManager.getApplicationIcon(detail.packageName))
                                    } catch (e: Exception) {
                                        setImageResource(android.R.drawable.sym_def_app_icon)
                                    }
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = detail.appName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "PID: ${detail.pid}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = themeAccent)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Package Name:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(detail.packageName, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Memory Footprint:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(Formatters.formatBytes(detail.ramBytesUsed), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeAccent)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("OS Importance Level:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(detail.importance, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Process Priority (OOM adj):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        val oomRating = when {
                            detail.importance.contains("Foreground") -> "OOM_ADJ = 0 (Persistent)"
                            detail.importance.contains("Visible") -> "OOM_ADJ = 100"
                            detail.importance.contains("Service") -> "OOM_ADJ = 500"
                            else -> "OOM_ADJ = 900+ (Cache-Reapable)"
                        }
                        Text(oomRating, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFFFF9100))
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "BTech Interview Trivia:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeAccent,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            val triviaText = when {
                                detail.isSystemApp -> "System Core apps run inside UID 1000 or root sandboxes. They cannot be forcefully stopped by user-space applications to guard kernel stability."
                                detail.importance.contains("Service") -> "Services run in background, utilizing START_STICKY properties in the Android Services Scheduler to automatically respawn."
                                else -> "This process is cached. When physical RAM hits the threshold, the Low Memory Killer Daemon (LMK) will reap this process using Unix SIGKILL commands based on its OOM scores."
                            }
                            Text(text = triviaText, fontSize = 11.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        )
    }

    // Modal Educational Guide Dialog
    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            confirmButton = {
                Button(
                    onClick = { showExplanationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = themeAccent, contentColor = Color.Black)
                ) {
                    Text("Got it!", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = null,
                        tint = themeAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("BTech CSE Theory: Android Process Lifecycle")
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "How does Android manage processes? Here are critical concepts frequently asked in Android framework & Operating Systems placement interviews:",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }

                    item {
                        Text(
                            text = "1. Operating System Sandboxing",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every Android application is allocated its own unique Unix User ID (UID) on installation. It runs isolated in its own ART (Android Runtime) JVM sandbox instance to prevent cross-process memory corruption.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Text(
                            text = "2. The low memory killer daemon (LMKD)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When physical memory is scarce, standard Linux swap memory is absent in Android. LMKD reclaims cache through OOM Score (Out-Of-Memory Adjacency rating from -1000 to 1000). The higher the score, the earlier Linux terminates it.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Text(
                            text = "3. Lifecycle Importance States",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Foreground: The active visible activity on-screen. OOM rating lowest (0).\n• Visible: Unfocused but partially visual (e.g., covered by translucent dialog). \n• Service: Running background worker doing networks/tasks. \n• Cached: Dormant task in back-stack memory. Reclaimed first during RAM boost.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun ProcessItemCard(
    process: ProcessState,
    themeAccent: Color,
    onKillAction: () -> Unit,
    onInspectAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspectAction() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Android App Icon via PackageManager drawable rendering loading dynamically
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
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
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = process.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Color Tag indicating importance level
                    val importanceColor = when {
                        process.packageName == LocalContext.current.packageName -> themeAccent
                        process.importance.contains("Foreground") -> Color(0xFFFF1744)
                        process.isSystemApp -> Color(0xFF00FFCC)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(importanceColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (process.packageName == LocalContext.current.packageName) "Self" else if (process.isSystemApp) "System" else "User",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = importanceColor
                        )
                    }
                }

                Text(
                    text = process.packageName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1
                )

                Text(
                    text = "OS priority: ${process.importance}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeAccent.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right column: Memory used, plus force kill action
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = Formatters.formatBytes(process.ramBytesUsed),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "PID: ${process.pid}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (process.packageName != LocalContext.current.packageName && !process.isSystemApp) {
                    IconButton(
                        onClick = onKillAction,
                        modifier = Modifier.size(24.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFFFF1744).copy(alpha = 0.1f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PowerSettingsNew,
                            contentDescription = "Halt process",
                            tint = Color(0xFFFF1744),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
