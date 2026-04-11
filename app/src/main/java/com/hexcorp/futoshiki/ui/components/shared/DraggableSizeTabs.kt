package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val SIZES = listOf(4, 5, 6)

@Composable
private fun SizeLabel(
    size: Int,
    color: Color,
    fontWeight: FontWeight
) {
    Text(
        text = "$size x $size",
        color = color,
        fontSize = 16.sp,
        fontWeight = fontWeight,
        fontFamily = ReemKufi
    )
}

@Composable
fun DraggableSizeTabs(
    currentSize: Int,
    onSizeChange: (Int) -> Unit,
    height: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var trackWidthPx by remember { mutableIntStateOf(0) }
    val thumbPadPx = with(density) { 4.dp.toPx() }

    val sw = if (trackWidthPx > 0) trackWidthPx / 3f else 0f

    val thumbX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val updatedOnSizeChange by rememberUpdatedState(onSizeChange)
    val updatedCurrentSize by rememberUpdatedState(currentSize)

    val isDark = LocalIsDark.current
    val strokeColor = if (isDark) Color(0xFF101010).copy(alpha = 0.3f) else Color.Black

    LaunchedEffect(currentSize, sw) {
        if (sw > 0 && !isDragging) {
            val idx = SIZES.indexOf(currentSize).coerceAtLeast(0)
            val target = idx * sw + thumbPadPx
            thumbX.animateTo(
                targetValue = target,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .onSizeChanged { size ->
                if (size.width > 0) trackWidthPx = size.width
            }
            .border(2.dp, strokeColor, RoundedCornerShape(11.dp))
            .clip(RoundedCornerShape(11.dp))
            .background(accentColor())
            .pointerInput(sw, trackWidthPx) {
                if (sw <= 0f) return@pointerInput
                coroutineScope {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            launch {
                                val nextX = (thumbX.value + dragAmount).coerceIn(
                                    thumbPadPx,
                                    trackWidthPx - sw + thumbPadPx
                                )
                                thumbX.snapTo(nextX)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            val relativeX = thumbX.value - thumbPadPx
                            val snappedIdx = (relativeX / sw).roundToInt().coerceIn(0, 2)
                            val targetX = snappedIdx * sw + thumbPadPx
                            val newSize = SIZES[snappedIdx]
                            if (newSize != updatedCurrentSize) updatedOnSizeChange(newSize)
                            launch {
                                thumbX.animateTo(
                                    targetValue = targetX,
                                    animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                                )
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            val idx = SIZES.indexOf(updatedCurrentSize).coerceAtLeast(0)
                            val targetX = idx * sw + thumbPadPx
                            launch {
                                thumbX.animateTo(
                                    targetValue = targetX,
                                    animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                                )
                            }
                        }
                    )
                }
            }
    ) {
        // 1. Background Labels
        Row(modifier = Modifier.fillMaxSize()) {
            SIZES.forEach { s ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    SizeLabel(s, FutoshikiColors.tabText(), FontWeight.Medium)
                }
            }
        }

        // 2. Thumb & 3. Inverted Labels
        if (trackWidthPx > 0) {
            val thumbWidthPx = sw - thumbPadPx * 2
            val vPadPx = with(density) { 4.dp.toPx() }
            val cornerRadiusPx = with(density) { 8.dp.toPx() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbX.value.roundToInt(), 0) }
                    .padding(vertical = 4.dp)
                    .width(with(density) { thumbWidthPx.toDp() })
                    .fillMaxHeight()
                    .border(2.dp, strokeColor, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF101010) else FutoshikiColors.surface())
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = true
                        shape = object : Shape {
                            override fun createOutline(
                                size: Size,
                                layoutDirection: LayoutDirection,
                                density: Density
                            ): Outline {
                                return Outline.Rounded(
                                    RoundRect(
                                        left = thumbX.value,
                                        top = vPadPx,
                                        right = thumbX.value + thumbWidthPx,
                                        bottom = size.height - vPadPx,
                                        cornerRadius = CornerRadius(cornerRadiusPx)
                                    )
                                )
                            }
                        }
                    }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    val selectedTextColor = FutoshikiColors.onSurface()
                    SIZES.forEach { s ->
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            SizeLabel(s, selectedTextColor, FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Interaction layer (Clicks)
        Row(modifier = Modifier.fillMaxSize()) {
            SIZES.forEach { s ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isDragging && s != currentSize) updatedOnSizeChange(s)
                        }
                )
            }
        }
    }
}
