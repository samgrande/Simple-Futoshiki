package com.hexcorp.futoshiki.ui.screens.theming

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun ThemeModeSlider(
    currentTheme: AppTheme,
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val modes = ThemeMode.entries.toTypedArray()

    var width by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Dragging state
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
    // Snap animation
    val animatableOffset = remember { Animatable(currentMode.ordinal.toFloat()) }
    
    // Rotation animation when dragging
    val rotation by animateFloatAsState(
        targetValue = if (isDragging) 360f else 0f,
        animationSpec = if (isDragging) {
            infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart)
        } else {
            tween(300)
        },
        label = "shurikenRotation"
    )

    // Keep animatable in sync with currentMode when not dragging
    LaunchedEffect(currentMode) {
        if (!isDragging) {
            animatableOffset.animateTo(currentMode.ordinal.toFloat(), tween(300))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                // Consume all horizontal drags within the slider so they don't
                // propagate to any parent gesture handler.
                detectHorizontalDragGestures { change, _ -> change.consume() }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            modes.forEach { mode ->
                val isSelected = if (isDragging) {
                    val sectionWidth = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 1f
                    (dragOffset / sectionWidth).roundToInt().coerceIn(0, modes.size - 1) == mode.ordinal
                } else {
                    mode == currentMode
                }

                Text(
                    text = mode.name,
                    fontSize = 9.sp,
                    fontFamily = ReemKufi,
                    fontWeight = FontWeight.Bold,
                    color = FutoshikiColors.onSurface().copy(alpha = if (isSelected) 1f else 0.4f),
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .width(60.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onModeChange(mode) },
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Slider Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .onGloballyPositioned { width = it.size.width }
                .pointerInput(modes.size) {
                    detectTapGestures { offset ->
                        val sectionWidth = width.toFloat() / (modes.size - 1)
                        val index = (offset.x / sectionWidth).roundToInt().coerceIn(0, modes.size - 1)
                        onModeChange(modes[index])
                    }
                }
                .pointerInput(modes.size) {
                    detectHorizontalDragGestures(
                        onDragStart = { 
                            isDragging = true 
                            val sectionWidth = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 1f
                            dragOffset = animatableOffset.value * sectionWidth
                        },
                        onDragEnd = {
                            isDragging = false
                            val sectionWidth = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 1f
                            val targetIndex = (dragOffset / sectionWidth).roundToInt().coerceIn(0, modes.size - 1)
                            onModeChange(modes[targetIndex])
                            scope.launch {
                                animatableOffset.animateTo(targetIndex.toFloat(), tween(200))
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch {
                                animatableOffset.animateTo(currentMode.ordinal.toFloat(), tween(200))
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        dragOffset = (dragOffset + dragAmount).coerceIn(0f, width.toFloat())
                        val sectionWidth = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 1f
                        scope.launch {
                            animatableOffset.snapTo(dragOffset / sectionWidth)
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Horizontal Line
            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                drawLine(
                    color = if (isDark) Color.White else Color.Gray,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Dots for each section
                val sectionWidth = size.width / (modes.size - 1)
                val accent = when (currentTheme) {
                    AppTheme.FIRE  -> FutoshikiColors.FireAccent
                    AppTheme.WATER -> FutoshikiColors.WaterAccent
                    AppTheme.EARTH -> FutoshikiColors.EarthAccent
                    AppTheme.WOOD  -> FutoshikiColors.WoodAccent
                }
                for (i in 0 until modes.size) {
                    drawCircle(
                        color = accent,
                        radius = 4.dp.toPx(),
                        center = Offset(i * sectionWidth, size.height / 2)
                    )
                }
            }

            // Shuriken Thumb
            val sectionWidthPx = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 0f
            val thumbOffset = (animatableOffset.value * sectionWidthPx).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffset - with(density) { 20.dp.roundToPx() }, 0) }
                    .size(40.dp)
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                // Blurred shadow: 0 offset, dark mode gets darker shadow
                Image(
                    painter = painterResource(id = if (isDark) R.drawable.shuriken_dark else R.drawable.shuriken),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(FutoshikiColors.shadowColor()),
                    modifier = Modifier
                        .size(40.dp)
                        .blur(6.dp)
                )
                Image(
                    painter = painterResource(id = if (isDark) R.drawable.shuriken_dark else R.drawable.shuriken),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
