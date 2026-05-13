package com.hexcorp.futoshiki

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hexcorp.futoshiki.game.FutoshikiViewModel
import com.hexcorp.futoshiki.game.Screen
import com.hexcorp.futoshiki.ui.screens.game.GameScreen
import com.hexcorp.futoshiki.ui.screens.landing.LandingScreen
import com.hexcorp.futoshiki.ui.screens.theming.ThemingScreen
import com.hexcorp.futoshiki.ui.theme.FutoshikiTheme
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.animations.CircularRevealShape
import com.hexcorp.futoshiki.ui.korge.KorGEView
import com.hexcorp.futoshiki.ui.components.shared.TimerPill
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        enableEdgeToEdge()

        val root = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        root.addView(
            ComposeView(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setContent {
                    FutoshikiApp(onQuit = { finish() })
                }
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) {
                onBackPressedDispatcher.onBackPressed()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}

@Composable
fun FutoshikiApp(
    vm: FutoshikiViewModel = viewModel(),
    onQuit: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    val isDark = when (state.themeMode) {
        ThemeMode.AUTO -> systemDark
        ThemeMode.DAY -> false
        ThemeMode.NIGHT -> true
        ThemeMode.CUSTOM -> state.customDayNight
    }

    val isLanding = state.screen == Screen.LANDING
    val isGame = state.screen == Screen.GAME
    val isPaused = state.screen == Screen.PAUSE

    val isSkyboxDark = when (state.themeMode) {
        ThemeMode.CUSTOM -> state.customMonoAccent
        ThemeMode.AUTO -> isDark
        ThemeMode.DAY -> false
        ThemeMode.NIGHT -> false
    }

    var previousScreen by remember { mutableStateOf<Screen?>(null) }
    var landingEntrancePlayed by remember { mutableStateOf(false) }
    var landingEntranceDone by remember { mutableStateOf(false) }

    LaunchedEffect(state.screen) {
        when (state.screen) {
            Screen.LANDING -> {
                if (!landingEntrancePlayed) {
                    landingEntrancePlayed = true
                    val isInitialLaunch = previousScreen == null
                    val delayTime = if (isInitialLaunch) 1400L else 300L
                    delay(delayTime)
                    landingEntranceDone = true
                }
            }
            else -> {
                landingEntrancePlayed = false
                landingEntranceDone = false
            }
        }
        previousScreen = state.screen
    }

    val showCover = state.screen == Screen.LANDING && !landingEntranceDone
    val isInitialLaunch = previousScreen == null

    var blackRevealProgress by remember { mutableFloatStateOf(0f) }
    val animatedBlackReveal by animateFloatAsState(
        targetValue = blackRevealProgress,
        animationSpec = if (blackRevealProgress == 0f) snap() else tween(600, easing = LinearOutSlowInEasing),
        label = "blackReveal"
    )

    LaunchedEffect(state.screen) {
        if (state.screen != Screen.GAME && state.screen != Screen.LANDING && blackRevealProgress > 0f) {
            blackRevealProgress = 0f
        }
    }

    FutoshikiTheme(
        theme = state.theme,
        isDark = isDark
    ) {
        var mainContainerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
        BoxWithConstraints(modifier = Modifier.fillMaxSize().onGloballyPositioned { mainContainerCoords = it }) {
            val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val vh = maxHeight - navBarBottom
            
            val isSmallScreen = vh < 800.dp
            val headerH = if (isSmallScreen) vh * 0.07f else vh * 0.09f
            val ninjaH = if (isSmallScreen) 100.dp else 135.dp
            val korgeGap = if (isSmallScreen) 14.dp else 16.dp
            val korgeHeight = headerH + korgeGap + ninjaH

// 1. Shared KorGE rendered once at the top (hidden but kept for fast theme exit)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(korgeHeight)
                    .zIndex(5f)
            ) {
                KorGEView(
                    manager = vm.korgeManager,
                    isSkyboxDark = isSkyboxDark,
                    isPaused = isPaused,
                    isMenuScreen = isLanding,
                    modifier = Modifier.fillMaxSize()
                )

                AnimatedVisibility(
                    visible = showCover,
                    enter = EnterTransition.None,
                    exit = fadeOut(animationSpec = tween(600))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FutoshikiColors.background())
                    )
                }

                if (state.screen == Screen.THEMING) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(korgeHeight)
                            .background(FutoshikiColors.background())
                    )
                }

                // Timer pill - rendered here so it appears above the KorGE AndroidView
                // zIndex(15f) to stay above pause dim overlay (zIndex 10f)
                if ((isGame || isPaused) && !state.isSolved && !state.showDefeat && !state.showCongrats) {
                    val isCustomMonoNight = state.themeMode == ThemeMode.CUSTOM && !state.customMonoAccent && state.customDayNight
                    val isCustomAccentDark = state.themeMode == ThemeMode.CUSTOM && state.customMonoAccent == false && isDark
                    val gameTimerColor = if (isCustomAccentDark) Color.White else Color.Black
                    CompositionLocalProvider(LocalIsDark provides if (isCustomMonoNight) false else LocalIsDark.current) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 420.dp)
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(start = 20.dp, end = 22.dp, top = if (isSmallScreen) 7.dp else 11.dp)
                                .zIndex(15f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            TimerPill(
                                seconds = state.timerSeconds,
                                won = state.won,
                                isPaused = isPaused,
                                enabled = !state.won && !state.isCountdownActive,
                                onClick = {
                                    Log.d("FutoshikiDebug", "TimerPill clicked! isPaused=$isPaused, enabled=${!state.won && !state.isCountdownActive}")
                                    if (!isPaused) vm.pause() else vm.resume()
                                },
                                textColor = gameTimerColor,
                                showPill = isPaused,
                                modifier = Modifier
                                    .onGloballyPositioned { coords ->
                                        mainContainerCoords?.let { container ->
                                            if (container.isAttached && coords.isAttached) {
                                                val localPos = container.localPositionOf(coords, Offset.Zero)
                                                val center = Offset(
                                                    localPos.x + coords.size.width / 2f,
                                                    localPos.y + coords.size.height / 2f
                                                )
                                                vm.updatePillPosition(localPos, center)
                                            }
                                        }
                                    }
                                    .graphicsLayer { alpha = if (state.won) 0f else 1f }
                            )
                        }
                    }
                }

                // Pause overlay - dim + PAUSED text on top of korge
                if (state.screen == Screen.PAUSE) {
                    val useSolidBg = state.showConfirmQuit || state.showConfirmNewGame || state.showHelp
                    val bgColor = if (useSolidBg) FutoshikiColors.background() else if (isDark) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.5f)
                    val textColor = if (isDark) Color.White else Color.Black
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgColor)
                            .zIndex(10f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P A U S E D",
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelF,
                            letterSpacing = 4.sp
                        )
                    }
                }

                // Solution overlay - dim + SOLUTION text on top of korge
                if (state.screen == Screen.GAME && state.isSolved && !state.showCongrats) {
                    val dimColor = if (isDark) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                    val textColor = if (isDark) Color.White else Color.Black
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(dimColor)
                            .zIndex(10f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S O L U T I O N",
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PixelF,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }

            // 2. Screen Content
            AnimatedContent(
                targetState = state.screen,
                transitionSpec = {
                    if (targetState == Screen.GAME && initialState == Screen.LANDING) {
                        // Instant switch for seamless korge, content handles its own entrance
                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    } else if ((targetState == Screen.GAME && initialState == Screen.PAUSE) ||
                               (targetState == Screen.PAUSE && initialState == Screen.GAME)) {
                        // Instant switch between game/pause — both use GameScreen composable
                        // PauseOverlay handles its own animation internally
                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    } else if (targetState == Screen.THEMING || initialState == Screen.THEMING) {
                        (fadeIn(tween(300, easing = FastOutSlowInEasing)) togetherWith 
                        fadeOut(tween(0)))
                        .using(SizeTransform(clip = false))
                    } else {
                        fadeIn(tween(220, easing = FastOutSlowInEasing)) togetherWith 
                        fadeOut(tween(180, easing = FastOutSlowInEasing))
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(FutoshikiColors.background())
                    .then(
                        if (state.screen == Screen.THEMING) Modifier.zIndex(10f)
                        else Modifier
                    ),
                label = "screenTransition",
                contentKey = { screen -> screen }
            ) { screen ->
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(FutoshikiColors.background())
                ) {
                    when (screen) {
                        Screen.LANDING -> {
                            LandingScreen(
                                currentSize = state.size,
                                currentDifficulty = state.difficulty,
                                onStart = { size, difficulty -> 
                                    vm.newGame(size, difficulty)
                                },
                                onTheming = { vm.goToTheming() },
                                onQuit = onQuit,
                                showKorge = false,
                                korgeManager = vm.korgeManager,
                                isSkyboxDark = isSkyboxDark,
                                modifier = Modifier.fillMaxSize(),
                                scope = this@AnimatedContent,
                                onSizeSave = { vm.saveSizePreference(it) },
                                onDifficultySave = { vm.saveDifficultyPreference(it) },
                                skipEntranceAnimation = !isInitialLaunch
                            )
                        }

                        Screen.GAME, Screen.PAUSE -> {
                            GameScreen(
                                viewModel = vm,
                                state = state.copy(isDark = isDark),
                                showKorge = false // Managed by MainActivity
                            )
                        }

                        Screen.THEMING -> {
                            ThemingScreen(
                                currentTheme = state.theme,
                                themeMode = state.themeMode,
                                isDark = isDark,
                                customMonoAccent = state.customMonoAccent,
                                customDayNight = state.customDayNight,
                                onThemeModeChange = { mode -> vm.updateThemeMode(mode) },
                                onThemeChange = { theme -> vm.updateTheme(theme) },
                                onCustomThemeChange = { monoAccent, dayNight ->
                                    vm.updateCustomMonoAccent(monoAccent)
                                    vm.updateCustomDayNight(dayNight)
                                },
                                onBack = { vm.backFromTheming() },
                                modifier = Modifier.fillMaxSize(),
                                scope = this@AnimatedContent
                            )
                        }
                    }
                }
            }
        }
    }

}