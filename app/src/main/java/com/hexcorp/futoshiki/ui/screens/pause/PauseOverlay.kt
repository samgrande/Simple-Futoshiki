package com.hexcorp.futoshiki.ui.screens.pause

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.components.shared.HelpPanel
import com.hexcorp.futoshiki.ui.components.shared.LogoMark
import com.hexcorp.futoshiki.ui.components.shared.TimerPill
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import kotlin.math.roundToInt

@Composable
fun PauseOverlay(
    revealCenter: Offset,
    pillOffset: Offset,
    seconds: Int,
    onResume: () -> Unit,
    onMainMenu: () -> Unit,
    onSolve: () -> Unit,
    onNewGame: () -> Unit,
    onTheming: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelp by remember { mutableStateOf(false) }
    var showConfirmQuit by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    BackHandler(enabled = visible) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> showConfirmQuit = false
            else -> onResume()
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(180),
        label = "alpha"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = alpha
                }
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
                FutoshikiTitle(fontSize = 32.sp)

                AnimatedContent(
                    targetState = if (showConfirmQuit) "confirm" else if (showHelp) "help" else "menu",
                    transitionSpec = {
                        val duration = 280
                        if (targetState != "menu") {
                            (slideInVertically(tween(duration)) { it / 4 } + fadeIn(tween(duration)))
                                .togetherWith(slideOutVertically(tween(duration)) { -it / 4 } + fadeOut(tween(400)))
                        } else {
                            (slideInVertically(tween(duration)) { -it / 4 } + fadeIn(tween(duration)))
                                .togetherWith(slideOutVertically(tween(duration)) { it / 4 } + fadeOut(tween(400)))
                        }.using(SizeTransform(clip = false))
                    },
                    label = "pauseContentTransition"
                ) { state ->
                    val isDark = LocalIsDark.current
                    when (state) {
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
                                    text          = "QUIT TO MAIN MENU?",
                                    fontSize      = 14.sp,
                                    fontWeight    = FontWeight.SemiBold,
                                    fontFamily    = ReemKufi,
                                    color         = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(32.dp))
                                BigButton(
                                    label = "YES",
                                    onClick = onMainMenu,
                                    primary = true,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(14.dp))
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
                                    text          = "PAUSED",
                                    fontSize      = 14.sp,
                                    fontWeight    = FontWeight.SemiBold,
                                    fontFamily    = ReemKufi,
                                    color         = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(48.dp))
                                BigButton(
                                    label = "MAIN MENU",
                                    onClick = { showConfirmQuit = true },
                                    primary = true,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(14.dp))
                                BigButton(
                                    label = "SOLVE",
                                    onClick = onSolve,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(14.dp))
                                BigButton(
                                    label = "HELP",
                                    onClick = { showHelp = true },
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(14.dp))
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
