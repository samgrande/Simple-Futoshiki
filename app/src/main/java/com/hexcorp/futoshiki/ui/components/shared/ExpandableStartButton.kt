package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.game.Difficulty
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.Yuji
import com.hexcorp.futoshiki.ui.theme.accentColor

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
    currentSize: Int = selectedSize,
    currentDifficulty: Difficulty = selectedDifficulty,
    hasActiveGame: Boolean = false,
    isQuickStartMode: Boolean = false,
    onQuickStartModeChange: (Boolean) -> Unit = {},
    isSmallScreen: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    val accent = accentColor()
    
    val normalBg = if (isDark) Color.White else Color.Black
    val expandedBg = if (isDark) Color(0xFF0B0B0B) else Color(0xFFF5F2F2)
    val quickStartBg = accent
    
    val normalText = if (isDark) Color.Black else Color.White
    val expandedText = if (isDark) Color.White else Color.Black
    val quickStartText = if (isDark) Color.Black else Color.White

    var swipeProgress by remember { mutableFloatStateOf(0f) }

    val widthFraction by animateFloatAsState(
        targetValue = if (isExpanded) 1.08f else 0.9f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "widthFraction"
    )

    val modeProgress by animateFloatAsState(
        targetValue = if (isQuickStartMode) 1f else 0f,
        animationSpec = tween(200),
        label = "modeAnim"
    )

    val currentBg by animateColorAsState(
        targetValue = when {
            isExpanded -> expandedBg
            isQuickStartMode -> quickStartBg
            else -> normalBg
        },
        animationSpec = tween(150),
        label = "bgColor"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isExpanded -> expandedText
            isQuickStartMode -> quickStartText
            else -> normalText
        },
        animationSpec = tween(150),
        label = "textColor"
    )
    
    val strokeColor = if (isDark) Color.White else Color.Black
    
    val borderWidth = 2f
    val cornerRadius = 14f
    
    val swipeBorderColor by animateColorAsState(
        targetValue = if (swipeProgress > 0f) accent.copy(swipeProgress * 0.6f) else strokeColor,
        animationSpec = tween(100),
        label = "borderColor"
    )

    val baseModifier = modifier
        .fillMaxWidth(widthFraction)
        .clip(RoundedCornerShape(14.dp))
        .border(
            width = 2.dp,
            color = swipeBorderColor,
            shape = RoundedCornerShape(14.dp)
        )
        .drawBehind {
            drawRect(currentBg)
        }

    val finalModifier = if (!isExpanded) {
        baseModifier
            .pointerInput(isQuickStartMode) {
                var totalDragX = 0f
                var toggledInCurrentDrag = false
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        toggledInCurrentDrag = false
                    },
                    onDragEnd = {
                        swipeProgress = 0f
                        totalDragX = 0f
                        toggledInCurrentDrag = false
                    },
                    onDragCancel = {
                        swipeProgress = 0f
                        totalDragX = 0f
                        toggledInCurrentDrag = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x

                        val buttonWidth = size.width
                        val fullSwipeThreshold = buttonWidth * 0.9f
                        val toggleThreshold = buttonWidth * 0.5f

                        when {
                            totalDragX > fullSwipeThreshold -> {
                                // Full left-to-right swipe: vibrate + auto-start immediately
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStart()
                                swipeProgress = 0f
                                totalDragX = 0f
                                toggledInCurrentDrag = false
                            }
                            kotlin.math.abs(totalDragX) > toggleThreshold && !toggledInCurrentDrag -> {
                                // 50%+ swipe: toggle quick-start mode, keep accumulating drag
                                onQuickStartModeChange(!isQuickStartMode)
                                toggledInCurrentDrag = true
                                swipeProgress = (totalDragX.coerceAtLeast(0f) / buttonWidth).coerceIn(0f, 1f)
                            }
                            else -> {
                                swipeProgress = (totalDragX.coerceAtLeast(0f) / buttonWidth).coerceIn(0f, 1f)
                            }
                        }
                    }
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (isQuickStartMode) {
                        onStart()
                    } else {
                        onExpandToggle()
                    }
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
            fadeIn(tween(350, delayMillis = 80))
                .togetherWith(fadeOut(tween(200)))
                .using(SizeTransform(clip = true, sizeAnimationSpec = { _, _ -> tween(280) }))
        },
        label = "buttonContentTransition",
        modifier = finalModifier
    ) { expanded ->
        if (!expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(currentBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isQuickStartMode || modeProgress > 0.5f) "START" else label,
                    color = textColor,
                    fontSize = 18.sp,
                    fontFamily = PixelF,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .widthIn(max = 420.dp)
                    .padding(top = if (isSmallScreen) 24.dp else 32.dp, bottom = if (isSmallScreen) 20.dp else 28.dp)
                    .pointerInput(Unit) {
                        var verticalDragSum = 0f
                        detectVerticalDragGestures(
                            onDragEnd = { verticalDragSum = 0f },
                            onDragCancel = { verticalDragSum = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                verticalDragSum += dragAmount
                                if (kotlin.math.abs(verticalDragSum) > 50f) {
                                    onExpandToggle()
                                    verticalDragSum = 0f
                                }
                            }
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GRID SIZE",
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.7f),
                    fontSize = 12.5.sp,
                    fontFamily = PixelF,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(bottom = if (isSmallScreen) 8.dp else 12.dp),
                    textAlign = TextAlign.Center
                )

                SizeSlider(
                    selectedSize = selectedSize,
                    onSizeSelected = onSizeSelected,
                    isDark = isDark,
                    currentSize = currentSize,
                    isSmallScreen = isSmallScreen
                )

                Spacer(Modifier.height(if (isSmallScreen) 20.dp else 32.dp))

                Text(
                    text = "DIFFICULTY",
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.7f),
                    fontSize = 12.5.sp,
                    fontFamily = PixelF,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(bottom = if (isSmallScreen) 8.dp else 12.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
                ) {
                    Difficulty.entries.forEach { difficulty ->
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
                
                Spacer(Modifier.height(if (isSmallScreen) 24.dp else 36.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(if (isSmallScreen) 44.dp else 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor())
                        .clickable(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStart()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "START",
                        color = if (isDark) Color.Black else Color.White,
                        fontSize = 16.sp,
                        fontFamily = PixelF,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
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
    val containerBg = if (isDark) Color(0xFF141414) else Color(0xFFD6D6D6)
    val accent = accentColor()
    val options = listOf(4, 5, 6)
    val textColor by animateColorAsState(
        targetValue = if (isDark) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )
    
    var totalDrag by remember { mutableFloatStateOf(0f) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val selectedIndex = options.indexOf(selectedSize)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(if (isSmallScreen) 44.dp else 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerBg)
            .padding(4.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDrag = 0f
                        dragAccumulator = 0f
                    },
                    onDragEnd = {
                        totalDrag = 0f
                        dragAccumulator = 0f
                    },
                    onDragCancel = {
                        totalDrag = 0f
                        dragAccumulator = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                        dragAccumulator += dragAmount
                        
                        val threshold = 30f
                        
                        if (dragAccumulator > threshold && selectedIndex < options.size - 1) {
                            onSizeSelected(options[selectedIndex + 1])
                            dragAccumulator = 0f
                            totalDrag = 0f
                        } else if (dragAccumulator < -threshold && selectedIndex > 0) {
                            onSizeSelected(options[selectedIndex - 1])
                            dragAccumulator = 0f
                            totalDrag = 0f
                        }
                    }
                )
            }
    ) {
        val width = maxWidth
        val optionsCount = options.size
        val thumbWidth = width / optionsCount
        val currentSelectedIndex = options.indexOf(selectedSize)
        
        val thumbOffset by animateDpAsState(
            targetValue = thumbWidth * currentSelectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "thumbOffset"
        )
        
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(thumbWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(accent)
        )
        
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            options.forEach { size ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSizeSelected(size) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$size x $size",
                        color = textColor,
                        fontSize = 15.sp,
                        fontFamily = PixelF,
                        fontWeight = if (selectedSize == size) FontWeight.Bold else FontWeight.Normal
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
    val unselectedBg = if (isDark) Color(0xFF141414) else Color(0xFFD6D6D6)
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
                .aspectRatio(0.98f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(12.dp))
                .background(cardBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = kanji,
                color = textColor,
                fontSize = if (isSmallScreen) 28.sp else 32.sp,
                textAlign = TextAlign.Center,
                fontFamily = Yuji,
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
        
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 12.dp))

        Text(
            text = difficulty.name,
            color = textColor.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontFamily = PixelF,
            letterSpacing = 2.sp
        )
    }
}