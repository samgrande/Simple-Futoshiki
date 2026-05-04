package com.hexcorp.futoshiki.ui.screens.landing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import com.hexcorp.futoshiki.game.Difficulty
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.components.shared.LogoMark
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.Midorima
import com.hexcorp.futoshiki.ui.theme.PixelF

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LandingScreen(
    currentSize: Int,
    onStart: (Int, Difficulty) -> Unit,
    onTheming: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier,
    scope: AnimatedVisibilityScope? = null
) {
    var showHelp by remember { mutableStateOf(false) }
    var showConfirmQuit by remember { mutableStateOf(false) }
    var startExpanded by remember { mutableStateOf(false) }
    
    var selectedSize by remember { mutableIntStateOf(currentSize) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    BackHandler(enabled = true) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> showConfirmQuit = false
            startExpanded -> {
                selectedSize = currentSize
                selectedDifficulty = Difficulty.EASY
                startExpanded = false
            }
            else -> showConfirmQuit = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FutoshikiColors.background()),
        contentAlignment = Alignment.Center
    ) {
        // Dismissal Scrim
        if (startExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { 
                            selectedSize = currentSize
                            selectedDifficulty = Difficulty.EASY
                            startExpanded = false 
                        }
                    )
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxHeight()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.2f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = slideOutVertically(tween(600)) { -it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier),
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

            Box(
                modifier = if (scope != null) {
                    with(scope) {
                        Modifier.animateEnterExit(
                            exit = slideOutVertically(tween(600)) { -it * 2 } + fadeOut(tween(400))
                        )
                    }
                } else Modifier
            ) {
                FutoshikiTitle(fontSize = 46.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = slideOutVertically(tween(600)) { it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier)
            ) {
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
                    val isDark = LocalIsDark.current
                    LandingMenuContent(
                        state = state,
                        isDark = isDark,
                        selectedSize = selectedSize,
                        onSizeSelected = { selectedSize = it },
                        selectedDifficulty = selectedDifficulty,
                        onDifficultyChange = { selectedDifficulty = it },
                        startExpanded = startExpanded,
                        onStartToggle = { startExpanded = !startExpanded },
                        onStart = { onStart(selectedSize, selectedDifficulty) },
                        onTheming = onTheming,
                        onQuit = onQuit,
                        onShowHelp = { showHelp = true },
                        onHideHelp = { showHelp = false },
                        onHideConfirmQuit = { showConfirmQuit = false }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = slideOutVertically(tween(600)) { it * 4 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier)
            ) {
                Text(
                    text = "Made with ♡ by @HeX",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    fontFamily = PixelF
                )
            }
        }
    }
}
