package com.hexcorp.futoshiki.ui.screens.landing

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.game.Difficulty
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.components.shared.ExpandableStartButton
import com.hexcorp.futoshiki.ui.components.shared.HelpPanel
import com.hexcorp.futoshiki.ui.theme.Midorima
import com.hexcorp.futoshiki.ui.theme.PixelF

@Composable
fun LandingMenuContent(
    state: String,
    isDark: Boolean,
    selectedSize: Int,
    onSizeSelected: (Int) -> Unit,
    selectedDifficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    currentDifficulty: Difficulty,
    onDifficultySave: (Difficulty) -> Unit,
    startExpanded: Boolean,
    onStartToggle: () -> Unit,
    onStart: () -> Unit,
    onTheming: () -> Unit,
    onQuit: () -> Unit,
    onShowHelp: () -> Unit,
    onHideHelp: () -> Unit,
    onHideConfirmQuit: () -> Unit,
    modifier: Modifier = Modifier,
    isSmallScreen: Boolean = false
) {
    val buttonSpacing = if (isSmallScreen) 28.dp else 35.dp
    val topSpacing = if (isSmallScreen) 32.dp else 48.dp

    when (state) {
        "help" -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                HelpPanel(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f),
                    scrollable = true
                )
                Spacer(Modifier.height(20.dp))
                BigButton(
                    label = "BACK",
                    onClick = onHideHelp,
                    inverted = true,
                    isDark = isDark
                )
            }
        }
        "confirm" -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
            ) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text          = "Q U I T   T H E   G A M E  ?",
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.SemiBold,
                    fontFamily    = PixelF,
                    color         = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(32.dp))
                BigButton(
                    label = "Y E S",
                    onClick = onQuit,
                    inverted = true,
                    isDark = isDark
                )
                Spacer(Modifier.height(35.dp))
                BigButton(
                    label = "N O",
                    onClick = onHideConfirmQuit,
                    isDark = isDark
                )
            }
        }
        else -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
            ) {
                Spacer(Modifier.height(topSpacing))
                
                ExpandableStartButton(
                    label = "NEW GAME",
                    isExpanded = startExpanded,
                    onExpandToggle = onStartToggle,
                    selectedSize = selectedSize,
                    onSizeSelected = onSizeSelected,
                    selectedDifficulty = selectedDifficulty,
                    onDifficultyChange = onDifficultyChange,
                    onStart = onStart,
                    isDark = isDark,
                    onDifficultySave = onDifficultySave,
                    currentSize = selectedSize,
                    currentDifficulty = selectedDifficulty,
                    hasActiveGame = false,
                    isSmallScreen = isSmallScreen
                )

                AnimatedVisibility(
                    visible = !startExpanded,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(200))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(buttonSpacing))
                        BigButton(
                            label = "HELP",
                            onClick = onShowHelp,
                            isDark = isDark,
                            height = 64.dp
                        )
                        Spacer(Modifier.height(buttonSpacing))
                        BigButton(
                            label = "THEMES",
                            onClick = onTheming,
                            isDark = isDark,
                            height = 64.dp
                        )
                    }
                }
            }
        }
    }
}
