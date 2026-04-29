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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun FireUnderline(width: Dp, height: Dp, modifier: Modifier, accent: Color) {
    val transition = rememberInfiniteTransition(label = "fire")

    // Slow rolling base — heavy, rage-like
    val slowPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "slow"
    )
    // Mid flame bodies
    val midPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "mid"
    )
    // Slow rage pulse — fire breathes, not flickers
    val pulse by transition.animateFloat(
        initialValue = 0.55f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Canvas(modifier = modifier.size(width, height).clipToBounds()) {
        val w = size.width
        val h = size.height
        val baseY = h * 0.72f
        val steps = 80

        // Layer 1: Wide glow base — slow, smoldering roll
        run {
            val path = Path()
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val x = t * w
                val v = abs(sin(t * 4f * PI.toFloat() - slowPhase * 2f * PI.toFloat()))
                val y = baseY - h * 0.28f * v
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, accent.copy(alpha = pulse * 0.30f), style = Stroke(width = 7f, cap = StrokeCap.Round))
        }

        // Layer 2: Main flame bodies — medium height, smooth arches
        run {
            val path = Path()
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val x = t * w
                val v = abs(sin(t * 6f * PI.toFloat() - midPhase * 2f * PI.toFloat()))
                val y = baseY - h * 0.58f * v
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, accent.copy(alpha = pulse * 0.65f), style = Stroke(width = 3f, cap = StrokeCap.Round))
        }

        // Layer 3: Flame tips — tall, sharp points (squared to narrow the peaks)
        run {
            val path = Path()
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val x = t * w
                val raw = abs(sin(t * 7f * PI.toFloat() - midPhase * 2f * 2f * PI.toFloat()))
                val v = raw * raw  // squaring sharpens the tip
                val y = baseY - h * 0.90f * v
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, accent.copy(alpha = pulse * 0.88f), style = Stroke(width = 2f, cap = StrokeCap.Round))
        }
    }
}
