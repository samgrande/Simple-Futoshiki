package com.hex.futoshiki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.hex.futoshiki.ui.theme.FutoshikiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FutoshikiTheme {
                FutoshikiApp()
            }
        }
    }
}

@Composable
fun FutoshikiApp(
    vm: FutoshikiViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    // Animate between landing and game
    AnimatedContent(
        targetState = state.screen == Screen.LANDING,
        transitionSpec = {
            if (targetState) {
                // going to landing
                fadeIn(tween(220)) togetherWith fadeOut(tween(180))
            } else {
                // going to game
                fadeIn(tween(220)) togetherWith fadeOut(tween(180))
            }
        },
        modifier = Modifier.fillMaxSize(),
        label = "screenTransition"
    ) { isLanding ->
        if (isLanding) {
            LandingScreen(
                onStart = { vm.newGame(state.size) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            GameScreen(
                viewModel = vm,
                state     = state
            )
        }
    }
}
