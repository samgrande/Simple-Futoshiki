package com.hexcorp.futoshiki

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.hexcorp.futoshiki.ui.theme.ThemeMode
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

    val isDark = when (state.themeMode) {
        ThemeMode.AUTO -> systemDark
        ThemeMode.DAY -> false
        ThemeMode.NIGHT -> true
        ThemeMode.BLISS -> {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            hour < 6 || hour >= 18
        }
    }

    FutoshikiTheme(
        theme = state.theme,
        isDark = isDark
    ) {
        AnimatedContent(
            targetState = state.screen,
            transitionSpec = {
                if (targetState == Screen.THEMING || initialState == Screen.THEMING) {
                    fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                } else {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                }
            },
            modifier = Modifier.fillMaxSize(),
            label = "screenTransition",
            contentKey = { screen ->
                if (screen == Screen.GAME || screen == Screen.PAUSE) "GAME_GROUP" else screen
            }
        ) { screen ->
            when (screen) {
                Screen.LANDING -> {
                    LandingScreen(
                        onStart = { vm.newGame(state.size) },
                        onTheming = { vm.goToTheming() },
                        onQuit = onQuit,
                        modifier = Modifier.fillMaxSize(),
                        scope = this
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
                        onThemeModeChange = { mode -> vm.updateThemeMode(mode) },
                        onThemeChange = { theme -> vm.updateTheme(theme) },
                        onBack = { vm.backFromTheming() },
                        modifier = Modifier.fillMaxSize(),
                        scope = this
                    )
                }
            }
        }
    }
}
