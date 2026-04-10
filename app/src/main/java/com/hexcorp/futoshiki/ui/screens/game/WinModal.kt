package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.components.shared.formatTimer
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlinx.coroutines.delay

@Composable
fun WinModal(
    timerSeconds: Int,
    onPlayAgain: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        visible = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1000, easing = LinearOutSlowInEasing),
        label = "winAlpha"
    )

    var buttonBounds by remember { mutableStateOf<Rect?>(null) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isShaking by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isShaking = true
        } else {
            isShaking = false
        }
    }

    val shakeTransition = rememberInfiniteTransition(label = "kanjiShake")
    val shakeAnim by shakeTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
    ) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
            RainBackground(buttonBounds)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1.3f))

                Image(
                    painter = painterResource(id = R.drawable.kanji_congrats),
                    contentDescription = "Congratulations",
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .aspectRatio(180f / 312f)
                        .graphicsLayer {
                            translationX = if (isShaking) shakeAnim else 0f
                            rotationZ = if (isShaking) shakeAnim * 0.5f else 0f
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { }
                )

                Spacer(modifier = Modifier.height(72.dp))

                Text(
                    text          = "C O N G R A T U L A T I O N",
                    color         = accentColor(),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    fontFamily    = ReemKufi,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text          = "S O L V E D   I N",
                    color         = Color.White.copy(alpha = 0.65f),
                    fontSize      = 8.5.sp,
                    fontWeight    = FontWeight.Medium,
                    fontFamily    = ReemKufi,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val timeStr = formatTimer(timerSeconds)
                val mm = timeStr.substring(0, 2)
                val ss = timeStr.substring(3, 5)
                val displayTime = "${mm[0]} ${mm[1]} : ${ss[0]} ${ss[1]}"

                Text(
                    text          = displayTime,
                    color         = Color.White,
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.Bold,
                    fontFamily    = ReemKufi,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                val accent = accentColor()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp)
                        .onGloballyPositioned { buttonBounds = it.boundsInRoot() }
                        .border(2.dp, accent, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPlayAgain() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ReemKufi,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
