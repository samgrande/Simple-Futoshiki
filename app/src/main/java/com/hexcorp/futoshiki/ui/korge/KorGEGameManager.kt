package com.hexcorp.futoshiki.ui.korge

import com.hexcorp.futoshiki.game.entities.GameWorld
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
}
