package com.hex.futoshiki.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Screen enum ───────────────────────────────────────────────────────────────

enum class Screen { LANDING, GAME, PAUSE }

// ── UI State ──────────────────────────────────────────────────────────────────

data class GameState(
    val screen: Screen = Screen.LANDING,
    val size: Int = 4,
    val puzzle: Puzzle? = null,
    val grid: List<List<Int>> = emptyList(),
    val selected: Pair<Int, Int>? = null,
    val errors: Set<String> = emptySet(),
    val won: Boolean = false,
    val showCongrats: Boolean = false,
    val timerSeconds: Int = 0,
    val timerRunning: Boolean = false,
    val gameKey: Int = 0          // incremented each new game → triggers cell pop-in animation
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class FutoshikiViewModel : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var timerJob: Job? = null

    // ── New game ─────────────────────────────────────────────────────────────

    fun newGame(size: Int) {
        val puzzle = generatePuzzle(size)
        val grid = puzzle.initial.map { it.toMutableList().toList() }
        stopTimer()
        _state.update { st ->
            st.copy(
                screen = Screen.GAME,
                size = size,
                puzzle = puzzle,
                grid = grid,
                selected = null,
                errors = emptySet(),
                won = false,
                showCongrats = false,
                timerSeconds = 0,
                timerRunning = true,
                gameKey = st.gameKey + 1
            )
        }
        startTimer()
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

        _state.update { it.copy(grid = newGrid, errors = errors, won = won) }

        if (won) {
            stopTimer()
            viewModelScope.launch {
                delay(300)
                _state.update { it.copy(showCongrats = true) }
            }
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
        _state.update { it.copy(grid = newGrid, errors = errors, won = false) }
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
        _state.update { it.copy(selected = r to c) }
    }

    fun moveSelection(dr: Int, dc: Int) {
        val st = _state.value
        val (r, c) = st.selected ?: return
        val nr = (r + dr).coerceIn(0, st.size - 1)
        val nc = (c + dc).coerceIn(0, st.size - 1)
        _state.update { it.copy(selected = nr to nc) }
    }

    // ── Pause / Resume ───────────────────────────────────────────────────────

    fun pause() {
        if (_state.value.screen != Screen.GAME || _state.value.won) return
        stopTimer()
        _state.update { it.copy(screen = Screen.PAUSE, timerRunning = false) }
    }

    fun resume() {
        _state.update { it.copy(screen = Screen.GAME, timerRunning = true) }
        startTimer()
    }

    // ── Solve (cheat) ────────────────────────────────────────────────────────

    fun solve() {
        val puzzle = _state.value.puzzle ?: return
        stopTimer()
        _state.update { st ->
            st.copy(
                grid = puzzle.solution.map { it.toList() },
                errors = emptySet(),
                won = true,
                timerRunning = false,
                screen = Screen.GAME
            )
        }
    }

    // ── Size change ──────────────────────────────────────────────────────────

    fun changeSize(newSize: Int) {
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
}
