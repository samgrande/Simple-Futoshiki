package com.hexcorp.futoshiki.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing

object AnimationConstants {
    const val INSTANT_MS = 0
    const val INSTANT_SHORT_MS = 100
    const val SHORT_MS = 150
    const val MEDIUM_MS = 300
    const val LONG_MS = 600
    const val EXTRA_LONG_MS = 900

    val fastOutSlowInEasing: Easing = FastOutSlowInEasing
    val linearEasing: Easing = LinearEasing

    const val GRID_CELL_ENTER_DELAY_MS = 15
    const val GRID_CELL_ENTER_DURATION_MS = 300
    const val GRID_ALPHA_DURATION_MS = 400
    const val GRID_SCALE_DURATION_MS = 300
    const val COLOR_TRANSITION_MS = 150
    const val CIRCULAR_REVEAL_DURATION_MS = 400
    const val BLACK_REVEAL_DURATION_MS = 600
    const val SCREEN_FADE_SHORT_MS = 220
    const val SCREEN_FADE_MEDIUM_MS = 500
    const val COUNTDOWN_DIGIT_MS = 1500L
    const val COUNTDOWN_GO_MS = 300L
    const val COUNTDOWN_PULSE_MS = 380
}