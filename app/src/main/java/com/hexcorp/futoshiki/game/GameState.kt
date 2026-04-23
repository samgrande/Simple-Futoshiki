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
    val isSolved: Boolean = false
)
