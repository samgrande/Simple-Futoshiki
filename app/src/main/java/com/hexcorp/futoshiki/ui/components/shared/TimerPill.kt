package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun TimerPill(
    seconds: Int,
    won: Boolean,
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    icon: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIsDark.current
    val accent = accentColor()

    // Pill background: white in dark mode, black in light mode; accent when paused
    val pillBg = if (isDark) Color.White else Color.Black
    val bgColor by animateColorAsState(
        targetValue = if (isPaused) accent else pillBg,
        animationSpec = tween(300),
        label = "timerBg"
    )

    // Determine contrasting text/icon color for the current background
    val accentNeedsLightText = accent == FutoshikiColors.FireAccent ||
            accent == FutoshikiColors.WaterAccent
    val contentColor = if (isPaused) {
        if (accentNeedsLightText) Color.White else Color.Black
    } else {
        if (isDark) Color.Black else Color.White
    }

    val interactionSource = remember { MutableInteractionSource() }
    // Keep onClick reference fresh — pointerInput captures lambdas at launch time
    val currentOnClick by rememberUpdatedState(onClick)

    Row(
        modifier = modifier
            .then(if (!enabled) Modifier.alpha(0.38f) else Modifier)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                coroutineScope {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown()
                            val press = PressInteraction.Press(down.position)
                            launch { interactionSource.emit(press) }

                            val up = waitForUpOrCancellation()
                            if (up != null) {
                                launch { interactionSource.emit(PressInteraction.Release(press)) }
                                currentOnClick()
                            } else {
                                launch { interactionSource.emit(PressInteraction.Cancel(press)) }
                            }
                        }
                    }
                }
            }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) {
            icon()
        } else if (label == null) {
            if (isPaused) {
                Canvas(Modifier.size(12.dp, 14.dp)) {
                    val path = Path().apply {
                        moveTo(1.5f.dp.toPx(), 0f)
                        lineTo(size.width, size.height / 2f)
                        lineTo(1.5f.dp.toPx(), size.height)
                        close()
                    }
                    drawPath(path, color = contentColor)
                }
            } else {
                Box(Modifier.size(12.dp, 14.dp)) {
                    Box(
                        Modifier
                            .width(3.5.dp).fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(1.dp))
                            .background(contentColor.copy(alpha = 0.7f))
                    )
                    Box(
                        Modifier
                            .width(3.5.dp).fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(1.dp))
                            .background(contentColor.copy(alpha = 0.7f))
                    )
                }
            }
        }
        Text(
            text       = label ?: formatTimer(seconds),
            color      = contentColor,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
            letterSpacing = 1.sp
        )
    }
}
