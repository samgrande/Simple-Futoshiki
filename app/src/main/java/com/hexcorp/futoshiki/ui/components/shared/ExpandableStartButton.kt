package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.hexcorp.futoshiki.ui.theme.PixelF

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
    isDark: Boolean
) {
    val bgColor = if (isDark) Color(0xFFEAEAEA) else Color.Black
    val textColor = if (isDark) Color.Black else Color.White
    val haptics = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .let {
                if (!isExpanded) {
                    it.combinedClickable(
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
                    it.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Block scrim clicks */ }
                    )
                }
            },
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
                    color = textColor,
                    fontSize = 18.sp,
                    fontFamily = PixelF,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Size Slider (4x4, 5x5, 6x6)
                SizeSlider(
                    selectedSize = selectedSize,
                    onSizeSelected = onSizeSelected,
                    isDark = isDark
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Difficulty Selector Cards (易, 普, 難)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Difficulty.values().forEach { difficulty ->
                        DifficultyCard(
                            difficulty = difficulty,
                            isSelected = selectedDifficulty == difficulty,
                            onClick = { onDifficultyChange(difficulty) },
                            isDark = isDark
                        )
                    }
                }
                
                Spacer(Modifier.height(32.dp))

                // START Button
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.08f))
                        .clickable(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStart()
                            onExpandToggle()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "START",
                        color = textColor,
                        fontSize = 16.sp,
                        fontFamily = PixelF,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Spacer(Modifier.height(8.dp))
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
    val containerBg = if (isDark) Color(0xFFD0D0D0) else Color(0xFF111111)
    val accent = com.hexcorp.futoshiki.ui.theme.accentColor()
    val options = listOf(4, 5, 6)
    val textColor = if (isDark) Color.Black else Color.White
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerBg)
            .padding(4.dp)
    ) {
        val width = maxWidth
        val segmentWidth = width / options.size
        val selectedIndex = options.indexOf(selectedSize)
        
        // Animated background thumb
        val thumbOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "thumbOffset"
        )
        
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(segmentWidth)
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
                        fontSize = 11.sp,
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
    val unselectedBg = if (isDark) Color(0xFFD0D0D0) else Color(0xFF111111)
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
        targetValue = if (isDark) Color.Black else Color.White,
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
        modifier = Modifier.width(84.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
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
                fontSize = 36.sp,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = difficulty.name,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = PixelF,
            letterSpacing = 1.sp
        )
    }
}
