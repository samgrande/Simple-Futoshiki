package com.hexcorp.futoshiki.ui.components.shared

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
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.LocalAppTheme
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun WavyUnderline(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    val theme = LocalAppTheme.current
    val accent = accentColor()
    when (theme) {
        AppTheme.FIRE  -> FireUnderline(width, height, modifier, accent)
        AppTheme.WATER -> WaterUnderline(width, height, modifier, accent)
        AppTheme.WOOD  -> WoodUnderline(width, height, modifier, accent)
        AppTheme.EARTH -> EarthUnderline(width, height, modifier, accent)
    }
}

// ── Fire: slow rage — deep rolling flames with smooth arching bodies ──────────

@Composable
private fun FireUnderline(width: Dp, height: Dp, modifier: Modifier, accent: Color) {
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

// ── Water: slow, smooth, layered flow rippling forward ───────────────────────

@Composable
private fun WaterUnderline(width: Dp, height: Dp, modifier: Modifier, accent: Color) {
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

// ── Wood: organic canopy sway with drifting leaf particles ───────────────────

@Composable
private fun WoodUnderline(width: Dp, height: Dp, modifier: Modifier, accent: Color) {
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

// ── Earth: slow strata layers shifting like soil sediment ────────────────────

@Composable
private fun EarthUnderline(width: Dp, height: Dp, modifier: Modifier, accent: Color) {
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
