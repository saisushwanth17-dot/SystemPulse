package com.example.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedProgressBar(
    value: Float, // 0f to 100f
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    segments: Int = 10,
    segmentSpacerWidth: Dp = 2.dp,
    lowColor: Color = Color(0xFF00E676),
    mediumColor: Color = Color(0xFFFF9100),
    highColor: Color = Color(0xFFFF1744)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = value.coerceIn(0f, 100f) / 100f,
        animationSpec = spring(stiffness = 100f),
        label = "progress"
    )

    val activeColor = when {
        value < 70f -> lowColor
        value < 90f -> mediumColor
        else -> highColor
    }

    val trackColor = MaterialTheme.colorScheme.surfaceContainer

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val spacerWidthPx = segmentSpacerWidth.toPx()
        val totalSpacerWidth = (segments - 1) * spacerWidthPx
        val segmentWidth = (width - totalSpacerWidth) / segments

        // Draw segmented track
        for (i in 0 until segments) {
            val startX = i * (segmentWidth + spacerWidthPx)
            drawRect(
                color = trackColor,
                topLeft = Offset(startX, 0f),
                size = Size(segmentWidth, canvasHeight)
            )
        }

        // Draw segmented progress on top using clipRect to mask the exact progress level
        clipRect(right = width * animatedProgress) {
            for (i in 0 until segments) {
                val startX = i * (segmentWidth + spacerWidthPx)
                drawRect(
                    color = activeColor,
                    topLeft = Offset(startX, 0f),
                    size = Size(segmentWidth, canvasHeight)
                )
            }
        }
    }
}
