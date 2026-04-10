package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class RainDrop(
    val x: Float,
    val yOffset: Float,
    val speed: Float,
    val length: Float,
    val alpha: Float
)

@Composable
fun RainBackground(
    buttonBounds: Rect?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val drops = remember {
        List(100) {
            RainDrop(
                x = Random.nextFloat(),
                yOffset = Random.nextFloat(),
                speed = Random.nextFloat() * 1.2f + 1.8f,
                length = Random.nextFloat() * 40f + 40f,
                alpha = Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val slantX = -20f

        drops.forEach { drop ->
            val totalTravel = h + drop.length
            val yPos = ((drop.yOffset + progress * drop.speed) % 1f) * totalTravel - drop.length
            val xPos = (drop.x * (w + 40f)) - 20f + (yPos / h) * slantX

            var collisionY: Float? = null
            var showSplash = false
            val checkX = xPos
            val checkYEnd = yPos + drop.length

            if (buttonBounds != null && checkX >= buttonBounds.left && checkX <= buttonBounds.right) {
                if (checkYEnd >= buttonBounds.top) {
                    collisionY = buttonBounds.top
                    val edgeWidth = buttonBounds.width * 0.15f
                    if (checkX >= buttonBounds.left + edgeWidth && checkX <= buttonBounds.right - edgeWidth) {
                        if (yPos < buttonBounds.top) showSplash = true
                    }
                }
            }

            if (collisionY == null && checkYEnd >= h) {
                collisionY = h
                if (yPos < h) showSplash = true
            }

            if (collisionY != null) {
                if (yPos < collisionY) {
                    val visibleLength = (collisionY - yPos).coerceAtMost(drop.length)
                    drawLine(
                        color = Color.White.copy(alpha = drop.alpha),
                        start = Offset(xPos, yPos),
                        end = Offset(xPos + (visibleLength / 150f) * slantX, yPos + visibleLength),
                        strokeWidth = 2.5f
                    )
                }
                if (showSplash) {
                    val splashW = 10f
                    drawLine(
                        color = Color.White.copy(alpha = drop.alpha * 1.5f),
                        start = Offset(xPos - splashW, collisionY - 1f),
                        end = Offset(xPos + splashW, collisionY - 1f),
                        strokeWidth = 2f
                    )
                }
            } else if (yPos < h) {
                drawLine(
                    color = Color.White.copy(alpha = drop.alpha),
                    start = Offset(xPos, yPos),
                    end = Offset(xPos + (drop.length / 150f) * slantX, yPos + drop.length),
                    strokeWidth = 2.5f
                )
            }
        }
    }
}
