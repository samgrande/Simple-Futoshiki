package com.hex.futoshiki.ui.screens

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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            AnimatedContent(
                targetState = showHelp,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(300)) + slideInVertically { it / 2 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically { -it / 2 })
                    } else {
                        (fadeIn(tween(300)) + slideInVertically { -it / 2 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically { it / 2 })
                    }.using(SizeTransform(clip = false))
                },
                label = "landingContentTransition"
            ) { isHelp ->
                if (isHelp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        HelpPanel()
                        Spacer(Modifier.height(20.dp))
                        BigButton(label = "← BACK", onClick = { showHelp = false })
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LogoMark(size = 96.dp)
                        Spacer(Modifier.height(18.dp))
                        FutoshikiTitle(fontSize = 38.sp)
                        Spacer(Modifier.height(48.dp))

                        BigButton(label = "START", onClick = onStart, primary = true)
                        Spacer(Modifier.height(12.dp))
                        BigButton(label = "HELP", onClick = { showHelp = true })
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
    onSolve:  () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelp by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Fast and snappy reveal animation
    val revealProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "revealProgress"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // ── The Revealable Content ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val maxRadius = hypot(size.width, size.height)
                    val radius = maxRadius * revealProgress
                    
                    clip = true
                    shape = object : Shape {
                        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                            val path = Path().apply {
                                addOval(Rect(center = revealCenter, radius = radius))
                            }
                            return Outline.Generic(path)
                        }
                    }
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

                AnimatedContent(
                    targetState = showHelp,
                    transitionSpec = {
                        if (targetState) {
                            (fadeIn(tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.92f))
                                .togetherWith(fadeOut(tween(180)))
                        } else {
                            (fadeIn(tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.92f))
                                .togetherWith(fadeOut(tween(180)))
                        }.using(SizeTransform(clip = false))
                    },
                    label = "pauseContentTransition"
                ) { isHelp ->
                    if (isHelp) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HelpPanel()
                            Spacer(Modifier.height(24.dp))
                            BigButton(label = "← BACK", onClick = { showHelp = false })
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LogoMark(size = 80.dp)
                            Spacer(Modifier.height(16.dp))
                            FutoshikiTitle(fontSize = 32.sp)
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

                            BigButton(label = "RESUME", onClick = onResume, primary = true)
                            Spacer(Modifier.height(14.dp))
                            BigButton(label = "SOLVE",  onClick = onSolve)
                            Spacer(Modifier.height(14.dp))
                            BigButton(label = "HELP",   onClick = { showHelp = true })
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
