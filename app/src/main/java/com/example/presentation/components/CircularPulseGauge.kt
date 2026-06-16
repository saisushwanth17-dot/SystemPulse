package com.example.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularPulseGauge(
    value: Float, // 0f to 100f
    title: String,
    metricText: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    glowOpacity: Float = 0.25f
) {
    // Smooth transition between values using spring physics rather than a stiff jump
    val animatedProgress by animateFloatAsState(
        targetValue = value.coerceIn(0f, 100f) / 100f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 150f),
        label = "gauge_progress"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Draw custom arc, glowing background effect, and pointer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizeMin = size.minDimension
            val radius = (sizeMin - strokeWidth.toPx()) / 2f
            val center = size.center

            // Draw track
            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Draw ambient glowing brush underneath
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = glowOpacity), Color.Transparent),
                        center = center,
                        radius = radius * 1.2f
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = true
                )

                // Draw active filled progress arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(accentColor.copy(alpha = 0.6f), accentColor),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Inner textual status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = metricText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )
        }
    }
}
