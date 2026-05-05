package com.hexcorp.futoshiki

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
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
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import com.hexcorp.futoshiki.ui.animations.CircularRevealShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        enableEdgeToEdge()

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }

        root.addView(
            ComposeView(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
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

    // Intermediate transition states
    var blackRevealProgress by remember { mutableFloatStateOf(0f) }
    val animatedBlackReveal by animateFloatAsState(
        targetValue = blackRevealProgress,
        animationSpec = if (blackRevealProgress == 0f) snap() else tween(600, easing = LinearOutSlowInEasing),
        label = "blackReveal"
    )

    // Safety cleanup: Ensure the overlay doesn't get stuck if state changes unexpectedly
    LaunchedEffect(state.screen) {
        if (state.screen != Screen.GAME && state.screen != Screen.LANDING && blackRevealProgress > 0f) {
            blackRevealProgress = 0f
        }
        if (state.screen == Screen.GAME && blackRevealProgress > 0f) {
            // Safety timeout to clear overlay if reveal doesn't complete
            delay(2000)
            blackRevealProgress = 0f
        }
    }

    FutoshikiTheme(
        theme = state.theme,
        isDark = isDark
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = state.screen,
                transitionSpec = {
                    if (targetState == Screen.GAME && initialState == Screen.LANDING) {
                        fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                    } else if (targetState == Screen.THEMING || initialState == Screen.THEMING) {
                        fadeIn(tween(500, easing = EaseInOutQuart)) togetherWith 
                        fadeOut(tween(500, easing = EaseInOutQuart))
                    } else {
                        fadeIn(tween(220, easing = FastOutSlowInEasing)) togetherWith 
                        fadeOut(tween(180, easing = FastOutSlowInEasing))
                    }
                },
                modifier = Modifier.fillMaxSize(),
                label = "screenTransition",
                contentKey = { screen ->
                    val fromGame = state.previousScreen == Screen.PAUSE
                    if (screen == Screen.GAME || screen == Screen.PAUSE ||
                        (screen == Screen.THEMING && fromGame)) "GAME_GROUP" else screen
                }
            ) { screen ->
                val isGameReveal = screen == Screen.GAME && state.previousScreen == Screen.LANDING
                
                var gameRevealTarget by remember(screen) { 
                    mutableFloatStateOf(if (isGameReveal) 0f else 1f) 
                }
                
                LaunchedEffect(isGameReveal) {
                    if (isGameReveal) {
                        // Start game reveal immediately (we already waited for black in onStart)
                        gameRevealTarget = 1f
                        // Keep black overlay active until game reveal is finished (400ms + buffer)
                        delay(500)
                        blackRevealProgress = 0f
                    }
                }

                val gameRevealProgress by animateFloatAsState(
                    targetValue = gameRevealTarget,
                    animationSpec = if (isGameReveal) {
                        tween(400, easing = FastOutSlowInEasing)
                    } else {
                        snap()
                    },
                    label = "gameRevealProgress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isGameReveal) {
                            Modifier
                                .zIndex(5f)
                                .graphicsLayer {
                                    clip = true
                                    shape = CircularRevealShape(gameRevealProgress)
                                }
                        } else Modifier)
                ) {
                    when (screen) {
                        Screen.LANDING -> {
                            LandingScreen(
                                currentSize = state.size,
                                onStart = { size, difficulty -> 
                                    scope.launch {
                                        blackRevealProgress = 1f
                                        delay(650) 
                                        vm.newGame(size, difficulty)
                                    }
                                },
                                onTheming = { vm.goToTheming() },
                                onQuit = onQuit,
                                modifier = Modifier.fillMaxSize(),
                                scope = this@AnimatedContent
                            )
                        }

                        Screen.GAME, Screen.PAUSE -> {
                            GameScreen(
                                viewModel = vm,
                                state = state.copy(isDark = isDark)
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

            // Global Transition Overlay
            if (animatedBlackReveal > 0f) {
                val overlayColor = if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                        .graphicsLayer {
                            clip = true
                            shape = CircularRevealShape(animatedBlackReveal)
                        }
                        .background(overlayColor)
                )
            }
        }
    }
}
