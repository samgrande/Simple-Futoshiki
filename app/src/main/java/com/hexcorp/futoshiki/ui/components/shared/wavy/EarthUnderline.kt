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
fun EarthUnderline(width: Dp, height: Dp, modifier: Modifier, accent: Color) {
    val transition = rememberInfiniteTransition(label = "earth")
    // Each layer has its own independent animation so it completes exactly one
    // period per loop — fractional speed multipliers on a shared phase caused each
    // layer to shift by a non-integer number of periods, making the restart visible.
    val phaseTop by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6400, easing = LinearEasing), RepeatMode.Restart),
        label = "earth_top"
    )
    val phaseMid by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4267, easing = LinearEasing), RepeatMode.Restart),
        label = "earth_mid"
    )
    val phaseBot by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "earth_bot"
    )

    Canvas(modifier = modifier.size(width, height).clipToBounds()) {
        val w = size.width
        val h = size.height
        val segW = w / 4f   // Wide, slow segments for a heavy, earthy feel
        val period = segW * 2f
        val amp = h * 0.18f // Low amplitude — soil doesn't rush

        // Three strata lines at different depths, each sliding at a different speed
        val strata = listOf(
            Triple(0.22f, 0.60f, phaseTop),  // top layer   — slowest
            Triple(0.50f, 0.42f, phaseMid),  // middle layer
            Triple(0.78f, 0.28f, phaseBot),  // bottom layer — fastest (deepest flow)
        )
        for ((yRatio, alpha, layerPhase) in strata) {
            val offsetX = -(layerPhase * period)
            val midY = h * yRatio

            val path = Path()
            path.moveTo(offsetX, midY)
            val count = (w / segW).toInt() + 4
            for (i in 0 until count) {
                val x = offsetX + i * segW
                val ctrlX = x + segW * 0.5f
                val ctrlY = if (i % 2 == 0) midY - amp else midY + amp
                path.quadraticTo(ctrlX, ctrlY, x + segW, midY)
            }
            drawPath(path, accent.copy(alpha = alpha), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
        }
    }
}
