package com.hexcorp.futoshiki.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hexcorp.futoshiki.audio.Sound
import com.hexcorp.futoshiki.audio.SoundManager
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import com.hexcorp.futoshiki.ui.korge.KorGEGameManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.util.Log
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
        size = loadSize(),
        difficulty = loadDifficulty()
    ))
    val state: StateFlow<GameState> = _state.asStateFlow()

    init {
        SoundManager.init(application)
    }

    val korgeManager = KorGEGameManager()

    private var timerJob: Job? = null
    private var pauseStartTime: Long? = null

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
            val mode = ThemeMode.valueOf(modeName ?: ThemeMode.AUTO.name)
            if (mode == ThemeMode.CUSTOM) ThemeMode.AUTO else mode
        } catch (e: Exception) {
            ThemeMode.AUTO
        }
    }

    private fun loadSize(): Int {
        return prefs.getInt("game_size", 4)
    }

    private fun loadDifficulty(): Difficulty {
        val name = prefs.getString("difficulty", Difficulty.EASY.name)
        return try {
            Difficulty.valueOf(name ?: Difficulty.EASY.name)
        } catch (e: Exception) {
            Difficulty.EASY
        }
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
        SoundManager.play(Sound.START)
        val puzzle = generatePuzzle(size, difficulty)
        val grid = puzzle.initial.map { it.toMutableList().toList() }
        stopTimer()
        korgeManager.updateAggression(0f)
        korgeManager.resetBoost()
        
        korgeManager.resetRunningStarted()
        korgeManager.resetSceneLoaded()
        korgeManager.introFinished = false
        korgeManager.resetNinjaPosition()
        korgeManager.cancelCurrentScene()
        korgeManager.playRestart()
        korgeManager.signalGameRestart()
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
                defeated = false,
                showDefeat = false,
                timerSeconds = 0,
                timerRunning = true,
                gameKey = st.gameKey + 1,

                difficulty = difficulty,
                ninjaScreenX = 500f,
                mistakeCount = 0,
                isCountdownActive = false,
                forceQuitInPause = false,
                pauseCount = 0,
                pauseTimeMs = 0L
            )
        }
        pauseStartTime = null
        korgeManager.updateNinjaScreenX(500f)
    }

    // ── Cell input ───────────────────────────────────────────────────────────

    fun inputNumber(num: Int) {
        val st = _state.value
        val (r, c) = st.selected ?: return
        if (st.won || st.defeated || st.puzzle == null) return
        if (st.puzzle.initial[r][c] != 0) return

        val newGrid = st.grid.mapIndexed { ri, row ->
            if (ri == r) row.toMutableList().also { it[c] = num } else row
        }
        val errors = validateGrid(newGrid, st.size, st.puzzle)
        val won = isWon(newGrid, errors)

        // Mistake: every wrong cell input counts
        val isMistake = num != 0 && num != st.puzzle.solution[r][c]
        val newMistakeCount = if (isMistake) st.mistakeCount + 1 else st.mistakeCount

        // Row just became correctly complete → reset mistake counter
        val prevRowFull = st.grid[r].all { it != 0 }
        val newRowFull = newGrid[r].all { it != 0 }
        val rowJustCompleted = !prevRowFull && newRowFull && newGrid[r] == st.puzzle.solution[r]

        // Column just became correctly complete → also resets mistakes
        val prevColFull = (0 until st.size).all { ri -> st.grid[ri][c] != 0 }
        val newColFull = (0 until st.size).all { ri -> newGrid[ri][c] != 0 }
        val colSolution = (0 until st.size).map { ri -> st.puzzle.solution[ri][c] }
        val newCol = (0 until st.size).map { ri -> newGrid[ri][c] }
        val colJustCompleted = !prevColFull && newColFull && newCol == colSolution

        val finalMistakeCount = if (rowJustCompleted || colJustCompleted) 0 else newMistakeCount

        if (Log.isLoggable("FutoshikiDebug", Log.DEBUG)) Log.d("FutoshikiDebug", "inputNumber($num) at ($r,$c): solution=${st.puzzle.solution[r][c]}, isMistake=$isMistake, mistakes=$finalMistakeCount, rowComplete=$rowJustCompleted, colComplete=$colJustCompleted")

        val newNinjaScreenX = calculateNinjaScreenX(newGrid, st.size, st.puzzle.solution, finalMistakeCount)
        // Defeat after 6 mistakes (not position-based)
        val defeated = !won && finalMistakeCount >= 6

        korgeManager.updateNinjaScreenX(newNinjaScreenX)

        _state.update { it.copy(
            grid = newGrid,
            errors = errors,
            won = won,
            ninjaScreenX = newNinjaScreenX,
            mistakeCount = finalMistakeCount,
            defeated = defeated,
            showDefeat = defeated,
            timerRunning = !won && !defeated
        ) }

        if (won) {
            _state.update { it.copy(showCongrats = true) }
            stopTimer()
            SoundManager.play(Sound.WIN)
            korgeManager.gameWorld?.runWinSequence()
        } else if (defeated) {
            stopTimer()
            SoundManager.play(Sound.LOSS)
            korgeManager.gameWorld?.runDefeatSequence()
        } else if (errors.isNotEmpty()) {
            SoundManager.play(Sound.ERROR)
            korgeManager.updateAggression(0.5f)
        } else {
            korgeManager.updateAggression(0f)
        }
    }

    fun clearCell(r: Int, c: Int) {
        val st = _state.value
        if (st.won || st.defeated || st.puzzle == null) return
        if (st.puzzle.initial[r][c] != 0) return
        val newGrid = st.grid.mapIndexed { ri, row ->
            if (ri == r) row.toMutableList().also { it[c] = 0 } else row
        }
        val errors = validateGrid(newGrid, st.size, st.puzzle)
        val newNinjaScreenX = calculateNinjaScreenX(newGrid, st.size, st.puzzle.solution, st.mistakeCount)
        korgeManager.updateNinjaScreenX(newNinjaScreenX)
        SoundManager.play(Sound.SELECT)
        _state.update { it.copy(grid = newGrid, errors = errors, won = false, ninjaScreenX = newNinjaScreenX) }
    }

    fun clearSelectedCell() {
        val (r, c) = _state.value.selected ?: return
        clearCell(r, c)
    }

    fun clearAll() {
        val st = _state.value
        if (st.puzzle == null || st.won || st.defeated) return
        val newGrid = st.puzzle.initial.map { it.toList() }
        _state.update { it.copy(grid = newGrid, errors = emptySet(), selected = null) }
    }

    // ── Selection ────────────────────────────────────────────────────────────

    fun selectCell(r: Int, c: Int) {
        val st = _state.value
        if (st.puzzle?.initial?.get(r)?.get(c) != 0) {
            SoundManager.play(Sound.WRONG)
            _state.update { it.copy(selected = null) }
            return
        }
        SoundManager.play(Sound.TAP)
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

    fun setForceQuitInPause(show: Boolean) {
        _state.update { it.copy(forceQuitInPause = show) }
    }

    fun setShowConfirmQuit(show: Boolean) {
        _state.update { it.copy(showConfirmQuit = show) }
    }

    fun setShowConfirmNewGame(show: Boolean) {
        _state.update { it.copy(showConfirmNewGame = show) }
    }

    fun setShowHelp(show: Boolean) {
        _state.update { it.copy(showHelp = show) }
    }

    fun setCountdownActive(active: Boolean) {
        _state.update { it.copy(isCountdownActive = active) }
    }

    // ── Countdown timer hold ─────────────────────────────────────────────────

    fun pauseTimer() { stopTimer() }

    fun resumeTimer() {
        if (_state.value.screen == Screen.GAME && !_state.value.won) startTimer()
    }

    // ── Pause / Resume ───────────────────────────────────────────────────────

    fun pause() {
        if (Log.isLoggable("FutoshikiDebug", Log.DEBUG)) Log.d("FutoshikiDebug", "pause() called, current screen=${_state.value.screen}")
        if (_state.value.screen != Screen.GAME) return
        SoundManager.play(Sound.BUTTON)
        val isEndGame = _state.value.won || _state.value.isSolved || _state.value.defeated
        stopTimer()
        pauseStartTime = System.currentTimeMillis()
        _state.update { it.copy(
            previousScreen = it.screen,
            screen = Screen.PAUSE,
            timerRunning = false,
            pauseCount = if (!isEndGame) it.pauseCount + 1 else it.pauseCount
        ) }
        if (Log.isLoggable("FutoshikiDebug", Log.DEBUG)) Log.d("FutoshikiDebug", "pause() done, new screen=${_state.value.screen}")
    }

    fun resume() {
        Log.d("FutoshikiDebug", "resume() called, current screen=${_state.value.screen}")
        SoundManager.play(Sound.BUTTON)
        val isWon = _state.value.won
        val isEndGame = isWon || _state.value.isSolved || _state.value.defeated
        val elapsed = pauseStartTime?.let { System.currentTimeMillis() - it } ?: 0L
        pauseStartTime = null
        _state.update { it.copy(
            previousScreen = it.screen,
            screen = Screen.GAME,
            timerRunning = !isWon,
            pauseTimeMs = if (!isEndGame) it.pauseTimeMs + elapsed else it.pauseTimeMs
        ) }
        if (!isWon) {
            startTimer()
        }
        if (Log.isLoggable("FutoshikiDebug", Log.DEBUG)) Log.d("FutoshikiDebug", "resume() done, new screen=${_state.value.screen}")
    }

    fun goToMainMenu() {
        stopTimer()
        korgeManager.cancelCurrentScene()
        korgeManager.introFinished = false
        korgeManager.playMenuIdle()
        _state.update { it.copy(previousScreen = it.screen, screen = Screen.LANDING, forceQuitInPause = false) }
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
        SoundManager.play(Sound.LOSS)
        korgeManager.gameWorld?.runSolveSequence()
        stopTimer()
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

    fun saveSizePreference(newSize: Int) {
        prefs.edit().putInt("game_size", newSize).apply()
        _state.update { it.copy(size = newSize) }
    }

    fun saveDifficultyPreference(difficulty: Difficulty) {
        prefs.edit().putString("difficulty", difficulty.name).apply()
        _state.update { it.copy(difficulty = difficulty) }
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

    private fun calculateNinjaScreenX(grid: List<List<Int>>, size: Int, solution: List<List<Int>>, mistakeCount: Int = 0): Float {
        var correct = 0
        for (r in 0 until size) {
            val row = grid[r]
            if (row.all { it != 0 } && row == solution[r]) correct++
        }
        for (c in 0 until size) {
            val col = (0 until size).map { grid[it][c] }
            val solCol = (0 until size).map { solution[it][c] }
            if (col.all { it != 0 } && col == solCol) correct++
        }
        val base = 500f
        val forwardStep = 70f
        val mistakeBackwardStep = 50f
        val offset = (correct * forwardStep) - (mistakeCount * mistakeBackwardStep)
        return (base + offset).coerceIn(100f, 900f)
    }
}
