package com.hexcorp.futoshiki.ui.korge

import com.hexcorp.futoshiki.game.entities.GameWorld
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class KorGEGameManager {
    private val _aggression = MutableStateFlow(0f)
    val aggression = _aggression.asStateFlow()

    var gameWorld: GameWorld? = null

    fun updateAggression(value: Float) {
        _aggression.value = value
    }
}
