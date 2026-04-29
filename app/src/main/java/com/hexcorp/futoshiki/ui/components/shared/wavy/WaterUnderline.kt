package com.hexcorp.futoshiki.ui.components.shared.wavy

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

@Composable
fun WaterUnderline(width: Dp, height: Dp, modifier: Modifier, accent: Color) {
    val transition = rememberInfiniteTransition(label = "water")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "water_phase"
    )

    Canvas(modifier = modifier.size(width, height).clipToBounds()) {
        val w = size.width
        val h = size.height
        val segW = w / 5f
        val period = segW * 2f

        // Two offset wave layers for a sense of water depth
        val layers = listOf(
            Triple(0f,          0.42f, Pair(0.7f, 3f)),
            Triple(period * 0.5f, 0.56f, Pair(0.3f, 4.5f)),
        )
        for ((phaseShift, midYRatio, style) in layers) {
            val (alpha, strokeW) = style
            // Start one full period earlier so the left edge is always covered,
            // regardless of phaseShift. The wave tiles seamlessly since offsetX
            // shifts by exactly one period per animation loop.
            val startX = -(phase * period) + phaseShift - period
            val midY = h * midYRatio

            val path = Path()
            path.moveTo(startX, midY)
            val count = (w / segW).toInt() + 6
            for (i in 0 until count) {
                val x = startX + i * segW
                val ctrlX = x + segW * 0.5f
                val ctrlY = if (i % 2 == 0) h * 0.04f else h * 0.96f
                path.quadraticTo(ctrlX, ctrlY, x + segW, midY)
            }
            drawPath(path, accent.copy(alpha = alpha), style = Stroke(width = strokeW, cap = StrokeCap.Round))
        }
    }
}
