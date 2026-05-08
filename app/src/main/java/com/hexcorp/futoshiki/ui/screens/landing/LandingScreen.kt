package com.hexcorp.futoshiki.ui.screens.landing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import com.hexcorp.futoshiki.game.Difficulty
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import com.hexcorp.futoshiki.ui.korge.KorGEView
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LandingScreen(
    currentSize: Int,
    onStart: (Int, Difficulty) -> Unit,
    onTheming: () -> Unit,
    onQuit: () -> Unit,
    showKorge: Boolean = true,
    korgeManager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager,
    isSkyboxDark: Boolean = false,
    modifier: Modifier = Modifier,
    scope: AnimatedVisibilityScope? = null,
    onSizeSave: (Int) -> Unit = {}
) {
    var showHelp by remember { mutableStateOf(false) }
    var showConfirmQuit by remember { mutableStateOf(false) }
    var startExpanded by remember { mutableStateOf(false) }
    
    var selectedSize by remember { mutableIntStateOf(currentSize) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    val isDark = LocalIsDark.current

    var entranceActive by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(900)
        entranceActive = false
    }

    val entranceProgress by animateFloatAsState(
        targetValue = if (entranceActive) 0f else 1f,
        animationSpec = tween(400),
        label = "entranceProgress"
    )

    val entranceOffset by animateDpAsState(
        targetValue = if (entranceActive) 30.dp else 0.dp,
        animationSpec = tween(400),
        label = "entranceOffset"
    )

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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val vh = maxHeight - navBarBottom

        val isSmallScreen = vh < 720.dp
        val headerH = if (isSmallScreen) vh * 0.07f else vh * 0.09f
        val ninjaH = if (isSmallScreen) 80.dp else 120.dp
        val korgeHeight = headerH + 16.dp + ninjaH

        // KorGEView removed - managed by MainActivity

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
                .padding(horizontal = 20.dp)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Match game screen layout - same height as korge + 16dp
            Spacer(Modifier.height(korgeHeight + 8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FutoshikiTitle(fontSize = 46.sp)
            }

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = entranceOffset)
                    .graphicsLayer { alpha = entranceProgress }
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = fadeOut(tween(200))
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
                        onSizeSelected = { selectedSize = it; onSizeSave(it) },
                        selectedDifficulty = selectedDifficulty,
                        onDifficultyChange = { selectedDifficulty = it },
                        startExpanded = startExpanded,
                        onStartToggle = { startExpanded = !startExpanded },
                        onStart = {
                            korgeManager.gameWorld?.startGame(skipIntro = false)
                            onStart(selectedSize, selectedDifficulty)
                        },
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
                    .graphicsLayer { alpha = entranceProgress }
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = fadeOut(tween(200))
                            )
                        }
                    } else Modifier)
            ) {
                Text(
                    text = "Made with love by @HeX",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    fontFamily = PixelF
                )
            }
        }
    }
}