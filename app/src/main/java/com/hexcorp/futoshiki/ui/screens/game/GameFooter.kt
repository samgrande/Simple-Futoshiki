package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.hexcorp.futoshiki.game.Difficulty
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.components.shared.ExpandableStartButton
import com.hexcorp.futoshiki.ui.theme.LocalIsDark

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
    onNewGame: (Int, Difficulty) -> Unit = { _, _ -> }
) {
    // Hide the footer completely while the intro countdown is running
    if (showCountdown) return

    val isDark = LocalIsDark.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = hPad)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (!isSolved) {
            BigButton(
                label = if (isSolveMode) "SOLVE" else "RESET",
                onClick = {
                    if (isSolveMode) {
                        onSolve()
                    } else {
                        onClearAll()
                    }
                },
                onLongClick = {
                    onSolveModeChange(true)
                },
                primary = isSolveMode,
                inverted = !isSolveMode,
                bordered = false,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 8.dp)
            )
        } else {
            // NEW GAME button for Solution screen
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
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
