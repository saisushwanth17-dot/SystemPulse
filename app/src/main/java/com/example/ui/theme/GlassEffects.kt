package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A13)) // Extremely deep cosmic black
            .drawBehind {
                // Subtle glowing organic shapes of deep indigo, violet, and dark teal
                // Deep Indigo glow (Top-left area)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3B3F51B5), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.2f),
                        radius = size.width * 0.9f
                    )
                )
                // Violet / Magenta glow (Right-center area)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2E9C27B0), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.5f),
                        radius = size.width * 0.95f
                    )
                )
                // Dark Teal / Cyan glow (Bottom-left area)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3000ACC1), Color.Transparent),
                        center = Offset(size.width * 0.25f, size.height * 0.85f),
                        radius = size.width * 0.85f
                    )
                )
                // Secondary Deep Blue glow (Bottom-right area)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x281E88E5), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.9f),
                        radius = size.width * 0.6f
                    )
                )
            }
    ) {
        content()
    }
}

fun Modifier.glassmorphic(
    cornerRadius: Dp = 16.dp,
    alpha: Float = 0.08f,
    borderAlphaScale: Float = 1.6f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(Color.White.copy(alpha = alpha))
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f * borderAlphaScale),
                Color.White.copy(alpha = 0.04f * borderAlphaScale)
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
