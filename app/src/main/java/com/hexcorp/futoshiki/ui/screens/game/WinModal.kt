package com.hexcorp.futoshiki.ui.screens.game

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.components.shared.formatTimer
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.Midorima
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

private fun shareScreenshot(context: Context, view: android.view.View) {
    val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    view.draw(canvas)
    val file = File(context.cacheDir, "futoshiki_win.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share win"))
}

@Composable
fun CongratsView(
    timerSeconds: Int,
    onPlayAgain: () -> Unit,
    isExpanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val view = LocalView.current
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

    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 1f,
        animationSpec = tween(400),
        label = "congratsAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .graphicsLayer { this.alpha = alpha },
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
            fontFamily    = com.hexcorp.futoshiki.ui.theme.PixelF,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp)) // Reduced spacer

        Text(
            text          = "S O L V E D   I N",
            color         = (if (isDark) Color.White else Color.Black).copy(alpha = 0.65f),
            fontSize      = 8.5.sp, // Slightly smaller
            fontWeight    = FontWeight.Medium,
            fontFamily    = com.hexcorp.futoshiki.ui.theme.PixelF,
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
            fontFamily    = Midorima,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FutoshikiColors.timerBg())
                .clickable { shareScreenshot(context, view) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share",
                tint = FutoshikiColors.timerText(),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
