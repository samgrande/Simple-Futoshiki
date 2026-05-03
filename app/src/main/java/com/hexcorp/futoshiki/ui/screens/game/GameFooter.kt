package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GameFooter(
    isSolved: Boolean,
    showCountdown: Boolean,
    onNewGame: () -> Unit,
    onClearAll: () -> Unit,
    hPad: Dp
) {
    // Hide the footer completely while the intro countdown is running
    if (showCountdown) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = hPad)
            .padding(bottom = 12.dp),
        horizontalArrangement = if (isSolved) {
            Arrangement.Center
        } else {
            Arrangement.spacedBy(12.dp)
        }
    ) {
        ThemedPillButton(
            label = "NEW GAME",
            onClick = onNewGame,
            modifier = if (isSolved) Modifier.fillMaxWidth(0.6f) else Modifier.weight(1f)
        )
        if (!isSolved) {
            ThemedPillButton(
                label = "CLEAR",
                onClick = onClearAll,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
