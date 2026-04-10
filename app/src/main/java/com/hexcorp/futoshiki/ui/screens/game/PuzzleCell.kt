package com.hexcorp.futoshiki.ui.screens.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PuzzleCell(
    value: Int,
    isGiven: Boolean,
    isSelected: Boolean,
    isRelated: Boolean,
    hasError: Boolean,
    sizeDp: Dp,
    animDelay: Int,
    gameKey: Int,
    givenCount: Int,
    r: Int,
    c: Int,
    onTap: (Int, Int) -> Unit,
    onClear: (Int, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val interactionSource = remember { MutableInteractionSource() }
    var isShaking by remember { mutableStateOf(false) }

    LaunchedEffect(isShaking) {
        if (isShaking) {
            delay(300)
            isShaking = false
        }
    }

    val shakeTransition = rememberInfiniteTransition(label = "shake")
    val shakeRotation by shakeTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeRot"
    )
    val shakeOffsetX by shakeTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeX"
    )
    val shakeOffsetY by shakeTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(60, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeY"
    )

    val currentRotation = if (isShaking) shakeRotation else 0f
    val currentOffsetX  = if (isShaking) shakeOffsetX  else 0f
    val currentOffsetY  = if (isShaking) shakeOffsetY  else 0f

    var triggered by remember(gameKey) { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0.55f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "cellPop"
    )
    LaunchedEffect(gameKey) {
        delay(animDelay.toLong())
        triggered = true
    }

    val bg = when {
        hasError   -> FutoshikiColors.errorBg()
        isSelected -> accentColor().copy(alpha = 0.12f)
        isRelated  -> FutoshikiColors.cellRelated()
        else       -> FutoshikiColors.cellDefault()
    }
    val borderColor = if (hasError) FutoshikiColors.ErrorStroke
                      else if (isSelected) accentColor()
                      else FutoshikiColors.onSurface()
    val borderWidth = if (isSelected) 2.5.dp else 1.5.dp
    val textColor   = if (hasError) FutoshikiColors.ErrorStroke else FutoshikiColors.onSurface()
    val cornerRadius = sizeDp * 0.27f

    val shadowColor = if (hasError) FutoshikiColors.ErrorStroke.copy(alpha = 0.22f)
                      else if (isSelected) accentColor().copy(alpha = 0.4f)
                      else Color(0x42000000)

    Box(
        modifier = Modifier
            .size(sizeDp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = currentRotation
                translationX = currentOffsetX
                translationY = currentOffsetY
            }
    ) {
        Box(
            modifier = Modifier
                .offset(x = 2.dp, y = 2.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(shadowColor)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(bg)
                .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (isGiven) {
                            val amplitude = (givenCount * 20).coerceIn(1, 255)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200L, amplitude))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(200L)
                            }
                            isShaking = true
                        }
                        onTap(r, c)
                    },
                    onLongClick = {
                        if (!isGiven) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClear(r, c)
                        } else {
                            val amplitude = (givenCount * 20).coerceIn(1, 255)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(200L, amplitude))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(200L)
                            }
                            isShaking = true
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (value != 0) {
                Text(
                    text       = value.toString(),
                    color      = textColor,
                    fontSize   = (sizeDp.value * 0.38f).sp,
                    fontWeight = if (isGiven) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = ReemKufi
                )
            }
        }
    }
}
