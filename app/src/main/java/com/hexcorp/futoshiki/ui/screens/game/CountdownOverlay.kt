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

private const val DIGIT_DURATION_MS = 1500L
private const val GO_HOLD_MS        = 300L

@Composable
fun CountdownOverlay(
    ninjaRunning: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent  = accentColor()
    val isDark  = LocalIsDark.current

    var label by remember { mutableStateOf("3") }
    var goneDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        label = "3"
        delay(DIGIT_DURATION_MS)
        if (!goneDone) label = "2"
        delay(DIGIT_DURATION_MS)
        if (!goneDone) label = "1"
    }

    LaunchedEffect(ninjaRunning) {
        if (ninjaRunning && !goneDone) {
            goneDone = true
            label    = "GO!"
            delay(GO_HOLD_MS)
            onDone()
        }
    }

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

    Box(
        modifier         = modifier.background(FutoshikiColors.background().copy(alpha = 0.70f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState  = label,
            transitionSpec = {
                scaleIn(tween(200)) togetherWith scaleOut(tween(200))
            },
            label = "countdownLabel"
        ) { lbl ->
            Text(
                text       = lbl,
                fontFamily = Midorima,
                fontSize   = if (lbl == "GO!") 84.sp else 110.sp,
                fontWeight = FontWeight.Normal,
                color      = textColor,
                modifier   = Modifier.scale(if (isGo) 1f else pulse)
            )
        }
    }
}