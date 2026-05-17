package com.hexcorp.futoshiki.ui.screens.theming

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.accentColor

@Composable
fun MonoTintedPill(
    isTinted: Boolean,
    onToggle: (Boolean) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = accentColor()
    // Premium glassmorphism background: light/dark responsive
    val containerBg = if (isDark) {
        Color(0xFFFFFFFF).copy(alpha = 0.08f)
    } else {
        Color(0xFF000000).copy(alpha = 0.06f)
    }

    val containerWidth = 220.dp
    val containerHeight = 38.dp
    val padding = 4.dp
    val indicatorWidth = (containerWidth - (padding * 2)) / 2

    // Smooth spring/tween transition for the sliding indicator
    val targetOffset = if (isTinted) indicatorWidth else 0.dp
    val animatedOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 250),
        label = "pillOffset"
    )

    Box(
        modifier = modifier
            .width(containerWidth)
            .height(containerHeight)
            .clip(CircleShape)
            .background(containerBg)
            .padding(padding)
    ) {
        // Sliding indicator behind text
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(indicatorWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(accent)
        )

        // Options Row
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // MONO option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle(false) },
                contentAlignment = Alignment.Center
            ) {
                val monoTextColor = if (!isTinted) {
                    Color.White
                } else {
                    FutoshikiColors.onSurface().copy(alpha = 0.45f)
                }
                Text(
                    text = "M O N O",
                    fontFamily = PixelF,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = monoTextColor,
                    letterSpacing = 1.sp
                )
            }

            // TINTED option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle(true) },
                contentAlignment = Alignment.Center
            ) {
                val tintedTextColor = if (isTinted) {
                    Color.White
                } else {
                    FutoshikiColors.onSurface().copy(alpha = 0.45f)
                }
                Text(
                    text = "T I N T E D",
                    fontFamily = PixelF,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = tintedTextColor,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
