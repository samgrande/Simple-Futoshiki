package com.hexcorp.futoshiki.ui.screens.pause

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.game.Difficulty
import com.hexcorp.futoshiki.ui.components.shared.*
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.PixelF
import kotlin.math.roundToInt

@Composable
fun PauseOverlay(
    revealCenter: Offset,
    pillOffset: Offset,
    seconds: Int,
    won: Boolean = false,
    currentSize: Int = 4,
    currentDifficulty: Difficulty = Difficulty.EASY,
    onResume: () -> Unit,
    onMainMenu: () -> Unit,
    onNewGame: (Int, Difficulty) -> Unit,
    onTheming: () -> Unit,
    modifier: Modifier = Modifier,
    startWithQuitConfirm: Boolean = false
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showConfirmQuit by rememberSaveable { mutableStateOf(startWithQuitConfirm) }
    var showConfirmNewGame by rememberSaveable { mutableStateOf(false) }
    var newGameExpanded by rememberSaveable { mutableStateOf(false) }
    
    var selectedSize by remember { mutableIntStateOf(currentSize) }
    var selectedDifficulty by remember { mutableStateOf(currentDifficulty) }

    BackHandler(enabled = true) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> if (won) onResume() else showConfirmQuit = false
            showConfirmNewGame -> showConfirmNewGame = false
            newGameExpanded -> {
                selectedSize = currentSize
                selectedDifficulty = currentDifficulty
                newGameExpanded = false
            }
            else -> onResume()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FutoshikiColors.background()),
            contentAlignment = Alignment.Center
        ) {
            // Dismissal Scrim
            if (newGameExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { 
                                selectedSize = currentSize
                                selectedDifficulty = currentDifficulty
                                newGameExpanded = false 
                            }
                        )
                )
            }
            
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxHeight()
                    .systemBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(0.8f))

                if (!newGameExpanded) {
                    LogoMark(size = 80.dp)
                    Spacer(Modifier.height(16.dp))
                }
                
                FutoshikiTitle(fontSize = 38.sp)

                AnimatedContent(
                    targetState = when {
                        showConfirmQuit -> "confirm_quit"
                        showConfirmNewGame -> "confirm_new_game"
                        showHelp -> "help"
                        else -> "menu"
                    },
                    transitionSpec = {
                        val duration = 280
                        if (targetState != "menu") {
                            (slideInVertically(tween(duration)) { it / 4 } + fadeIn(tween(duration)))
                                .togetherWith(
                                    slideOutVertically(tween(duration)) { -it / 4 } + fadeOut(tween(400))
                                )
                        } else {
                            (slideInVertically(tween(duration)) { -it / 4 } + fadeIn(tween(duration)))
                                .togetherWith(
                                    slideOutVertically(tween(duration)) { it / 4 } + fadeOut(tween(400))
                                )
                        }.using(SizeTransform(clip = false))
                    },
                    label = "pauseContentTransition"
                ) { currentState ->
                    val isDark = LocalIsDark.current
                    when (currentState) {
                        "help" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(24.dp))
                                HelpPanel()
                                Spacer(Modifier.height(24.dp))
                                BigButton(
                                    label = "← BACK",
                                    onClick = { showHelp = false },
                                    inverted = true,
                                    isDark = isDark
                                )
                            }
                        }

                        "confirm_quit" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(32.dp))
                                Text(
                                    text = "QUIT TO MAIN MENU?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelF,
                                    color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(32.dp))
                                BigButton(
                                    label = "YES",
                                    onClick = onMainMenu,
                                    inverted = true,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(20.dp))
                                BigButton(
                                    label = "NO",
                                    onClick = { if (won) onResume() else showConfirmQuit = false },
                                    isDark = isDark
                                )
                            }
                        }

                        "confirm_new_game" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(32.dp))
                                Text(
                                    text = "THE CURRENT PUZZLE WILL END",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelF,
                                    color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 1.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "PROCEED?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelF,
                                    color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(32.dp))
                                BigButton(
                                    label = "YES",
                                    onClick = { onNewGame(selectedSize, selectedDifficulty) },
                                    inverted = true,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(20.dp))
                                BigButton(
                                    label = "NO",
                                    onClick = { 
                                        selectedSize = currentSize
                                        showConfirmNewGame = false 
                                    },
                                    isDark = isDark
                                )
                            }
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "PAUSED",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = PixelF,
                                    color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(48.dp))
                                
                                // NEW GAME (Expandable)
                                ExpandableStartButton(
                                    label = "NEW GAME",
                                    isExpanded = newGameExpanded,
                                    onExpandToggle = { newGameExpanded = !newGameExpanded },
                                    selectedSize = selectedSize,
                                    onSizeSelected = { selectedSize = it },
                                    selectedDifficulty = selectedDifficulty,
                                    onDifficultyChange = { selectedDifficulty = it },
                                    onStart = { 
                                        if (won) {
                                            onNewGame(selectedSize, selectedDifficulty)
                                        } else {
                                            showConfirmNewGame = true
                                        }
                                    },
                                    isDark = isDark
                                )
                                
                                Spacer(Modifier.height(20.dp))
                                
                                // HELP
                                BigButton(
                                    label = "HELP",
                                    onClick = { showHelp = true },
                                    isDark = isDark
                                )
                                
                                Spacer(Modifier.height(20.dp))
                                
                                // THEMES
                                BigButton(
                                    label = "THEMES",
                                    onClick = onTheming,
                                    isDark = isDark
                                )
                                
                                Spacer(Modifier.height(20.dp))
                                
                                // MAIN MENU
                                BigButton(
                                    label = "MAIN MENU",
                                    onClick = { showConfirmQuit = true },
                                    inverted = false,
                                    isDark = isDark
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }
        }

        if (!showConfirmQuit && !showConfirmNewGame) {
            TimerPill(
                seconds = seconds,
                won = won,
                isPaused = true,
                onClick = onResume,
                modifier = Modifier.offset {
                    IntOffset(pillOffset.x.roundToInt(), pillOffset.y.roundToInt())
                }
            )
        }
    }
}