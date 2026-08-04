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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.components.shared.HelpPanel
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LandingScreen(
    currentSize: Int,
    currentDifficulty: Difficulty,
    onStart: (Int, Difficulty) -> Unit,
    onTheming: () -> Unit,
    onQuit: () -> Unit,
    korgeManager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager,
    modifier: Modifier = Modifier,
    scope: AnimatedVisibilityScope? = null,
    onSizeSave: (Int) -> Unit = {},
    onDifficultySave: (Difficulty) -> Unit = {},
    skipEntranceAnimation: Boolean = false
) {
    var showConfirmQuit by remember { mutableStateOf(false) }
    var startExpanded by remember { mutableStateOf(false) }
    var helpExpanded by remember { mutableStateOf(false) }
    
    var selectedSize by remember { mutableIntStateOf(currentSize) }
    var selectedDifficulty by remember { mutableStateOf(currentDifficulty) }
    val isDark = LocalIsDark.current

    var entranceActive by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val delayTime = if (skipEntranceAnimation) 300L else 1440L
        delay(delayTime)
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
            helpExpanded -> {
                korgeManager.gameWorld?.revertNinjaToStandSprite()
                helpExpanded = false
            }
            showConfirmQuit -> showConfirmQuit = false
            startExpanded -> {
                selectedSize = currentSize
                selectedDifficulty = currentDifficulty
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

        val isSmallScreen = vh < 800.dp
        val headerH = if (isSmallScreen) vh * 0.07f else vh * 0.09f
        val ninjaH = if (isSmallScreen) 100.dp else 135.dp
        val korgeGap = if (isSmallScreen) 14.dp else 16.dp
        val korgeHeight = headerH + korgeGap + ninjaH

        val titleSize = if (isSmallScreen) 42.sp else 46.sp
        val buttonSpacing = if (isSmallScreen) 28.dp else 35.dp
        val titleSpacing = if (isSmallScreen) 28.dp else 40.dp

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
                            selectedDifficulty = currentDifficulty
                            startExpanded = false 
                        }
                    )
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Match game screen layout - same height as korge area
            Spacer(Modifier.height(korgeHeight + if (isSmallScreen) 0.dp else 8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(titleSize.value.dp * 3f)
                    .offset(y = -(vh * 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                val isFirstLaunch = androidx.compose.runtime.remember { !skipEntranceAnimation }
                val composition by com.airbnb.lottie.compose.rememberLottieComposition(com.airbnb.lottie.compose.LottieCompositionSpec.RawRes(com.hexcorp.futoshiki.R.raw.futo))
                val progress by com.airbnb.lottie.compose.animateLottieCompositionAsState(
                    composition,
                    isPlaying = isFirstLaunch,
                    iterations = 1
                )
                val invertMatrix = floatArrayOf(
                    -1f,  0f,  0f, 0f, 255f,
                     0f, -1f,  0f, 0f, 255f,
                     0f,  0f, -1f, 0f, 255f,
                     0f,  0f,  0f, 1f,   0f
                )
                
                val dynamicProperties = com.airbnb.lottie.compose.rememberLottieDynamicProperties(
                    com.airbnb.lottie.compose.rememberLottieDynamicProperty(
                        property = com.airbnb.lottie.LottieProperty.COLOR_FILTER,
                        value = if (isDark) android.graphics.ColorMatrixColorFilter(invertMatrix) else null,
                        keyPath = arrayOf("**")
                    )
                )
                
                com.airbnb.lottie.compose.LottieAnimation(
                    composition = composition,
                    progress = { if (!isFirstLaunch) 1f else progress },
                    dynamicProperties = dynamicProperties,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.05f
                            scaleY = 1.05f
                        }
                )
            }

            Spacer(Modifier.height(titleSpacing))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = entranceOffset - (vh * 0.10f))
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
                        else -> "menu"
                    },
                    transitionSpec = {
                        val duration = 200
                        fadeIn(tween(duration)).togetherWith(fadeOut(tween(duration)))
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
                        currentDifficulty = currentDifficulty,
                        onDifficultySave = onDifficultySave,
                        startExpanded = startExpanded,
                        onStartToggle = { startExpanded = !startExpanded },
                        onStart = {
                            onStart(selectedSize, selectedDifficulty)
                        },
                        onTheming = onTheming,
                        onQuit = onQuit,
                        onShowHelp = {
                            korgeManager.gameWorld?.swapNinjaToReadSprite()
                            helpExpanded = true
                        },
                        onHideHelp = {
                            korgeManager.gameWorld?.revertNinjaToStandSprite()
                            helpExpanded = false
                        },
                        onHideConfirmQuit = { showConfirmQuit = false },
                        isSmallScreen = isSmallScreen
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

        AnimatedVisibility(
            visible = helpExpanded,
            enter = fadeIn(tween(250)) + scaleIn(
                animationSpec = spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessMedium),
                initialScale = 0.92f
            ),
            exit = fadeOut(tween(200)) + scaleOut(
                animationSpec = spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessMedium),
                targetScale = 0.92f
            ),
            modifier = Modifier.zIndex(10f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FutoshikiColors.surface())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                ) {
                    HelpPanel(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(vertical = 12.dp)
                    )
                    Spacer(Modifier.height(40.dp))
                    BigButton(
                        label = "BACK",
                        onClick = {
                            korgeManager.gameWorld?.revertNinjaToStandSprite()
                            helpExpanded = false
                        },
                        inverted = true,
                        isDark = isDark
                    )
                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }
}