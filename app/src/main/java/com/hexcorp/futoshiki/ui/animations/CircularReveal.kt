package com.hexcorp.futoshiki.ui.animations

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Size
import kotlin.math.hypot

class CircularRevealShape(
    private val progress: Float,
    private val center: Offset = Offset.Unspecified
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val actualCenter = if (center == Offset.Unspecified) {
            Offset(size.width / 2, size.height / 2)
        } else {
            center
        }

        // Calculate the maximum possible radius to cover the entire screen
        val maxRadius = hypot(
            maxOf(actualCenter.x, size.width - actualCenter.x),
            maxOf(actualCenter.y, size.height - actualCenter.y)
        )
        
        val radius = maxRadius * progress

        return Outline.Generic(Path().apply {
            addOval(Rect(actualCenter, radius))
        })
    }
}
