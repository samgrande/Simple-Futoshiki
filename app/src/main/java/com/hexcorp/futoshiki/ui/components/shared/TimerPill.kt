package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.Midorima
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerPill(
    seconds: Int,
    won: Boolean,
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    icon: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val isDark = LocalIsDark.current
    val accent = accentColor()
    
    val bgColor by animateColorAsState(
        targetValue = if (isPaused) accent else FutoshikiColors.timerBg(),
        animationSpec = tween(300),
        label = "timerBg"
    )

    val interactionSource = remember { MutableInteractionSource() }

    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .then(if (!enabled) Modifier.alpha(0.38f) else Modifier)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(enabled, onLongClick) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        var triggered = false
                        
                        val press = PressInteraction.Press(down.position)
                        scope.launch { interactionSource.emit(press) }
                        
                        val longPressJob = scope.launch {
                            delay(800)
                            if (!triggered) {
                                triggered = true
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onLongClick?.invoke()
                            }
                        }

                        val up = waitForUpOrCancellation()
                        longPressJob.cancel()
                        
                        if (up != null) {
                            scope.launch { interactionSource.emit(PressInteraction.Release(press)) }
                            if (!triggered) {
                                onClick()
                            }
                        } else {
                            scope.launch { interactionSource.emit(PressInteraction.Cancel(press)) }
                        }
                    }
                }
            }
            .padding(horizontal = 16.dp, vertical = 7.dp),
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
                    drawPath(path, color = if (isDark) Color(0xFF111111) else Color.Black.copy(alpha = 0.6f))
                }
            } else {
                val iconColor = if (isDark) Color(0xFF111111) else Color.White.copy(alpha = 0.55f)
                Box(Modifier.size(12.dp, 14.dp)) {
                    Box(
                        Modifier
                            .width(3.5.dp).fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(1.dp))
                            .background(iconColor)
                    )
                    Box(
                        Modifier
                            .width(3.5.dp).fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(1.dp))
                            .background(iconColor)
                    )
                }
            }
        }
        Text(
            text       = label ?: formatTimer(seconds),
            color      = if (isPaused) FutoshikiColors.onSurface() else FutoshikiColors.timerText(),
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
            letterSpacing = 1.sp
        )
    }
}
