package com.hexcorp.futoshiki.game

import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.ThemeMode

// ── Screen enum ───────────────────────────────────────────────────────────────

enum class Screen { LANDING, GAME, PAUSE, THEMING }

// ── UI State ──────────────────────────────────────────────────────────────────

data class GameState(
    val screen: Screen = Screen.LANDING,
    val previousScreen: Screen = Screen.LANDING,
    val size: Int = 4,
    val puzzle: Puzzle? = null,
    val grid: List<List<Int>> = emptyList(),
    val selected: Pair<Int, Int>? = null,
    val errors: Set<String> = emptySet(),
    val won: Boolean = false,
    val showCongrats: Boolean = false,
    val timerSeconds: Int = 0,
    val timerRunning: Boolean = false,
    val gameKey: Int = 0,
    val theme: AppTheme = AppTheme.FIRE,
    val isDark: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val customMonoAccent: Boolean = false,
    val customDayNight: Boolean = false,
    val isSolved: Boolean = false,
    val difficulty: Difficulty = Difficulty.EASY,
    val ninjaScreenX: Float = 500f,
    val pillOffsetX: Float = 0f,
    val pillOffsetY: Float = 0f,
    val pillCenterX: Float = 0f,
    val pillCenterY: Float = 0f,
    val forceQuitInPause: Boolean = false,
    val showConfirmQuit: Boolean = false,
    val showConfirmNewGame: Boolean = false,
    val showHelp: Boolean = false,
    val defeated: Boolean = false,
    val showDefeat: Boolean = false,
    val mistakeCount: Int = 0,
    val isCountdownActive: Boolean = false,
    val pauseCount: Int = 0,
    val pauseTimeMs: Long = 0L
)
