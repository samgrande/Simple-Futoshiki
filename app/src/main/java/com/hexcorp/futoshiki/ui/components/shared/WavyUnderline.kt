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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.hexcorp.futoshiki.ui.theme.accentColor

@Composable
fun WavyUnderline(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val accent = accentColor()
    Canvas(
        modifier = modifier
            .size(width, height)
            .clipToBounds()
    ) {
        val w = size.width
        val h = size.height
        val midY = h / 2f
        val segW = w / 7f
        val period = segW * 2f
        val offsetX = -phase * period

        val path = Path()
        path.moveTo(offsetX, midY)

        val segmentsNeeded = (w / segW).toInt() + 4
        for (i in 0 until segmentsNeeded) {
            val currentSegX = offsetX + i * segW
            val ctrlX = currentSegX + segW * 0.5f
            val ctrlY = if (i % 2 == 0) 0f else h
            val endX  = currentSegX + segW
            path.quadraticTo(ctrlX, ctrlY, endX, midY)
        }

        drawPath(
            path = path,
            color = accent.copy(alpha = 0.5f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
    }
}
