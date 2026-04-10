package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hexcorp.futoshiki.ui.theme.accentColor

enum class ArrowDirection { UP, RIGHT, DOWN, LEFT }

@Composable
fun ConstraintArrow(
    direction: ArrowDirection,
    sizeDp: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val degrees = when (direction) {
        ArrowDirection.UP    -> 270f
        ArrowDirection.RIGHT -> 0f
        ArrowDirection.DOWN  -> 90f
        ArrowDirection.LEFT  -> 180f
    }

    val accent = accentColor()
    Canvas(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer { rotationZ = degrees }
    ) {
        val scaleX = size.width / 12f
        val scaleY = size.height / 19f
        val scale = minOf(scaleX, scaleY)

        val offsetX = (size.width - (12f * scale)) / 2f
        val offsetY = (size.height - (19f * scale)) / 2f

        val path = Path().apply {
            moveTo(10.4244f * scale + offsetX, 7.61734f * scale + offsetY)
            cubicTo(
                11.4005f * scale + offsetX, 8.59361f * scale + offsetY,
                11.4005f * scale + offsetX, 10.1762f * scale + offsetY,
                10.4244f * scale + offsetX, 11.1525f * scale + offsetY
            )
            lineTo(3.88824f * scale + offsetX, 17.6886f * scale + offsetY)
            cubicTo(
                3.11313f * scale + offsetX, 18.4635f * scale + offsetY,
                1.85671f * scale + offsetX, 18.4635f * scale + offsetY,
                1.0816f * scale + offsetX, 17.6886f * scale + offsetY
            )
            cubicTo(
                0.306442f * scale + offsetX, 16.9135f * scale + offsetY,
                0.306442f * scale + offsetX, 15.6562f * scale + offsetY,
                1.0816f * scale + offsetX, 14.881f * scale + offsetY
            )
            lineTo(5.51715f * scale + offsetX, 10.4455f * scale + offsetY)
            cubicTo(
                6.10287f * scale + offsetX, 9.85969f * scale + offsetY,
                6.10287f * scale + offsetX, 8.91015f * scale + offsetY,
                5.51715f * scale + offsetX, 8.32437f * scale + offsetY
            )
            lineTo(1.0816f * scale + offsetX, 3.88882f * scale + offsetY)
            cubicTo(
                0.306442f * scale + offsetX, 3.11366f * scale + offsetY,
                0.306443f * scale + offsetX, 1.85637f * scale + offsetY,
                1.0816f * scale + offsetX, 1.08121f * scale + offsetY
            )
            cubicTo(
                1.85672f * scale + offsetX, 0.306306f * scale + offsetY,
                3.11313f * scale + offsetX, 0.306306f * scale + offsetY,
                3.88824f * scale + offsetX, 1.08121f * scale + offsetY
            )
            lineTo(10.4244f * scale + offsetX, 7.61734f * scale + offsetY)
            close()
        }

        drawPath(path, color = accent)
        drawPath(path, color = Color.Black, style = Stroke(width = 1.5f * scale))
    }
}
