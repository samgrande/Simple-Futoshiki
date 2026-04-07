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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hex.futoshiki.ui.components.*
import com.hex.futoshiki.ui.theme.FutoshikiColors
import com.hex.futoshiki.ui.theme.ReemKufi

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
                        (fadeIn(tween(300)) + slideInHorizontally { it / 2 })
                            .togetherWith(fadeOut(tween(250)) + slideOutHorizontally { -it / 2 })
                    } else {
                        (fadeIn(tween(300)) + slideInHorizontally { -it / 2 })
                            .togetherWith(fadeOut(tween(250)) + slideOutHorizontally { it / 2 })
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
    timerFormatted: String,
    onResume: () -> Unit,
    onSolve:  () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelp by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val backdropAlpha by animateFloatAsState(
        targetValue  = if (visible) 1f else 0f,
        animationSpec = tween(250),
        label        = "backdrop"
    )
    val cardScale by animateFloatAsState(
        targetValue  = if (visible) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f),
        label        = "cardScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FutoshikiColors.Overlay.copy(alpha = FutoshikiColors.Overlay.alpha * backdropAlpha)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(horizontal = 24.dp)
                .graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                    alpha = backdropAlpha
                }
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, FutoshikiColors.OnSurface, RoundedCornerShape(24.dp))
                .background(FutoshikiColors.Background)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            AnimatedContent(
                targetState = showHelp,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(tween(220, delayMillis = 90)) + slideInHorizontally { it / 2 })
                            .togetherWith(fadeOut(tween(180)) + slideOutHorizontally { -it / 2 })
                    } else {
                        (fadeIn(tween(220, delayMillis = 90)) + slideInHorizontally { -it / 2 })
                            .togetherWith(fadeOut(tween(180)) + slideOutHorizontally { it / 2 })
                    }.using(SizeTransform(clip = false))
                },
                label = "pauseContentTransition"
            ) { isHelp ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isHelp) {
                        HelpPanel()
                        Spacer(Modifier.height(20.dp))
                        BigButton(label = "← BACK", onClick = { showHelp = false })
                    } else {
                        LogoMark(size = 64.dp)
                        Spacer(Modifier.height(10.dp))
                        FutoshikiTitle(fontSize = 26.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text          = "PAUSED",
                            fontSize      = 12.sp,
                            fontWeight    = FontWeight.SemiBold,
                            fontFamily    = ReemKufi,
                            color         = Color(0xFF888888),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(24.dp))

                        BigButton(label = "RESUME", onClick = onResume, primary = true)
                        Spacer(Modifier.height(10.dp))
                        BigButton(label = "SOLVE",  onClick = onSolve)
                        Spacer(Modifier.height(10.dp))
                        BigButton(label = "HELP",   onClick = { showHelp = true })
                    }
                }
            }
        }
    }
}
