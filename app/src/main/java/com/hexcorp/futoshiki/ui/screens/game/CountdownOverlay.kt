package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.Midorima
import com.hexcorp.futoshiki.ui.theme.accentColor
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import kotlinx.coroutines.delay

// Ninja intro timing breakdown (in ms):
//   Stand still        : 500
//   Dragon flies over  : 1200
//   Ninja turns left   : 1300
//   Ninja reacts right : 300
//   TOTAL              : 3300  →  3 × 1100ms per digit

private const val DIGIT_DURATION_MS = 1100L  // each digit lasts this long
private const val GO_HOLD_MS        = 550L   // how long "GO!" stays visible before grid fades in

/**
 * Full-screen countdown that replaces the puzzle grid during the KorGE intro.
 *
 * - Counts 3 → 2 → 1 at [DIGIT_DURATION_MS] intervals.
 * - Holds on "1" if [ninjaRunning] hasn't fired yet (safety for slow devices).
 * - Shows "GO!" the **instant** [ninjaRunning] becomes true.
 * - Calls [onDone] after [GO_HOLD_MS] so the parent can fade the grid in.
 */
@Composable
fun CountdownOverlay(
    ninjaRunning: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent  = accentColor()
    val isDark  = LocalIsDark.current

    // ── State ────────────────────────────────────────────────────────────────
    var label by remember { mutableStateOf("3") }
    var goneDone by remember { mutableStateOf(false) }

    // ── Digit ticker: counts 3 → 2 → 1, then waits for ninjaRunning ─────────
    LaunchedEffect(Unit) {
        label = "3"
        delay(DIGIT_DURATION_MS)
        label = "2"
        delay(DIGIT_DURATION_MS)
        label = "1"
        // Finish 800ms earlier than original (550ms GO hold + 250ms of "1")
        delay(DIGIT_DURATION_MS - 250)
        onDone()
    }

    // As soon as ninjaRunning flips true, switch to GO! then call onDone
    LaunchedEffect(ninjaRunning) {
        if (ninjaRunning && !goneDone) {
            label    = "GO!"
            goneDone = true
            // Grid is already fading in thanks to the earlier onDone() call
        }
    }

    // ── Pulse on digits ──────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.07f,
        animationSpec = infiniteRepeatable(
            animation  = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val isGo  = label == "GO!"
    val textColor = when {
        isGo   -> accent
        isDark -> Color.White
        else   -> Color(0xFF1A1A1A)
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    Box(
        modifier         = modifier.background(FutoshikiColors.background().copy(alpha = 0.70f)),
        contentAlignment = androidx.compose.ui.BiasAlignment(0f, 0.2f)
    ) {
        AnimatedContent(
            targetState  = label,
            transitionSpec = {
                (scaleIn(tween(200)) + fadeIn(tween(160))) togetherWith
                (scaleOut(tween(160)) + fadeOut(tween(120)))
            },
            label = "countdownLabel"
        ) { lbl ->
            Text(
                text       = lbl,
                fontFamily = Midorima,
                fontSize   = if (lbl == "GO!") 84.sp else 110.sp,
                fontWeight = FontWeight.Normal,   // PixelF is single-weight
                color      = textColor,
                modifier   = Modifier.scale(if (isGo) 1f else pulse)
            )
        }
    }
}
