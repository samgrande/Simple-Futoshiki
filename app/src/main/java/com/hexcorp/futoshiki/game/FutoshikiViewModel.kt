package com.hexcorp.futoshiki.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import com.hexcorp.futoshiki.ui.korge.KorGEGameManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

class FutoshikiViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("futoshiki_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(GameState(
        theme = loadTheme(),
        themeMode = loadThemeMode(),
        isDark = loadIsDark(),
        customMonoAccent = loadCustomMonoAccent(),
        customDayNight = loadCustomDayNight(),
        size = loadSize()
    ))
    val state: StateFlow<GameState> = _state.asStateFlow()

    val korgeManager = KorGEGameManager()

    private var timerJob: Job? = null

    private fun loadTheme(): AppTheme {
        val themeName = prefs.getString("app_theme", AppTheme.FIRE.name)
        return try {
            AppTheme.valueOf(themeName ?: AppTheme.FIRE.name)
        } catch (e: Exception) {
            AppTheme.FIRE
        }
    }

    private fun loadIsDark(): Boolean {
        return prefs.getBoolean("is_dark", false)
    }

    private fun loadThemeMode(): ThemeMode {
        val modeName = prefs.getString("theme_mode", ThemeMode.AUTO.name)
        return try {
            ThemeMode.valueOf(modeName ?: ThemeMode.AUTO.name)
        } catch (e: Exception) {
            ThemeMode.AUTO
        }
    }

    private fun loadSize(): Int {
        return prefs.getInt("game_size", 4)
    }

    private fun loadCustomMonoAccent(): Boolean {
        return prefs.getBoolean("custom_mono_accent", false)
    }

    private fun loadCustomDayNight(): Boolean {
        return prefs.getBoolean("custom_day_night", false)
    }

    // ── New game ─────────────────────────────────────────────────────────────

    fun newGame(size: Int, difficulty: Difficulty = Difficulty.EASY) {
        prefs.edit().putInt("game_size", size).apply()
        val puzzle = generatePuzzle(size, difficulty)
        val grid = puzzle.initial.map { it.toMutableList().toList() }
        stopTimer()
        korgeManager.updateAggression(0f)
        korgeManager.resetBoost()
        korgeManager.introFinished = false
        korgeManager.resetRunningStarted()
        korgeManager.resetSceneLoaded()
        _state.update { st ->
            st.copy(
                previousScreen = st.screen,
                screen = Screen.GAME,
                size = size,
                puzzle = puzzle,
                grid = grid,
                selected = null,
                errors = emptySet(),
                won = false,
                isSolved = false,
                showCongrats = false,
                timerSeconds = 0,
                timerRunning = true,
                gameKey = st.gameKey + 1,
                completedRowsCount = 0,
                finishedRows = emptySet(),
                finishedCols = emptySet(),
                difficulty = difficulty,
                ninjaScreenX = 500f
            )
        }
        korgeManager.updateNinjaScreenX(500f)
    }

    // ── Cell input ───────────────────────────────────────────────────────────

    fun inputNumber(num: Int) {
        val st = _state.value
        val (r, c) = st.selected ?: return
        if (st.won || st.puzzle == null) return
        if (st.puzzle.initial[r][c] != 0) return   // given cell — locked

        val newGrid = st.grid.mapIndexed { ri, row ->
            if (ri == r) row.toMutableList().also { it[c] = num } else row
        }
        val errors = validateGrid(newGrid, st.size, st.puzzle)
        val won = isWon(newGrid, errors)

        val newNinjaScreenX = calculateNinjaScreenX(newGrid, st.size, st.puzzle.solution)
        korgeManager.updateNinjaScreenX(newNinjaScreenX)

        _state.update { it.copy(
            grid = newGrid, 
            errors = errors, 
            won = won, 
            ninjaScreenX = newNinjaScreenX
        ) }

        if (won) {
            _state.update { it.copy(won = true, showCongrats = true) }
            stopTimer()
            korgeManager.gameWorld?.runWinSequence()
        } else if (errors.isNotEmpty()) {
            korgeManager.updateAggression(0.5f)
        } else {
            korgeManager.updateAggression(0f)
        }
    }

    fun clearCell(r: Int, c: Int) {
        val st = _state.value
        if (st.won || st.puzzle == null) return
        if (st.puzzle.initial[r][c] != 0) return
        val newGrid = st.grid.mapIndexed { ri, row ->
            if (ri == r) row.toMutableList().also { it[c] = 0 } else row
        }
        val errors = validateGrid(newGrid, st.size, st.puzzle)
        val newNinjaScreenX = calculateNinjaScreenX(newGrid, st.size, st.puzzle.solution)
        korgeManager.updateNinjaScreenX(newNinjaScreenX)
        _state.update { it.copy(grid = newGrid, errors = errors, won = false, ninjaScreenX = newNinjaScreenX) }
    }

    fun clearSelectedCell() {
        val (r, c) = _state.value.selected ?: return
        clearCell(r, c)
    }

    fun clearAll() {
        val st = _state.value
        if (st.puzzle == null || st.won) return
        val newGrid = st.puzzle.initial.map { it.toList() }
        _state.update { it.copy(grid = newGrid, errors = emptySet(), selected = null) }
    }

    // ── Selection ────────────────────────────────────────────────────────────

    fun selectCell(r: Int, c: Int) {
        val st = _state.value
        if (st.puzzle?.initial?.get(r)?.get(c) != 0) {
            _state.update { it.copy(selected = null) }
            return
        }
        _state.update { it.copy(selected = r to c) }
    }

    fun deselectCell() {
        _state.update { it.copy(selected = null) }
    }

    fun moveSelection(dr: Int, dc: Int) {
        val st = _state.value
        val (r, c) = st.selected ?: return
        val nr = (r + dr).coerceIn(0, st.size - 1)
        val nc = (c + dc).coerceIn(0, st.size - 1)
        _state.update { it.copy(selected = nr to nc) }
    }

    fun updatePillPosition(offset: androidx.compose.ui.geometry.Offset, center: androidx.compose.ui.geometry.Offset) {
        _state.update { it.copy(
            pillOffsetX = offset.x,
            pillOffsetY = offset.y,
            pillCenterX = center.x,
            pillCenterY = center.y
        ) }
    }

    // ── Countdown timer hold ─────────────────────────────────────────────────

    fun pauseTimer() { stopTimer() }

    fun resumeTimer() {
        if (_state.value.screen == Screen.GAME && !_state.value.won) startTimer()
    }

    // ── Pause / Resume ───────────────────────────────────────────────────────

    fun pause() {
        if (_state.value.screen != Screen.GAME) return
        stopTimer()
        _state.update { it.copy(previousScreen = it.screen, screen = Screen.PAUSE, timerRunning = false) }
    }

    fun resume() {
        val isWon = _state.value.won
        _state.update { it.copy(previousScreen = it.screen, screen = Screen.GAME, timerRunning = !isWon) }
        if (!isWon) {
            startTimer()
        }
    }

    fun goToMainMenu() {
        stopTimer()
        _state.update { it.copy(previousScreen = it.screen, screen = Screen.LANDING) }
    }

    fun goToTheming() {
        stopTimer()
        _state.update { it.copy(previousScreen = it.screen, screen = Screen.THEMING) }
    }

    fun goToThemingFromGame() {
        _state.update { it.copy(previousScreen = it.screen, screen = Screen.THEMING) }
    }

    fun backFromTheming() {
        val prev = _state.value.previousScreen
        if (prev == Screen.PAUSE || prev == Screen.GAME) {
            _state.update { it.copy(previousScreen = it.screen, screen = Screen.PAUSE) }
        } else {
            goToMainMenu()
        }
    }

    fun updateTheme(newTheme: AppTheme) {
        prefs.edit().putString("app_theme", newTheme.name).apply()
        _state.update { it.copy(theme = newTheme) }
    }

    fun updateThemeMode(newMode: ThemeMode) {
        prefs.edit().putString("theme_mode", newMode.name).apply()
        _state.update { it.copy(themeMode = newMode) }
    }

    fun toggleDarkMode() {
        val newIsDark = !_state.value.isDark
        prefs.edit().putBoolean("is_dark", newIsDark).apply()
        _state.update { it.copy(isDark = newIsDark) }
    }

    fun updateCustomMonoAccent(isAccent: Boolean) {
        prefs.edit().putBoolean("custom_mono_accent", isAccent).apply()
        _state.update { it.copy(customMonoAccent = isAccent) }
    }

    fun updateCustomDayNight(isNight: Boolean) {
        prefs.edit().putBoolean("custom_day_night", isNight).apply()
        _state.update { it.copy(customDayNight = isNight) }
    }

    // ── Solve (cheat) ────────────────────────────────────────────────────────

    fun solve() {
        val puzzle = _state.value.puzzle ?: return
        korgeManager.gameWorld?.runSolveSequence()
        _state.update { st ->
            st.copy(
                previousScreen = st.screen,
                grid = puzzle.solution.map { it.toList() },
                errors = emptySet(),
                selected = null,
                won = true,
                isSolved = true,
                timerRunning = false,
                screen = Screen.GAME
            )
        }
        // Trigger the KorGE solve animation: dragon flies away, ninja stands immediately
        korgeManager.gameWorld?.runWinSequence(immediate = true)
    }

    // ── Size change ──────────────────────────────────────────────────────────

    fun changeSize(newSize: Int) {
        prefs.edit().putInt("game_size", newSize).apply()
        _state.update { it.copy(size = newSize) }
        newGame(newSize)
    }

    // ── Timer internals ──────────────────────────────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(timerSeconds = it.timerSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private fun calculateNinjaScreenX(grid: List<List<Int>>, size: Int, solution: List<List<Int>>): Float {
        var correct = 0
        var incorrect = 0
        for (r in 0 until size) {
            val row = grid[r]
            if (row.all { it != 0 }) {
                if (row == solution[r]) correct++ else incorrect++
            }
        }
        for (c in 0 until size) {
            val col = (0 until size).map { grid[it][c] }
            val solCol = (0 until size).map { solution[it][c] }
            if (col.all { it != 0 }) {
                if (col == solCol) correct++ else incorrect++
            }
        }
        val base = 500f
        val forwardStep = 70f
        val backwardStep = 110f
        val offset = (correct * forwardStep) - (incorrect * backwardStep)
        return (base + offset).coerceIn(100f, 750f)
    }
}
