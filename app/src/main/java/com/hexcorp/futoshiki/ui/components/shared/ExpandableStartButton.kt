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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val normalBg = if (isDark) Color.White else Color.Black
    val expandedBg = if (isDark) Color(0xFF0B0B0B) else Color(0xFFF5F2F2)
    
    val normalText = if (isDark) Color.Black else Color.White
    val expandedText = if (isDark) Color.White else Color.Black

    val rippleProgress by animateFloatAsState(
        targetValue = if (isPressed || isExpanded) 1f else 0f,
        animationSpec = if (isPressed || isExpanded) {
            tween(durationMillis = 600, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 300)
        },
        label = "rippleProgress"
    )

    val currentTextColor = lerp(normalText, expandedText, rippleProgress)
    val currentStrokeColor = lerp(normalBg, if (isDark) Color.White else Color.Black, rippleProgress)

    val haptics = LocalHapticFeedback.current
    
    val baseModifier = modifier
        .fillMaxWidth(0.9f)
        .animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        .clip(RoundedCornerShape(14.dp))
        .border(
            width = 2.dp,
            color = currentStrokeColor,
            shape = RoundedCornerShape(14.dp)
        )
        .drawBehind {
            drawRect(normalBg)
            if (rippleProgress > 0f) {
                val maxRadius = size.width * 1.2f
                drawCircle(
                    color = expandedBg,
                    radius = maxRadius * rippleProgress,
                    center = center
                )
            }
        }

    val finalModifier = if (!isExpanded) {
        baseModifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onStart()
            },
            onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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

    Column(
        modifier = finalModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = currentTextColor,
                    fontSize = 18.sp,
                    fontFamily = PixelF,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 380.dp)
                    .padding(top = 40.dp, bottom = 32.dp)
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
                // Section Title: Grid Size
                Text(
                    text = "GRID SIZE",
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.7f),
                    fontSize = 12.5.sp,
                    fontFamily = PixelF,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(bottom = 14.dp),
                    textAlign = TextAlign.Center
                )

                // Size Slider
                SizeSlider(
                    selectedSize = selectedSize,
                    onSizeSelected = onSizeSelected,
                    isDark = isDark
                )
                
                Spacer(Modifier.height(40.dp))

                // Section Title: Difficulty
                Text(
                    text = "DIFFICULTY",
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.7f),
                    fontSize = 12.5.sp,
                    fontFamily = PixelF,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(bottom = 14.dp),
                    textAlign = TextAlign.Center
                )
                
                // Difficulty Selector Cards (易, 普, 難)
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Difficulty.entries.forEach { difficulty ->
                        Box(modifier = Modifier.weight(1f)) {
                            DifficultyCard(
                                difficulty = difficulty,
                                isSelected = selectedDifficulty == difficulty,
                                onClick = { onDifficultyChange(difficulty) },
                                isDark = isDark
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(48.dp))

                // START Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF141414) else Color(0xFFD6D6D6))
                        .clickable(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStart()
                            onExpandToggle()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "START GAME",
                        color = currentTextColor,
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
    isDark: Boolean
) {
    val containerBg = if (isDark) Color(0xFF141414) else Color(0xFFD6D6D6)
    val accent = com.hexcorp.futoshiki.ui.theme.accentColor()
    val options = listOf(4, 5, 6)
    val textColor by animateColorAsState(
        targetValue = if (isDark) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerBg)
            .padding(4.dp)
    ) {
        val width = maxWidth
        val optionsCount = options.size
        val thumbWidth = width / optionsCount
        val selectedIndex = options.indexOf(selectedSize)
        
        // Dynamic thumb offset based on selection
        val thumbOffset by animateDpAsState(
            targetValue = thumbWidth * selectedIndex,
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
        
        // Interaction Layer
        var totalDrag by remember { mutableFloatStateOf(0f) }
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { totalDrag = 0f },
                        onDragCancel = { totalDrag = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            totalDrag += dragAmount
                            val threshold = 50f
                            if (totalDrag > threshold) {
                                if (selectedIndex < options.size - 1) {
                                    onSizeSelected(options[selectedIndex + 1])
                                    totalDrag = 0f
                                }
                            } else if (totalDrag < -threshold) {
                                if (selectedIndex > 0) {
                                    onSizeSelected(options[selectedIndex - 1])
                                    totalDrag = 0f
                                }
                            }
                        }
                    )
                }
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
    isDark: Boolean
) {
    val accent = com.hexcorp.futoshiki.ui.theme.accentColor()
    val unselectedBg = if (isDark) Color(0xFF141414) else Color(0xFFD6D6D6)
    val targetBg = if (isSelected) accent else unselectedBg
    
    // Animated states
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
                .aspectRatio(0.98f) // Maintain the slightly taller rectangular shape
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
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                fontFamily = Yuji,
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = difficulty.name,
            color = textColor.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontFamily = PixelF,
            letterSpacing = 2.sp
        )
    }
}
