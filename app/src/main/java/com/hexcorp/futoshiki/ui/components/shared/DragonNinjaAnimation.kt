package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.hexcorp.futoshiki.R
import kotlinx.coroutines.delay

@Composable
fun DragonNinjaAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dragon_movement")
    
    // Intro animation: starts big, then shrinks to normal
    val introProgress = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        delay(1000) // Stay big for a second
        introProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
        )
    }

    val dragonXOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_x"
    )

    val ninjaXOffset by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ninja_x"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, Color.Black, RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        // Background Lottie animation
        DotLottieAnimation(
            source = DotLottieSource.Res(R.raw.background),
            autoplay = true,
            loop = true,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = 4f, scaleY = 4f)
        )

        // Dark area below ground to represent soil/depth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .align(Alignment.BottomCenter)
                .background(Color(0xFF1A1A1A))
        )

        // The ground Lottie animation
        DotLottieAnimation(
            source = DotLottieSource.Res(R.raw.ground),
            autoplay = true,
            loop = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val scale = 1.5f + (introProgress.value * 1.0f)
            val tX = introProgress.value * -450f
            val tY = introProgress.value * 50f
            val yOffset = 32.dp + (9.dp * introProgress.value)

            // Ninja running on the right - moved down to sit on the line
            DotLottieAnimation(
                source = DotLottieSource.Res(R.raw.ninja),
                autoplay = true,
                loop = true,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(90.dp)
                    .padding(end = 40.dp)
                    .offset(y = 18.dp)
                    .offset(x = -10.dp + ninjaXOffset.dp)
            )

            // Dragon chasing from the left - significantly larger (fixed big size)
            DotLottieAnimation(
                source = DotLottieSource.Res(R.raw.dragon),
                autoplay = true,
                loop = true,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(600.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        transformOrigin = TransformOrigin(0f, 1f),
                        translationX = tX,
                        translationY = tY
                    )
                    .offset(x = (dragonXOffset - 65).dp, y = yOffset)
            )
        }
    }
}
