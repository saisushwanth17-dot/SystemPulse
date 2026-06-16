package com.example.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RealTimeSparkline(
    history: List<Float>, // Values from 0f to 100f
    lineColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    strokeWidth: Dp = 2.dp,
    fillOpacity: Float = 0.15f
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (history.size < 2) return@Canvas

        val list = history.takeLast(60) // Show last 60 points
        val maxPoints = list.size
        val canvasWidth = size.width
        val canvasHeight = size.height

        val stepX = canvasWidth / (maxPoints - 1)
        val path = Path()
        val fillPath = Path()

        // Normalize points cleanly
        fun getY(value: Float): Float {
            val clamped = value.coerceIn(0f, 100f)
            return canvasHeight - (clamped / 100f * canvasHeight)
        }

        // Initialize path
        path.moveTo(0f, getY(list[0]))
        fillPath.moveTo(0f, canvasHeight)
        fillPath.lineTo(0f, getY(list[0]))

        // Join coordinates
        for (i in 1 until maxPoints) {
            val cx = i * stepX
            val cy = getY(list[i])
            path.lineTo(cx, cy)
            fillPath.lineTo(cx, cy)
        }

        fillPath.lineTo(canvasWidth, canvasHeight)
        fillPath.close()

        // Draw background gradient shadow
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = fillOpacity), Color.Transparent),
                startY = 0f,
                endY = canvasHeight
            )
        )

        // Draw active trend line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}
