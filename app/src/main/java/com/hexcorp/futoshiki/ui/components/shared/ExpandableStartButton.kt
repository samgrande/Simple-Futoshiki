package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.hexcorp.futoshiki.game.Difficulty
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.Yuji
import com.hexcorp.futoshiki.ui.theme.accentColor
import com.hexcorp.futoshiki.audio.Sound
import com.hexcorp.futoshiki.audio.SoundManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableStartButton(
    label: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    selectedSize: Int,
    onSizeSelected: (Int) -> Unit,
    selectedDifficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    onStart: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onDifficultySave: ((Difficulty) -> Unit)? = null,
    currentSize: Int = 4,
    currentDifficulty: Difficulty = Difficulty.EASY,
    isSmallScreen: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    
    val accent = accentColor()
    
    val normalBg = if (isDark) Color.White else Color.Black
    val expandedBg = if (isDark) Color(0xFF0B0B0B) else Color(0xFFF5F2F2)
    val quickStartBg = accent
    
    val normalText = if (isDark) Color.Black else Color.White
    val expandedText = if (isDark) Color.White else Color.Black
    val quickStartText = if (isDark) Color.Black else Color.White

    var swipeProgress by remember { mutableFloatStateOf(0f) }
    var dragFromRight by remember { mutableStateOf(false) }
    val displayProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val widthFraction by animateFloatAsState(
        targetValue = if (isExpanded) 1.0f else 0.9f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "widthFraction"
    )

    val strokeColor = if (isDark) Color.White else Color.Black
    
    val swipeBorderColor by animateColorAsState(
        targetValue = if (swipeProgress > 0f) accent.copy(alpha = (swipeProgress * 0.6f).coerceIn(0f, 1f)) else strokeColor,
        animationSpec = tween(100),
        label = "borderColor"
    )

    val textCrossfadeProgress by animateFloatAsState(
        targetValue = if (displayProgress.value > 0.5f) 1f else 0f,
        animationSpec = tween(100),
        label = "textCrossfade"
    )

    val textColor = lerp(normalText, quickStartText, displayProgress.value)

    val baseModifier = modifier.fillMaxWidth(widthFraction)

    val finalModifier = if (!isExpanded) {
        baseModifier
            .pointerInput(Unit) {
                var totalDragX = 0f
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        dragFromRight = false
                    },
                    onDragEnd = {
                        val threshold = size.width * 0.9f
                        if (totalDragX > threshold || totalDragX < -threshold) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStart()
                        }
                        totalDragX = 0f
                        dragFromRight = false
                        swipeProgress = 0f
                        scope.launch { displayProgress.animateTo(0f, tween(200)) }
                    },
                    onDragCancel = {
                        totalDragX = 0f
                        dragFromRight = false
                        swipeProgress = 0f
                        scope.launch { displayProgress.animateTo(0f, tween(200)) }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        dragFromRight = totalDragX < 0
                        val raw = (abs(totalDragX) / size.width).coerceIn(0f, 1f)
                        swipeProgress = raw
                        scope.launch { displayProgress.snapTo(raw) }
                    }
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    SoundManager.play(Sound.BUTTON)
                    onExpandToggle()
                }
            )
    } else {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { /* Block scrim clicks */ }
        )
    }

    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            if (targetState) {
                fadeIn(tween(300, easing = FastOutSlowInEasing))
                    .togetherWith(fadeOut(tween(150)))
                    .using(SizeTransform(clip = false, sizeAnimationSpec = { _, _ ->
                        tween(450, easing = FastOutSlowInEasing)
                    }))
            } else {
                fadeIn(tween(150))
                    .togetherWith(fadeOut(tween(300, easing = FastOutSlowInEasing)))
                    .using(SizeTransform(clip = false, sizeAnimationSpec = { _, _ ->
                        tween(450, easing = FastOutSlowInEasing)
                    }))
            }
        },
        label = "buttonContentTransition",
        modifier = finalModifier
    ) { expanded ->
        if (!expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, swipeBorderColor, RoundedCornerShape(14.dp))
                    .background(normalBg),
                contentAlignment = Alignment.Center
            ) {
                if (!dragFromRight) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxHeight()
                            .fillMaxWidth(displayProgress.value)
                            .background(
                                brush = Brush.horizontalGradient(
                                    0f to quickStartBg,
                                    0.8f to quickStartBg,
                                    1f to quickStartBg.copy(alpha = 0f)
                                )
                            )
                    )
                }
                if (dragFromRight) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxHeight()
                            .fillMaxWidth(displayProgress.value)
                            .background(
                                brush = Brush.horizontalGradient(
                                    0f to quickStartBg.copy(alpha = 0f),
                                    0.2f to quickStartBg,
                                    1f to quickStartBg
                                )
                            )
                    )
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = textColor,
                        fontSize = 18.sp,
                        fontFamily = PixelF,
                        letterSpacing = 1.sp,
                        modifier = Modifier.graphicsLayer { alpha = 1f - textCrossfadeProgress }
                    )
                    Text(
                        text = "START",
                        color = textColor,
                        fontSize = 18.sp,
                        fontFamily = PixelF,
                        letterSpacing = 1.sp,
                        modifier = Modifier.graphicsLayer { alpha = textCrossfadeProgress }
                    )
    }
    }
        } else {
            val density = LocalDensity.current
            var dragOffsetY by remember { mutableFloatStateOf(0f) }
            val dragAnimatable = remember { Animatable(0f) }
            val closeThresholdPx = with(density) { 15.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = if (dragOffsetY != 0f) dragAnimatable.value else 0f
                    }
                    .shadow(if (isSmallScreen) 4.dp else 8.dp, RoundedCornerShape(14.dp))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (kotlin.math.abs(dragOffsetY) > closeThresholdPx) {
                                        onExpandToggle()
                                    }
                                    dragAnimatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
                                    )
                                    dragOffsetY = 0f
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    dragAnimatable.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
                                    )
                                    dragOffsetY = 0f
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetY = (dragOffsetY + dragAmount * 0.05f)
                                    .coerceIn(-closeThresholdPx * 2f, closeThresholdPx * 2f)
                                scope.launch { dragAnimatable.snapTo(dragOffsetY) }
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, strokeColor, RoundedCornerShape(14.dp))
                        .background(expandedBg)
                        .padding(top = if (isSmallScreen) 18.dp else 25.dp, bottom = if (isSmallScreen) 18.dp else 25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Text(
                    text = "GRID SIZE",
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.65f),
                    fontSize = 11.5.sp,
                    fontFamily = PixelF,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.5.sp,
                        modifier = Modifier.padding(bottom = if (isSmallScreen) 6.dp else 10.dp),
                        textAlign = TextAlign.Center
                    )

                    SizeSlider(
                        selectedSize = selectedSize,
                        onSizeSelected = onSizeSelected,
                        isDark = isDark,
                        currentSize = currentSize,
                        isSmallScreen = isSmallScreen
                    )

                    Spacer(Modifier.height(if (isSmallScreen) 12.dp else 22.dp))

                    Text(
                        text = "DIFFICULTY",
                        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.65f),
                        fontSize = 11.5.sp,
                        fontFamily = PixelF,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.5.sp,
                        modifier = Modifier.padding(bottom = if (isSmallScreen) 6.dp else 10.dp),
                        textAlign = TextAlign.Center
                    )

                    val difficulties = Difficulty.entries
                    var diffDragAccum by remember { mutableFloatStateOf(0f) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .pointerInput(difficulties) {
                                detectHorizontalDragGestures(
                                    onDragStart = { diffDragAccum = 0f },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        diffDragAccum += dragAmount
                                        val threshold = 60f
                                        val cur = selectedDifficulty.ordinal
                                        if (diffDragAccum > threshold && cur > 0) {
                                            onDifficultyChange(difficulties[cur - 1])
                                            SoundManager.play(Sound.TAP)
                                            diffDragAccum = 0f
                                        } else if (diffDragAccum < -threshold && cur < difficulties.size - 1) {
                                            onDifficultyChange(difficulties[cur + 1])
                                            SoundManager.play(Sound.TAP)
                                            diffDragAccum = 0f
                                        }
                                    },
                                    onDragEnd = { diffDragAccum = 0f },
                                    onDragCancel = { diffDragAccum = 0f }
                                )
                            },
                        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 14.dp)
                    ) {
                        difficulties.forEach { difficulty ->
                            Box(modifier = Modifier.weight(1f)) {
                                DifficultyCard(
                                    difficulty = difficulty,
                                    isSelected = selectedDifficulty == difficulty,
                                    onClick = { onDifficultyChange(difficulty) },
                                    isDark = isDark,
                                    isSmallScreen = isSmallScreen
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(if (isSmallScreen) 12.dp else 25.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(60.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color.White else Color.Black)
                            .clickable(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStart()
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "START",
                            color = if (isDark) Color(0xFF0B0B0B) else Color(0xFFF5F2F2),
                            fontSize = if (isSmallScreen) 16.sp else 18.sp,
                            fontFamily = PixelF,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}
  
@Composable
fun SizeSlider(
    selectedSize: Int,
    onSizeSelected: (Int) -> Unit,
    isDark: Boolean,
    currentSize: Int = selectedSize,
    isSmallScreen: Boolean = false
) {
    val containerBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8E8E8)
    val accent = accentColor()
    val options = listOf(4, 5, 6)
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val textColor by animateColorAsState(
        targetValue = if (isDark) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )

    var totalWidthPx by remember { mutableIntStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }
    var animateOnNextChange by remember { mutableStateOf(false) }
    val thumbAnimatable = remember { Animatable(0f) }
    val selectedIndex = options.indexOf(selectedSize)
    val itemWidthPx = if (totalWidthPx > 0) totalWidthPx.toFloat() / options.size else 0f

    LaunchedEffect(selectedSize, totalWidthPx) {
        if (itemWidthPx > 0 && !isDragging) {
            val targetPx = itemWidthPx * selectedIndex
            if (animateOnNextChange) {
                thumbAnimatable.animateTo(
                    targetValue = targetPx,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                )
            } else {
                thumbAnimatable.snapTo(targetPx)
                animateOnNextChange = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(if (isSmallScreen) 34.dp else 40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerBg)
            .padding(2.dp)
            .onSizeChanged { totalWidthPx = it.width }
            .pointerInput(itemWidthPx) {
                if (itemWidthPx <= 0f) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newX = (thumbAnimatable.value + dragAmount)
                            .coerceIn(0f, totalWidthPx.toFloat() - itemWidthPx)
                        scope.launch { thumbAnimatable.snapTo(newX) }
                    },
                    onDragEnd = {
                        isDragging = false
                        val snappedIdx = (thumbAnimatable.value / itemWidthPx)
                            .roundToInt()
                            .coerceIn(0, options.size - 1)
                        val newSize = options[snappedIdx]
                        if (newSize != selectedSize) {
                            onSizeSelected(newSize)
                            SoundManager.play(Sound.SWIPE)
                        }
                        val targetPx = snappedIdx * itemWidthPx
                        scope.launch {
                            thumbAnimatable.animateTo(
                                targetValue = targetPx,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                            )
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        val idx = options.indexOf(selectedSize)
                        val targetPx = idx * itemWidthPx
                        scope.launch {
                            thumbAnimatable.animateTo(
                                targetValue = targetPx,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                            )
                        }
                    }
                )
            }
    ) {
        if (itemWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbAnimatable.value.roundToInt(), 0) }
                    .width(with(density) { itemWidthPx.toDp() })
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent)
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEach { size ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                SoundManager.play(Sound.SWIPE)
                                onSizeSelected(size)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$size x $size",
                        color = textColor,
                        fontSize = 15.sp,
                        fontFamily = PixelF,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DifficultyCard(
    difficulty: Difficulty,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean,
    isSmallScreen: Boolean = false
) {
    val accent = accentColor()
    val unselectedBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8E8E8)
    val unselectedBorder = if (isDark) Color(0xFF2A2A2A) else Color(0xFFD0D0D0)
    val targetBg = if (isSelected) accent else unselectedBg
    
    val cardBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 300),
        label = "cardBg"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isDark) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )
    
    val kanji = when (difficulty) {
        Difficulty.EASY -> "易"
        Difficulty.MEDIUM -> "普"
        Difficulty.HARD -> "難"
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(10.dp))
                .background(cardBg)
                .then(
                    if (!isSelected) Modifier.border(1.dp, unselectedBorder, RoundedCornerShape(10.dp))
                    else Modifier
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        SoundManager.play(Sound.TAP)
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = kanji,
                color = textColor,
                fontSize = if (isSmallScreen) 34.sp else 40.sp,
                textAlign = TextAlign.Center,
                fontFamily = Yuji,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
        
        Spacer(Modifier.height(if (isSmallScreen) 6.dp else 8.dp))

        Text(
            text = difficulty.name,
            color = textColor.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontFamily = PixelF,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}
