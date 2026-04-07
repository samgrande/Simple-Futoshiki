package com.hex.futoshiki.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hex.futoshiki.ui.theme.FutoshikiColors
import com.hex.futoshiki.ui.theme.ReemKufi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val SIZES = listOf(4, 5, 6)

@Composable
fun DraggableSizeTabs(
    currentSize: Int,
    onSizeChange: (Int) -> Unit,
    height: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var trackWidthPx by remember { mutableIntStateOf(0) }
    val thumbPadPx = with(density) { 2.dp.toPx() }
    
    val sw = if (trackWidthPx > 0) trackWidthPx / 3f else 0f

    val thumbX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Use updated state to prevent stale captures in pointerInput
    val updatedOnSizeChange by rememberUpdatedState(onSizeChange)
    val updatedCurrentSize by rememberUpdatedState(currentSize)

    // Sync when external state changes or initial measure
    LaunchedEffect(currentSize, sw) {
        if (sw > 0 && !isDragging) {
            val idx = SIZES.indexOf(currentSize).coerceAtLeast(0)
            val target = idx * sw + thumbPadPx
            thumbX.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
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
            .border(1.dp, Color.Black, RoundedCornerShape(11.dp))
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFFF4F4F4))
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
                            if (newSize != updatedCurrentSize) {
                                updatedOnSizeChange(newSize)
                            }
                            
                            launch {
                                thumbX.animateTo(
                                    targetValue = targetX,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
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
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    )
                }
            }
    ) {
        // Thumb pill
        if (trackWidthPx > 0) {
            val thumbWidthPx = sw - thumbPadPx * 2
            val thumbWidthDp = with(density) { thumbWidthPx.toDp() }
            val thumbOffsetDp = with(density) { thumbX.value.toDp() }
            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .offset(x = thumbOffsetDp)
                    .width(thumbWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .background(FutoshikiColors.TabThumb)
            )
        }

        // Labels
        Row(modifier = Modifier.fillMaxSize()) {
            SIZES.forEach { s ->
                val isActive = s == currentSize
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isDragging && s != currentSize) {
                                updatedOnSizeChange(s)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("$s")
                            withStyle(SpanStyle(fontSize = (13 * 1.3f).sp)) { append("×") }
                            append("$s")
                        },
                        color = FutoshikiColors.TabText,
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = ReemKufi
                    )
                }
            }
        }
    }
}
