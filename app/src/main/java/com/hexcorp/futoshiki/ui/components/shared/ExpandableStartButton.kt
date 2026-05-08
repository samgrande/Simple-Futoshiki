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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    val normalBg = if (isDark) Color.White else Color.Black
    val expandedBg = if (isDark) Color(0xFF0B0B0B) else Color(0xFFF5F2F2)
    
    val normalText = if (isDark) Color.Black else Color.White
    val expandedText = if (isDark) Color.White else Color.Black

    // Use expanded background when expanded, normal when collapsed
    val currentBg by animateColorAsState(
        targetValue = if (isExpanded) expandedBg else normalBg,
        animationSpec = tween(300),
        label = "bgColor"
    )

    // Separate ripple for inside fill (excluding border)
    var showRipple by remember { mutableStateOf(false) }
    
    val rippleProgress by animateFloatAsState(
        targetValue = if (showRipple && !isExpanded) 1f else 0f,
        animationSpec = if (showRipple && !isExpanded) {
            tween(durationMillis = 250)
        } else {
            tween(durationMillis = 200)
        },
        label = "rippleProgress"
    )

    // Text color inverts when ripple shows (matches inverted background)
    val currentTextColor by animateColorAsState(
        targetValue = when {
            showRipple && !isExpanded -> if (isDark) Color.White else Color.Black
            isDark -> Color.Black
            else -> Color.White
        },
        animationSpec = tween(200),
        label = "textColor"
    )

    // Border color stays constant (no ripple on border)
    val currentStrokeColor = if (isDark) Color.White else Color.Black
    
    val borderWidth = 2f
    val cornerRadius = 14f

    val baseModifier = modifier
        .fillMaxWidth(if (isExpanded) 1.08f else 0.9f)
        .animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = 700f
            ),
            alignment = Alignment.TopCenter
        )
        .clip(RoundedCornerShape(14.dp))
        .border(
            width = 2.dp,
            color = currentStrokeColor,
            shape = RoundedCornerShape(14.dp)
        )
        .drawBehind {
            val width = size.width
            val height = size.height
            
            // Draw background
            drawRect(currentBg)
            
            // Draw ripple inside (clip to inner area, excluding border)
            if (rippleProgress > 0f) {
                val innerPath = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            left = borderWidth,
                            top = borderWidth,
                            right = width - borderWidth,
                            bottom = height - borderWidth,
                            radiusX = cornerRadius - borderWidth,
                            radiusY = cornerRadius - borderWidth
                        )
                    )
                }
                
                clipPath(innerPath, clipOp = ClipOp.Intersect) {
                    val maxRadius = kotlin.math.sqrt(width * width + height * height)
                    val currentRadius = maxRadius * rippleProgress
                    
                    // Ripple color matches menu background (pure dark/pure light)
                    val rippleColor = if (isDark) Color(0xFF0B0B0B) else Color(0xFFF5F2F2)
                    
                    // Always start ripple from the middle of the button
                    val rippleCenterX = width / 2
                    val rippleCenterY = height / 2
                    
                    drawCircle(
                        color = rippleColor.copy(alpha = 1f),
                        radius = currentRadius,
                        center = Offset(rippleCenterX, rippleCenterY)
                    )
                }
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
                // Ripple first, then expand after ripple fills
                scope.launch {
                    showRipple = true
                    delay(250) // Wait for ripple to fill
                    onExpandToggle()
                    delay(300) // Wait for expansion to start
                    showRipple = false
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
            fadeIn(tween(450, delayMillis = 150))
                .togetherWith(fadeOut(tween(250)))
                .using(SizeTransform(clip = false))
        },
        label = "buttonContentTransition",
        modifier = finalModifier
    ) { expanded ->
        if (!expanded) {
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
                    .fillMaxWidth(0.98f)
                    .widthIn(max = 420.dp)
                    .padding(top = 32.dp, bottom = 28.dp)
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
                    modifier = Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )

                // Size Slider
                SizeSlider(
                    selectedSize = selectedSize,
                    onSizeSelected = onSizeSelected,
                    isDark = isDark
                )
                
                Spacer(Modifier.height(32.dp))

                // Section Title: Difficulty
                Text(
                    text = "DIFFICULTY",
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.7f),
                    fontSize = 12.5.sp,
                    fontFamily = PixelF,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
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
                
                Spacer(Modifier.height(36.dp))

                // SAVE Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF141414) else Color(0xFFD6D6D6))
                        .clickable(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onExpandToggle() // Just collapse, don't start
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SAVE",
                        color = if (isDark) Color.White else Color.Black,
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
