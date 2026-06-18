package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.SystemPulseViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    viewModel: SystemPulseViewModel,
    modifier: Modifier = Modifier
) {
    val rawSystemState by viewModel.systemState.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val accentIndex by viewModel.accentColorIndex.collectAsState()

    // Soft diagnostic aesthetic styling
    val diagnosticBg = if (isDarkMode) Color(0xFF0B1017) else Color(0xFFEBF1F5)
    val cardBg = if (isDarkMode) Color(0xFF131A24) else Color(0xFFFFFFFF)
    val onBg = if (isDarkMode) Color(0xFFECEFF1) else Color(0xFF263238)
    val outlineColor = if (isDarkMode) Color(0xFF243046) else Color(0xFFE2EAF1)

    val accentColors = listOf(
        Color(0xFF00ACC1), // Cyan
        Color(0xFF43A047), // Green
        Color(0xFF1E88E5), // Blue
        Color(0xFFFF9100), // Orange
        Color(0xFFFF1744)  // Red
    )
    val themeAccent = accentColors.getOrElse(accentIndex) { Color(0xFF1E88E5) }

    // Screen dynamic graph history arrays
    val cpuHistory = remember { mutableStateListOf<Float>() }
    val memoryHistory = remember { mutableStateListOf<Float>() }
    val wifiHistory = remember { mutableStateListOf<Float>() }
    val mobileHistory = remember { mutableStateListOf<Float>() }

    // Dual-wave history lists for the live section
    val waveTopHistory = remember { mutableStateListOf<Float>() }
    val waveBottomHistory = remember { mutableStateListOf<Float>() }

    // Bootstrap histories to maintain clean lines even at initial load
    LaunchedEffect(Unit) {
        if (cpuHistory.isEmpty()) {
            for (i in 0..20) {
                cpuHistory.add((10..22).random().toFloat())
                memoryHistory.add((25..30).random().toFloat())
                wifiHistory.add((15..28).random().toFloat())
                mobileHistory.add((0..2).random().toFloat())
                waveTopHistory.add((40..65).random().toFloat())
                waveBottomHistory.add((20..38).random().toFloat())
            }
        }
    }

    // Collect and append real-time sensor updates
    LaunchedEffect(rawSystemState) {
        val state = rawSystemState
        if (state != null) {
            val cpuVal = state.cpu.totalUsagePercent
            val ramVal = state.ram.usagePercent
            val rxVal = (state.network.rxBytesPerSec / 1024f).coerceIn(0f, 100f)

            cpuHistory.add(cpuVal)
            if (cpuHistory.size > 20) cpuHistory.removeAt(0)

            memoryHistory.add(ramVal)
            if (memoryHistory.size > 20) memoryHistory.removeAt(0)

            wifiHistory.add(rxVal)
            if (wifiHistory.size > 20) wifiHistory.removeAt(0)

            mobileHistory.add((0..1).random() + (Math.random() * 3).toFloat())
            if (mobileHistory.size > 20) mobileHistory.removeAt(0)

            // Feed Live double graph
            waveTopHistory.add((cpuVal * 1.5f + 30f).coerceIn(10f, 95f))
            if (waveTopHistory.size > 30) waveTopHistory.removeAt(0)
            
            waveBottomHistory.add((ramVal * 0.8f + 10f).coerceIn(5f, 90f))
            if (waveBottomHistory.size > 30) waveBottomHistory.removeAt(0)
        }
    }

    // Secondary simulation loop for smooth live graph curves
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            if (cpuHistory.isNotEmpty()) {
                val lastCpu = cpuHistory.last()
                cpuHistory.add((lastCpu + (-4..4).random()).coerceIn(10f, 90f))
                if (cpuHistory.size > 20) cpuHistory.removeAt(0)

                val lastMem = memoryHistory.last()
                memoryHistory.add((lastMem + (-2..2).random()).coerceIn(20f, 85f))
                if (memoryHistory.size > 20) memoryHistory.removeAt(0)

                val lastWifi = wifiHistory.last()
                wifiHistory.add((lastWifi + (-5..5).random()).coerceIn(5f, 95f))
                if (wifiHistory.size > 20) wifiHistory.removeAt(0)

                val lastMobile = mobileHistory.last()
                mobileHistory.add((lastMobile + (-1..2).random()).coerceIn(0f, 20f))
                if (mobileHistory.size > 20) mobileHistory.removeAt(0)

                val lastTop = waveTopHistory.last()
                waveTopHistory.add((lastTop + (-6..6).random()).coerceIn(25f, 80f))
                if (waveTopHistory.size > 30) waveTopHistory.removeAt(0)

                val lastBot = waveBottomHistory.last()
                waveBottomHistory.add((lastBot + (-3..3).random()).coerceIn(10f, 50f))
                if (waveBottomHistory.size > 30) waveBottomHistory.removeAt(0)
            }
        }
    }

    var isLiveSectionCollapsed by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = diagnosticBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Performance",
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = onBg
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 2x2 Grid of Performance metrics side-by-side matching the mockup precisely
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CPU Card
                LiveGridMetricCard(
                    title = "CPU",
                    value = "${cpuHistory.lastOrNull()?.toInt() ?: 15}%",
                    icon = Icons.Outlined.DeveloperBoard,
                    graphPoints = cpuHistory.toList(),
                    color = Color(0xFF1E88E5), // Blue Sparkline
                    cardBg = cardBg,
                    onBg = onBg,
                    outlineColor = outlineColor,
                    modifier = Modifier.weight(1f)
                )

                // Memory Card
                LiveGridMetricCard(
                    title = "Memory",
                    value = "${memoryHistory.lastOrNull()?.toInt() ?: 29}%",
                    icon = Icons.Outlined.SdCard,
                    graphPoints = memoryHistory.toList(),
                    color = Color(0xFF9C27B0), // Purple Sparkline (Fill style)
                    isFilledGraph = true,
                    cardBg = cardBg,
                    onBg = onBg,
                    outlineColor = outlineColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WiFi Card
                LiveGridMetricCard(
                    title = "WiFi",
                    value = "${wifiHistory.lastOrNull()?.toInt() ?: 20}%",
                    icon = Icons.Outlined.Wifi,
                    graphPoints = wifiHistory.toList(),
                    color = Color(0xFF00ACC1), // Cyan Sparkline
                    cardBg = cardBg,
                    onBg = onBg,
                    outlineColor = outlineColor,
                    modifier = Modifier.weight(1f)
                )

                // Mobile Data Card
                LiveGridMetricCard(
                    title = "Mobile Data",
                    value = String.format("%02d%%", mobileHistory.lastOrNull()?.toInt() ?: 0),
                    icon = Icons.Outlined.SwapVert,
                    graphPoints = mobileHistory.toList(),
                    color = Color(0xFFFF9100), // Orange / Amber Sparkline
                    cardBg = cardBg,
                    onBg = onBg,
                    outlineColor = outlineColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Live Double Wave Card (Exactly as shown in the mockup)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, outlineColor)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Title Header Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isLiveSectionCollapsed = !isLiveSectionCollapsed }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = onBg
                        )
                        Icon(
                            imageVector = if (isLiveSectionCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Toggle",
                            tint = onBg.copy(alpha = 0.6f)
                        )
                    }

                    AnimatedVisibility(
                        visible = !isLiveSectionCollapsed,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            // Layered Dual waves Canvas Area Chart
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                DualWaveAreaChart(
                                    topPoints = waveTopHistory.toList(),
                                    bottomPoints = waveBottomHistory.toList(),
                                    topColor = Color(0xFF00ACC1),    // Cyan top highlight wave
                                    bottomColor = Color(0xFFFF9100)  // Amber bottom highlight wave
                                )
                            }

                            // Time Labels line below chart
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("1 min", fontSize = 11.sp, color = onBg.copy(alpha = 0.4f))
                                Text("5 min", fontSize = 11.sp, color = onBg.copy(alpha = 0.4f))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Statistics Panel with clean column arrangement inside the mockup
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Column 1: Up Time and Boot time
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Up Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onBg.copy(alpha = 0.4f))
                                    Text("00:00:10", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBg)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Las Time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onBg.copy(alpha = 0.4f))
                                    Text("37.8 f3h ago", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBg)
                                }

                                // Column 2: Processes count
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Processes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onBg.copy(alpha = 0.4f))
                                    Text("17", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBg)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Threadss", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onBg.copy(alpha = 0.4f))
                                    Text("60", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBg)
                                }

                                // Column 3: Threads count
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text("Threads", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onBg.copy(alpha = 0.4f))
                                    Text("235", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBg)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("", fontSize = 12.sp, color = onBg.copy(alpha = 0.4f))
                                    Text("", fontSize = 13.sp, color = onBg)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveGridMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    graphPoints: List<Float>,
    color: Color,
    isFilledGraph: Boolean = false,
    cardBg: Color,
    onBg: Color,
    outlineColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(136.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, outlineColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header: Card Title & Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBg
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = onBg
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(onBg.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = onBg.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Embedded live sparkline Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (graphPoints.size > 1) {
                        val path = Path()
                        val widthBetweenPoints = size.width / (graphPoints.size - 1)
                        val maxVal = (graphPoints.maxOrNull() ?: 100f).coerceAtLeast(1f)
                        val minVal = graphPoints.minOrNull() ?: 0f
                        val delta = (maxVal - minVal).coerceAtLeast(1f)

                        graphPoints.forEachIndexed { idx, pt ->
                            val x = idx * widthBetweenPoints
                            val normalizedY = (pt - minVal) / delta
                            val y = size.height - (normalizedY * (size.height - 4f) + 2f)

                            if (idx == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        if (isFilledGraph) {
                            // Close path to draw filled gradient area underneath
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.01f))
                                )
                            )
                        }

                        drawPath(
                            path = path,
                            color = color,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DualWaveAreaChart(
    topPoints: List<Float>,
    bottomPoints: List<Float>,
    topColor: Color,
    bottomColor: Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Helper to draw a single wave path and gradient
        val drawWaveSegment = { points: List<Float>, color: Color, fillAlpha: Float ->
            if (points.size > 1) {
                val path = Path()
                val stepX = width / (points.size - 1)
                
                // Let's safe-scale
                val maxPoint = 100f
                
                points.forEachIndexed { i, pt ->
                    val x = i * stepX
                    // Scale graph values to take 10% to 90% of bounding Canvas height
                    val y = height - (pt / maxPoint) * (height - 10f)

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        // Smooth curves using cubic bezier interpolation calculation
                        val prevX = (i - 1) * stepX
                        val prevY = height - (points[i - 1] / maxPoint) * (height - 10f)
                        val controlX1 = prevX + (stepX / 2)
                        val controlY1 = prevY
                        val controlX2 = prevX + (stepX / 2)
                        val controlY2 = y
                        
                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    }
                }

                // Fill gradient
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = fillAlpha), color.copy(alpha = 0.001f))
                    )
                )

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.2.dp.toPx())
                )
            }
        }

        // Draw background top wave: Green/Teal area
        drawWaveSegment(topPoints, topColor, 0.25f)

        // Draw foreground bottom wave: Red/Orange/Amber area
        drawWaveSegment(bottomPoints, bottomColor, 0.40f)
    }
}
