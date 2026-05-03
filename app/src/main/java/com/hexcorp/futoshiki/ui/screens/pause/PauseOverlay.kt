package com.hexcorp.futoshiki.ui.screens.pause

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.components.shared.HelpPanel
import com.hexcorp.futoshiki.ui.components.shared.LogoMark
import com.hexcorp.futoshiki.ui.components.shared.TimerPill
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.Midorima
import kotlin.math.roundToInt

@Composable
fun PauseOverlay(
    revealCenter: Offset,
    pillOffset: Offset,
    seconds: Int,
    won: Boolean = false,
    onResume: () -> Unit,
    onMainMenu: () -> Unit,
    onSolve: () -> Unit,
    onNewGame: () -> Unit,
    onTheming: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showConfirmQuit by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = true) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> showConfirmQuit = false
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
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxHeight()
                    .systemBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(0.8f))

                LogoMark(size = 80.dp)
                Spacer(Modifier.height(16.dp))
                FutoshikiTitle(fontSize = 38.sp)

                AnimatedContent(
                    targetState = when {
                        showConfirmQuit -> "confirm"
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

                        "confirm" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(32.dp))
                                Text(
                                    text = "QUIT TO MAIN MENU?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Midorima,
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
                                    onClick = { showConfirmQuit = false },
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
                                    fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
                                    color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(48.dp))
                                BigButton(
                                    label = "MAIN MENU",
                                    onClick = { showConfirmQuit = true },
                                    inverted = true,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(20.dp))
                                BigButton(
                                    label = "SOLVE",
                                    onClick = { if (!won) onSolve() },
                                    isDark = isDark,
                                    modifier = Modifier.alpha(if (won) 0.35f else 1f)
                                )
                                Spacer(Modifier.height(20.dp))
                                BigButton(
                                    label = "HELP",
                                    onClick = { showHelp = true },
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(20.dp))
                                BigButton(
                                    label = "THEMES",
                                    onClick = onTheming,
                                    isDark = isDark
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }
        }

        TimerPill(
            seconds = seconds,
            won = false,
            isPaused = true,
            onClick = onResume,
            modifier = Modifier.offset {
                IntOffset(pillOffset.x.roundToInt(), pillOffset.y.roundToInt())
            }
        )
    }
}