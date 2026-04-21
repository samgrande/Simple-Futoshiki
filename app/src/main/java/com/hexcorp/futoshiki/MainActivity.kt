package com.hexcorp.futoshiki

import androidx.activity.viewModels
import android.content.pm.ActivityInfo
import android.util.Log
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hexcorp.futoshiki.game.FutoshikiViewModel
import com.hexcorp.futoshiki.game.Screen
import com.hexcorp.futoshiki.ui.screens.game.GameScreen
import com.hexcorp.futoshiki.ui.screens.landing.LandingScreen
import com.hexcorp.futoshiki.ui.screens.theming.ThemingScreen
import androidx.compose.foundation.isSystemInDarkTheme
import com.hexcorp.futoshiki.game.GodotManager
import com.hexcorp.futoshiki.ui.godot.GodotDragonView
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.FutoshikiTheme
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import org.godotengine.godot.Godot
import org.godotengine.godot.GodotHost
import org.godotengine.godot.GodotFragment

import java.util.*

class MainActivity : FragmentActivity(), GodotHost {
    private val viewModel: FutoshikiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Centralized Back Handling to prevent Godot from intercepting 'Back' as 'Quit'
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val state = viewModel.state.value
                when (state.screen) {
                    Screen.GAME -> viewModel.pause()
                    Screen.PAUSE -> viewModel.resume()
                    Screen.LANDING -> finish()
                    Screen.THEMING -> viewModel.backFromTheming()
                }
            }
        })

        setContent {
            FutoshikiApp(vm = viewModel, onQuit = { finish() })
        }
    }

    override fun getGodot(): Godot? {
        val fragment = supportFragmentManager.findFragmentByTag("godot_dragon") as? GodotFragment
        return fragment?.godot
    }

    override fun getActivity() = this

    override fun onGodotForceQuit(instance: Godot) {
        // No-op: Prevent engine from force-quitting the app
    }

    override fun onGodotRestartRequested(instance: Godot) {
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return
        }
        super.setRequestedOrientation(requestedOrientation)
    }

    override fun onGodotSetupCompleted() {
        getGodot()?.let {
            it.enableImmersiveMode(false)
            it.enableEdgeToEdge(false)
        }
    }

    override fun onGodotMainLoopStarted() {
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun FutoshikiApp(
    vm: FutoshikiViewModel = viewModel(),
    onQuit: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    var godotRef by remember { mutableStateOf<GodotFragment?>(null) }

    val isDark = when (state.themeMode) {
        ThemeMode.AUTO -> systemDark
        ThemeMode.DAY -> false
        ThemeMode.NIGHT -> true
        ThemeMode.BLISS -> {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            hour < 6 || hour >= 18
        }
    }

    val godotManager = remember(godotRef) { godotRef?.let { GodotManager(it) } }

    LaunchedEffect(state.errors, godotManager) {
        godotManager?.let { manager ->
            val aggression = (state.errors.size.toFloat() * 0.2f).coerceAtMost(1.0f)
            manager.updateDragonAggression(aggression)
        }
    }

    // Sync Godot scene pause state with UI state
    LaunchedEffect(state.screen, godotManager) {
        godotManager?.let { manager ->
            // Pause Godot if we are in the Pause menu, Main Menu, or Theming
            val shouldPauseGodot = state.screen != Screen.GAME
            manager.setPaused(shouldPauseGodot)
        }
    }

    FutoshikiTheme(
        theme = state.theme,
        isDark = isDark
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(FutoshikiColors.background())
        ) {
            val vh = maxHeight

            // LAYER 1: UI and Navigation
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
                            state = state.copy(isDark = isDark),
                            godotFragment = godotRef
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

            // LAYER 2: Godot Dragon (Moved AFTER the UI to be ON TOP)
            val headerH = vh * 0.11f
            val totalTopSpace = vh * 0.32f
            val hPad = 20.dp
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(top = headerH + 16.dp)
                    .height(totalTopSpace - headerH - 16.dp)
                    .padding(horizontal = hPad)
                    .clip(RoundedCornerShape(24.dp))
                    .alpha(if (state.screen == Screen.GAME || state.screen == Screen.PAUSE) 1f else 0f)
            ) {
                GodotDragonView(
                    godotFragment = godotRef,
                    onReady = { godotRef = it }
                )
            }
        }
    }
}
