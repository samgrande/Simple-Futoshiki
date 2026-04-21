package com.hexcorp.futoshiki

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hexcorp.futoshiki.game.FutoshikiViewModel
import com.hexcorp.futoshiki.game.Screen
import com.hexcorp.futoshiki.ui.screens.game.GameScreen
import com.hexcorp.futoshiki.ui.screens.landing.LandingScreen
import com.hexcorp.futoshiki.ui.screens.theming.ThemingScreen
import com.hexcorp.futoshiki.ui.theme.FutoshikiTheme
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import org.godotengine.godot.Godot
import org.godotengine.godot.GodotFragment
import org.godotengine.godot.GodotHost
import java.util.*

private const val GODOT_FRAGMENT_TAG = "godot_fragment"

data class GodotBounds(val left: Int, val top: Int, val width: Int, val height: Int) {
    companion object {
        val Hidden = GodotBounds(0, 0, 0, 0)
    }
}

val LocalGodotBounds = staticCompositionLocalOf<(GodotBounds) -> Unit> { {} }

class MainActivity : FragmentActivity(), GodotHost {

    private var godotFragment by mutableStateOf<GodotFragment?>(null)
    private var godotContainer: FrameLayout? = null

    override fun getActivity(): Activity = this

    override fun getGodot(): Godot? = godotFragment?.getGodot()

    override fun getCommandLine(): List<String> = listOf("--rendering-driver", "opengl3")

    // Godot's project settings default `display/window/handheld/orientation` to landscape,
    // and the engine calls setRequestedOrientation() on the host activity during init.
    // Ignore those requests — the manifest already locks MainActivity to portrait.
    override fun setRequestedOrientation(requestedOrientation: Int) {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    // Godot's SurfaceView consumes BACK and crashes on it. Intercept hardware
    // key events at the Activity and route BACK through OnBackPressedDispatcher
    // so Compose BackHandlers win before any focused engine view sees the key.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) {
                onBackPressedDispatcher.onBackPressed()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    fun updateGodotBounds(bounds: GodotBounds) {
        val container = godotContainer ?: return
        val lp = container.layoutParams as? FrameLayout.LayoutParams ?: return
        if (bounds.width == 0 || bounds.height == 0) {
            container.visibility = View.GONE
        } else {
            container.visibility = View.VISIBLE
            lp.leftMargin = bounds.left
            lp.topMargin = bounds.top
            lp.width = bounds.width
            lp.height = bounds.height
            container.layoutParams = lp
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        enableEdgeToEdge()

        val root = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        val godotContainerId = View.generateViewId()
        val container = FrameLayout(this).apply {
            id = godotContainerId
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            visibility = View.GONE
            // Prevent Godot's SurfaceView from stealing focus and intercepting
            // hardware keys (e.g. BACK). Key events must reach the Activity /
            // Compose BackHandler, not the embedded engine.
            isFocusable = false
            isFocusableInTouchMode = false
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
        godotContainer = container
        root.addView(
            container,
            FrameLayout.LayoutParams(0, 0)
        )

        root.addView(
            ComposeView(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setContent {
                    CompositionLocalProvider(
                        LocalGodotBounds provides { bounds -> updateGodotBounds(bounds) }
                    ) {
                        FutoshikiApp(godotFragment = godotFragment, onQuit = { finish() })
                    }
                }
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)

        try {
            val existing = supportFragmentManager.findFragmentByTag(GODOT_FRAGMENT_TAG) as? GodotFragment
            if (existing != null) {
                godotFragment = existing
                supportFragmentManager.beginTransaction()
                    .replace(godotContainerId, existing, GODOT_FRAGMENT_TAG)
                    .commitNow()
            } else {
                val fragment = GodotFragment()
                godotFragment = fragment
                supportFragmentManager.beginTransaction()
                    .replace(godotContainerId, fragment, GODOT_FRAGMENT_TAG)
                    .commitNow()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Godot init failed: ${e.message}")
            godotFragment = null
        }
    }
}

@Composable
fun FutoshikiApp(
    vm: FutoshikiViewModel = viewModel(),
    godotFragment: GodotFragment? = null,
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

    val setGodotBounds = LocalGodotBounds.current
    LaunchedEffect(state.screen) {
        if (state.screen != Screen.GAME && state.screen != Screen.PAUSE) {
            setGodotBounds(GodotBounds.Hidden)
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
                        state = state.copy(isDark = isDark),
                        godotFragment = godotFragment
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
