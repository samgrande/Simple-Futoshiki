package com.hex.futoshiki.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hex.futoshiki.ui.components.*
import com.hex.futoshiki.ui.theme.FutoshikiColors
import com.hex.futoshiki.ui.theme.ReemKufi
import kotlin.math.hypot

import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

// ── Landing screen ────────────────────────────────────────────────────────────

@Composable
fun LandingScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelp by remember { mutableStateOf(false) }
    var showConfirmQuit by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler(enabled = true) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> showConfirmQuit = false
            else -> showConfirmQuit = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FutoshikiColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxHeight()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.2f))

            // 1. Static Section (Title always, Logo conditional)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = !showHelp,
                    contentAlignment = Alignment.Center,
                    transitionSpec = {
                        (fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.5f))
                            .togetherWith(fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.5f))
                            .using(SizeTransform(clip = false))
                    },
                    label = "logoTransition"
                ) { isVisible ->
                    if (isVisible) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.wrapContentSize()
                        ) {
                            LogoMark(size = 96.dp)
                            Spacer(Modifier.height(18.dp))
                        }
                    } else {
                        Spacer(Modifier.size(0.dp))
                    }
                }
            }
            
            // FutoshikiTitle is always visible and stays in the same place
            FutoshikiTitle(fontSize = 38.sp)

            // 2. Animated Section (Buttons & Confirmation text)
            AnimatedContent(
                targetState = when {
                    showConfirmQuit -> "confirm"
                    showHelp -> "help"
                    else -> "menu"
                },
                transitionSpec = {
                    val duration = 300
                    if (targetState != "menu") {
                        (fadeIn(tween(duration)) + slideInVertically { it / 4 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically { -it / 4 })
                    } else {
                        (fadeIn(tween(duration)) + slideInVertically { -it / 4 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically { it / 4 })
                    }.using(SizeTransform(clip = false))
                },
                label = "landingContentTransition",
                modifier = Modifier.fillMaxWidth()
            ) { state ->
                when (state) {
                    "help" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                        ) {
                            Spacer(Modifier.height(24.dp))
                            HelpPanel(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .fillMaxHeight(0.7f),
                                scrollable = true
                            )
                            Spacer(Modifier.height(20.dp))
                            BigButton(label = "BACK", onClick = { showHelp = false })
                        }
                    }
                    "confirm" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(32.dp))
                            Text(
                                text          = "Q U I T   T H E   G A M E  ?",
                                fontSize      = 13.sp,
                                fontWeight    = FontWeight.SemiBold,
                                fontFamily    = ReemKufi,
                                color         = Color(0xFF999999),
                                letterSpacing = 2.sp
                            )
                            
                            Spacer(Modifier.height(32.dp))

                            BigButton(label = "Y E S", onClick = { (context as? ComponentActivity)?.finish() }, primary = true)
                            Spacer(Modifier.height(14.dp))
                            BigButton(label = "N O",  onClick = { showConfirmQuit = false })
                        }
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(48.dp))

                            BigButton(label = "START", onClick = onStart, primary = true)
                            Spacer(Modifier.height(12.dp))
                            BigButton(label = "HELP", onClick = { showHelp = true })
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Made with ♡ by @HeX",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    fontFamily = ReemKufi
                )
            }
        }
    }
}

// ── Pause overlay ─────────────────────────────────────────────────────────────

@Composable
fun PauseOverlay(
    revealCenter: Offset,
    pillOffset: Offset,
    seconds: Int,
    onResume: () -> Unit,
    onMainMenu: () -> Unit,
    onSolve:  () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelp by remember { mutableStateOf(false) }
    var showConfirmQuit by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Pause on back button/gesture
    BackHandler(enabled = visible) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> showConfirmQuit = false
            else -> onResume()
        }
    }

    // Quick fade-in animation
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(180), // Even quicker
        label = "alpha"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // ── The Fading Content ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = alpha
                }
                .background(FutoshikiColors.Background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(0.8f))

                // Static Logo & Title (Not in AnimatedContent)
                LogoMark(size = 80.dp)
                Spacer(Modifier.height(16.dp))
                FutoshikiTitle(fontSize = 32.sp)

                AnimatedContent(
                    targetState = if (showConfirmQuit) "confirm" else if (showHelp) "help" else "menu",
                    transitionSpec = {
                        val duration = 280
                        if (targetState != "menu") {
                            // Slide in from bottom (entering help/confirm)
                            (slideInVertically(tween(duration)) { it / 4 } + fadeIn(tween(duration)))
                                .togetherWith(slideOutVertically(tween(duration)) { -it / 4 } + fadeOut(tween(duration)))
                        } else {
                            // Slide in from top (returning to menu)
                            (slideInVertically(tween(duration)) { -it / 4 } + fadeIn(tween(duration)))
                                .togetherWith(slideOutVertically(tween(duration)) { it / 4 } + fadeOut(tween(duration)))
                        }.using(SizeTransform(clip = false))
                    },
                    label = "pauseContentTransition"
                ) { state ->
                    when (state) {
                        "help" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(24.dp))
                                HelpPanel()
                                Spacer(Modifier.height(24.dp))
                                BigButton(label = "← BACK", onClick = { showHelp = false })
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
                                    color         = Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                
                                Spacer(Modifier.height(32.dp))

                                BigButton(label = "YES", onClick = onMainMenu, primary = true)
                                Spacer(Modifier.height(14.dp))
                                BigButton(label = "NO",  onClick = { showConfirmQuit = false })
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
                                    color         = Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                
                                Spacer(Modifier.height(48.dp))

                                BigButton(label = "MAIN MENU", onClick = { showConfirmQuit = true }, primary = true)
                                Spacer(Modifier.height(14.dp))
                                BigButton(label = "SOLVE",     onClick = onSolve)
                                Spacer(Modifier.height(14.dp))
                                BigButton(label = "HELP",      onClick = { showHelp = true })
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }
        }

        // ── The stay-in-place TimerPill (Outside the clip) ────────────
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
