package com.hexcorp.futoshiki.ui.screens.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.components.shared.formatTimer
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlinx.coroutines.delay

@Composable
fun CongratsView(
    timerSeconds: Int,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val vibrator = remember { context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val isDark = LocalIsDark.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isShaking by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isShaking = true
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200L)
                }
            } else {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // Reduced vertical padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.kanji_congrats),
            contentDescription = "Congratulations",
            modifier = Modifier
                .fillMaxWidth(0.42f) // Reduced size
                .aspectRatio(180f / 312f)
                .graphicsLayer {
                    translationX = if (isShaking) shakeAnim else 0f
                    rotationZ = if (isShaking) shakeAnim * 0.5f else 0f
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { /* Just for shaking effect */ }
        )

        Spacer(modifier = Modifier.height(20.dp)) // Reduced spacer

        Text(
            text          = "C O N G R A T U L A T I O N",
            color         = accentColor(),
            fontSize      = 11.sp, // Slightly smaller
            fontWeight    = FontWeight.SemiBold,
            fontFamily    = ReemKufi,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp)) // Reduced spacer

        Text(
            text          = "S O L V E D   I N",
            color         = (if (isDark) Color.White else Color.Black).copy(alpha = 0.65f),
            fontSize      = 8.5.sp, // Slightly smaller
            fontWeight    = FontWeight.Medium,
            fontFamily    = ReemKufi,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(4.dp)) // Reduced spacer

        val timeStr = formatTimer(timerSeconds)
        val mm = timeStr.substring(0, 2)
        val ss = timeStr.substring(3, 5)
        val displayTime = "${mm[0]} ${mm[1]} : ${ss[0]} ${ss[1]}"

        Text(
            text          = displayTime,
            color         = if (isDark) Color.White else Color.Black,
            fontSize      = 22.sp, // Slightly smaller
            fontWeight    = FontWeight.Bold,
            fontFamily    = ReemKufi,
            letterSpacing = 2.sp
        )

        // Small bottom spacer to give some breathing room above the footer
        Spacer(modifier = Modifier.height(8.dp))
    }
}
