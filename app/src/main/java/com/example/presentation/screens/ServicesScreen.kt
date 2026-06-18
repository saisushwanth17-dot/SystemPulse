package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.SystemPulseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class representing a toggleable Android Service mockup item
data class ServiceMockState(
    val serviceName: String,
    val description: String,
    val isSystem: Boolean,
    var isEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    viewModel: SystemPulseViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val accentIndex by viewModel.accentColorIndex.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Soft diagnostic styling matching the mockup
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

    // Floating announcement state
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Collapse section states
    var isSystemCollapsed by remember { mutableStateOf(false) }
    var isUserGroup1Collapsed by remember { mutableStateOf(false) }
    var isUserGroup2Collapsed by remember { mutableStateOf(false) }

    // Mock services database exactly as represented in the fourth screenshot
    val servicesList = remember {
        mutableStateListOf(
            ServiceMockState(
                "com.google.android.location",
                "Controlled om.google.android.location description.",
                isSystem = true,
                isEnabled = true
            ),
            ServiceMockState(
                "com.google.android.user",
                "Convenient service clients convenient and android services.",
                isSystem = true,
                isEnabled = true
            ),
            ServiceMockState(
                "com.google.android.user",
                "Unoptimize com.google.android service.",
                isSystem = true,
                isEnabled = false
            ),
            // User Services - Group 1
            ServiceMockState(
                "com.google.android.manit",
                "Convenient service advertising authorization, and extemination services.",
                isSystem = false,
                isEnabled = true
            ),
            ServiceMockState(
                "com.google.android.location",
                "Provides diagnostic location, it already orients tracking.",
                isSystem = false,
                isEnabled = false
            ),
            // User Services - Group 2 (icon RedNet)
            ServiceMockState(
                "com.google.android.icon",
                "Configures to android icon and rednet for android service.",
                isSystem = false, // Will group in Group 2
                isEnabled = true
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = diagnosticBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Services",
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
                .padding(horizontal = 16.dp)
        ) {
            // Success Alert Banner
            AnimatedVisibility(
                visible = toastMessage != null,
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
                            text = toastMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Section: System Services
                item {
                    ServiceSectionHeader(
                        title = "System",
                        isCollapsed = isSystemCollapsed,
                        onBg = onBg,
                        onToggle = { isSystemCollapsed = !isSystemCollapsed }
                    )
                }

                if (!isSystemCollapsed) {
                    val systemServices = servicesList.filter { it.isSystem }
                    items(systemServices.size) { idx ->
                        val service = systemServices[idx]
                        ServiceItemRow(
                            service = service,
                            cardBg = cardBg,
                            onBg = onBg,
                            outlineColor = outlineColor,
                            themeAccent = themeAccent,
                            onToggle = { isChecked ->
                                val realIdx = servicesList.indexOf(service)
                                if (realIdx != -1) {
                                    servicesList[realIdx] = service.copy(isEnabled = isChecked)
                                    toastMessage = if (isChecked) {
                                        "Started system hook ${service.serviceName}"
                                    } else {
                                        "Stopped system hook ${service.serviceName}"
                                    }
                                    coroutineScope.launch {
                                        delay(3500)
                                        toastMessage = null
                                    }
                                }
                            }
                        )
                    }
                }

                // Section: User Services Group 1 (manit, location)
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    ServiceSectionHeader(
                        title = "User services",
                        isCollapsed = isUserGroup1Collapsed,
                        onBg = onBg,
                        onToggle = { isUserGroup1Collapsed = !isUserGroup1Collapsed }
                    )
                }

                if (!isUserGroup1Collapsed) {
                    val userServicesGrp1 = servicesList.filter { !it.isSystem && it.serviceName != "com.google.android.icon" }
                    items(userServicesGrp1.size) { idx ->
                        val service = userServicesGrp1[idx]
                        ServiceItemRow(
                            service = service,
                            cardBg = cardBg,
                            onBg = onBg,
                            outlineColor = outlineColor,
                            themeAccent = themeAccent,
                            onToggle = { isChecked ->
                                val realIdx = servicesList.indexOf(service)
                                if (realIdx != -1) {
                                    servicesList[realIdx] = service.copy(isEnabled = isChecked)
                                    toastMessage = if (isChecked) {
                                        "Loaded custom user daemon ${service.serviceName}"
                                    } else {
                                        "De-allocated custom daemon ${service.serviceName}"
                                    }
                                    coroutineScope.launch {
                                        delay(3500)
                                        toastMessage = null
                                    }
                                }
                            }
                        )
                    }
                }

                // Section: User Services Group 2 (icon rednet toggler)
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    ServiceSectionHeader(
                        title = "User services",
                        isCollapsed = isUserGroup2Collapsed,
                        onBg = onBg,
                        onToggle = { isUserGroup2Collapsed = !isUserGroup2Collapsed }
                    )
                }

                if (!isUserGroup2Collapsed) {
                    val userServicesGrp2 = servicesList.filter { !it.isSystem && it.serviceName == "com.google.android.icon" }
                    items(userServicesGrp2.size) { idx ->
                        val service = userServicesGrp2[idx]
                        ServiceItemRow(
                            service = service,
                            cardBg = cardBg,
                            onBg = onBg,
                            outlineColor = outlineColor,
                            themeAccent = themeAccent,
                            onToggle = { isChecked ->
                                val realIdx = servicesList.indexOf(service)
                                if (realIdx != -1) {
                                    servicesList[realIdx] = service.copy(isEnabled = isChecked)
                                    toastMessage = if (isChecked) {
                                        "Started icon manager: ${service.serviceName}"
                                    } else {
                                        "Disabled icon manager: ${service.serviceName}"
                                    }
                                    coroutineScope.launch {
                                        delay(3500)
                                        toastMessage = null
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceSectionHeader(
    title: String,
    isCollapsed: Boolean,
    onBg: Color,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = onBg
        )
        Icon(
            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = onBg.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ServiceItemRow(
    service: ServiceMockState,
    cardBg: Color,
    onBg: Color,
    outlineColor: Color,
    themeAccent: Color,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, outlineColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service texts
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.serviceName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBg
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.description,
                    fontSize = 11.sp,
                    color = onBg.copy(alpha = 0.5f),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Switch Toggler
            Switch(
                checked = service.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00ACC1), // Soft Cyan theme
                    uncheckedThumbColor = onBg.copy(alpha = 0.3f),
                    uncheckedTrackColor = onBg.copy(alpha = 0.08f),
                    uncheckedBorderColor = onBg.copy(alpha = 0.15f)
                )
            )
        }
    }
}
