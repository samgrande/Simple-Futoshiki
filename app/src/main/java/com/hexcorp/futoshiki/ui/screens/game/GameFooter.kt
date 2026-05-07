package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.hexcorp.futoshiki.game.Difficulty
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.components.shared.ExpandableStartButton
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import com.hexcorp.futoshiki.ui.theme.PixelF

@Composable
fun GameFooter(
    isSolved: Boolean,
    showCountdown: Boolean,
    onClearAll: () -> Unit,
    onSolve: () -> Unit,
    isSolveMode: Boolean,
    onSolveModeChange: (Boolean) -> Unit,
    hPad: Dp,
    // New parameters for solution screen new game
    newGameExpanded: Boolean = false,
    onNewGameExpandedChange: (Boolean) -> Unit = {},
    selectedSize: Int = 4,
    onSizeSelected: (Int) -> Unit = {},
    selectedDifficulty: Difficulty = Difficulty.EASY,
    onDifficultyChange: (Difficulty) -> Unit = {},
    onNewGame: (Int, Difficulty) -> Unit = { _, _ -> },
    buttonHeight: Dp = 64.dp,
    isDark: Boolean = LocalIsDark.current
) {
    // Hide the footer completely while the intro countdown is running
    if (showCountdown) return

    // Auto-revert SOLVE button back to RESET after 5 seconds
    androidx.compose.runtime.LaunchedEffect(isSolveMode) {
        if (isSolveMode) {
            delay(5000)
            onSolveModeChange(false)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = hPad)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (!isSolved) {
            if (isSolveMode) {
                BigButton(
                    label = "SOLVE",
                    onClick = {
                        onSolve()
                        onSolveModeChange(false)
                    },
                    primary = true,
                    monochrome = true,
                    height = buttonHeight,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 8.dp)
                )
            } else {
                ResetButton(
                    label = "RESET",
                    onClick = onClearAll,
                    onLongClick = { onSolveModeChange(true) },
                    height = buttonHeight,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 8.dp)
                )
            }
        } else {
            // Expandable NEW GAME button on Solution screen
            ExpandableStartButton(
                label = "NEW GAME",
                isExpanded = newGameExpanded,
                onExpandToggle = { onNewGameExpandedChange(!newGameExpanded) },
                selectedSize = selectedSize,
                onSizeSelected = onSizeSelected,
                selectedDifficulty = selectedDifficulty,
                onDifficultyChange = onDifficultyChange,
                onStart = { onNewGame(selectedSize, selectedDifficulty) },
                isDark = isDark,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResetButton(
    label: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    height: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val accent = com.hexcorp.futoshiki.ui.theme.accentColor()
    val bgColor = FutoshikiColors.cellDefault()
    val textColorBase = FutoshikiColors.onSurface()
    val rippleColor = FutoshikiColors.onSurface()
    val invertedTextColor = FutoshikiColors.background()
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val holdProgress = remember { Animatable(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(500) // Initial hold delay before ripple starts
            if (isPressed) {
                // Fill over 400ms (faster)
                holdProgress.animateTo(1f, animationSpec = tween(400, easing = LinearEasing))
                if (holdProgress.value >= 1f) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick?.invoke()
                    holdProgress.snapTo(0f) // Reset after trigger
                }
            }
        } else {
            // Quick fade out if released early
            holdProgress.animateTo(0f, animationSpec = tween(250))
        }
    }

    val animatedTextColor by animateColorAsState(
        targetValue = if (holdProgress.value > 0.5f) invertedTextColor else textColorBase,
        animationSpec = tween(10), // Faster color swap
        label = "resetTextInvert"
    )

    val displayText = if (holdProgress.value > 0.5f) "SOLVE" else label

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .drawBehind {
                if (holdProgress.value > 0f) {
                    val maxRadius = size.width.coerceAtLeast(size.height) * 1.2f
                    drawCircle(
                        color = rippleColor,
                        radius = maxRadius * holdProgress.value,
                        center = center
                    )
                }
            }
            .testTag("reset_button")
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // Disable default ripple
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = animatedTextColor,
            fontSize = 18.sp,
            fontFamily = PixelF,
            letterSpacing = 1.sp
        )
    }
}
