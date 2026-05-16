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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WoodUnderline(width: Dp, height: Dp, modifier: Modifier, accent: Color) {
    val transition = rememberInfiniteTransition(label = "wood")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "wood_phase"
    )

    Canvas(modifier = modifier.size(width, height).clipToBounds()) {
        val w = size.width
        val h = size.height
        val segW = w / 8f
        val period = segW * 2f
        val offsetX = -(phase * period)

        // Whole line gently bobs up and down like branches in a breeze
        val sway = sin(phase * 2f * PI.toFloat()) * h * 0.1f
        val baseY = h * 0.6f + sway

        val path = Path()
        path.moveTo(offsetX, baseY)
        val count = (w / segW).toInt() + 5
        for (i in 0 until count) {
            val x = offsetX + i * segW
            val ctrlX = x + segW * 0.5f
            // Use absolute x position (not segment index i) so peakScale is the same
            // for a given canvas position regardless of phase — this makes the loop seamless.
            val peakScale = 0.4f + 0.55f * abs(sin(x * 1.3f / segW))
            val ctrlY = if (i % 2 == 0) h * (0.05f + (1f - peakScale) * 0.25f) else h * 0.95f
            path.quadraticTo(ctrlX, ctrlY, x + segW, baseY)
        }
        drawPath(path, accent.copy(alpha = 0.65f), style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Leaf particles drifting leftward above the wave
        val leafCount = 7
        for (i in 0 until leafCount) {
            val t = ((i.toFloat() / leafCount) + phase) % 1f
            val leafX = t * (w + segW) - segW * 0.5f
            val leafY = h * 0.1f + sin(i * 2.3f + phase * 2f * PI.toFloat()) * h * 0.18f
            drawCircle(
                color = accent.copy(alpha = 0.38f),
                radius = 3f,
                center = Offset(leafX, leafY)
            )
        }
    }
}
