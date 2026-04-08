package com.hex.futoshiki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hex.futoshiki.game.FutoshikiViewModel
import com.hex.futoshiki.game.Screen
import com.hex.futoshiki.ui.screens.GameScreen
import com.hex.futoshiki.ui.screens.LandingScreen
import com.hex.futoshiki.ui.screens.ThemingScreen
import com.hex.futoshiki.ui.theme.FutoshikiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FutoshikiTheme {
                FutoshikiApp(onQuit = { finish() })
            }
        }
    }
}

@Composable
fun FutoshikiApp(
    vm: FutoshikiViewModel = viewModel(),
    onQuit: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()

    FutoshikiTheme(theme = state.theme) {
        // Animate between landing, game, and theming
        AnimatedContent(
            targetState = state.screen,
            transitionSpec = {
                fadeIn(tween(220)) togetherWith fadeOut(tween(180))
            },
            modifier = Modifier.fillMaxSize(),
            label = "screenTransition"
        ) { screen ->
            when (screen) {
                Screen.LANDING -> {
                    LandingScreen(
                        onStart = { vm.newGame(state.size) },
                        onTheming = { vm.goToTheming() },
                        onQuit = onQuit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Screen.GAME, Screen.PAUSE -> {
                    GameScreen(
                        viewModel = vm,
                        state     = state
                    )
                }
                Screen.THEMING -> {
                    ThemingScreen(
                        currentTheme = state.theme,
                        onApply = { theme -> 
                            vm.updateTheme(theme)
                            vm.goToMainMenu()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
