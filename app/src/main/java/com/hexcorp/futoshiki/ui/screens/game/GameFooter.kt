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
                    .fillMaxWidth(0.9f)
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
    val bgColor = FutoshikiColors.cellDefault()
    val textColor = FutoshikiColors.onSurface()
    
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .testTag("reset_button")
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick?.invoke()
                }
            ),
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
}
