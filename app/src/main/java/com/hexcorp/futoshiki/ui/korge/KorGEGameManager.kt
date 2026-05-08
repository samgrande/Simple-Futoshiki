package com.hexcorp.futoshiki.ui.korge

import com.hexcorp.futoshiki.game.entities.GameWorld
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class GameScene {
    MENU_IDLE,
    FULL_INTRO,
    SKIP_INTRO,
    WIN_SEQUENCE,
    SOLVE_SEQUENCE,
    RESTART
}

data class SceneParams(
    val winImmediate: Boolean = false,
    val skipIntro: Boolean = false
)

class KorGEGameManager {
    private val _aggression = MutableStateFlow(0f)
    val aggression = _aggression.asStateFlow()

    private val _speedMultiplier = MutableStateFlow(1.0f)
    val speedMultiplier = _speedMultiplier.asStateFlow()

    private val _distanceMultiplier = MutableStateFlow(1.0f)
    val distanceMultiplier = _distanceMultiplier.asStateFlow()

    private val _ninjaScreenX = MutableStateFlow(500f)
    val ninjaScreenX = _ninjaScreenX.asStateFlow()

    var introFinished = false
    var isPaused = false
    var gameWorld: GameWorld? = null

    private val _restartGame = MutableStateFlow(0)
    val restartGame = _restartGame.asStateFlow()

    private val _runningStarted = MutableStateFlow(false)
    val runningStarted = _runningStarted.asStateFlow()

    private val _showCountdownOnResume = MutableStateFlow(false)
    val showCountdownOnResume = _showCountdownOnResume.asStateFlow()

    private val _freshGameStart = MutableStateFlow(false)
    val freshGameStart = _freshGameStart.asStateFlow()

    private var currentSceneJob: Job? = null
    private val sceneCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun signalRunningStarted() {
        _runningStarted.value = true
    }

    fun resetRunningStarted() {
        _runningStarted.value = false
    }

    private val _sceneLoaded = MutableStateFlow(false)
    val sceneLoaded = _sceneLoaded.asStateFlow()

    fun signalSceneLoaded() {
        _sceneLoaded.value = true
    }

    fun resetSceneLoaded() {
        _sceneLoaded.value = false
    }

    fun updateAggression(value: Float) {
        _aggression.value = value
    }

    fun applyRowCompletionBoost() {
        _speedMultiplier.value = (_speedMultiplier.value * 1.3f).coerceAtMost(2.5f)
        _distanceMultiplier.value = (_distanceMultiplier.value * 1.3f).coerceAtMost(3.0f)
    }

    fun getSpeedMultiplier(): Float = _speedMultiplier.value

    fun getDistanceMultiplier(): Float = _distanceMultiplier.value

    fun resetBoost() {
        _speedMultiplier.value = 1.0f
        _distanceMultiplier.value = 1.0f
        _ninjaScreenX.value = 500f
    }

    fun updateNinjaScreenX(value: Float) {
        _ninjaScreenX.value = value
    }

    fun signalGameRestart() {
        _restartGame.value = _restartGame.value + 1
        _showCountdownOnResume.value = true
    }

    fun resetRestartGame() {
        _restartGame.value = 0
    }

    fun signalFreshGameStart() {
        _freshGameStart.value = true
    }

    fun clearFreshGameStart() {
        _freshGameStart.value = false
    }

    fun clearShowCountdown() {
        _showCountdownOnResume.value = false
    }

    fun resetNinjaPosition() {
        _ninjaScreenX.value = 500f
    }

    fun cancelCurrentScene() {
        currentSceneJob?.cancel()
        currentSceneJob = null
    }

    fun playScene(scene: GameScene, params: SceneParams = SceneParams()) {
        cancelCurrentScene()
        
        currentSceneJob = sceneCoroutineScope.launch {
            when (scene) {
                GameScene.MENU_IDLE -> gameWorld?.startMenuIdle()
                GameScene.FULL_INTRO -> gameWorld?.startGame(skipIntro = false)
                GameScene.SKIP_INTRO -> gameWorld?.startGame(skipIntro = true)
                GameScene.WIN_SEQUENCE -> gameWorld?.runWinSequence(immediate = params.winImmediate)
                GameScene.SOLVE_SEQUENCE -> gameWorld?.runSolveSequence()
                GameScene.RESTART -> gameWorld?.restartGame()
            }
        }
    }

    fun playMenuIdle() = playScene(GameScene.MENU_IDLE)
    fun playFullIntro() = playScene(GameScene.FULL_INTRO)
    fun playSkipIntro() = playScene(GameScene.SKIP_INTRO)
    fun playWin(immediate: Boolean = false) = playScene(GameScene.WIN_SEQUENCE, SceneParams(winImmediate = immediate))
    fun playSolve() = playScene(GameScene.SOLVE_SEQUENCE)
    fun playRestart() = playScene(GameScene.RESTART)
}
