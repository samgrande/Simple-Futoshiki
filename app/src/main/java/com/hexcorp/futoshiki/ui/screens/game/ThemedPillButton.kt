package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.Midorima
import com.hexcorp.futoshiki.ui.theme.accentColor

@Composable
fun ThemedPillButton(
    label: String,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val accent = accentColor()
    val isDark = LocalIsDark.current

    val contentColor = if (isDark) Color.White else Color.Black

    val bgAlpha = if (enabled || onClick == null) 1f else 0.45f
    val isClickable = enabled && onClick != null

    val shadowFactor = if (isDark) 0.85f else 0.7f
    val darkAccent = accent.copy(
        red = (accent.red * shadowFactor).coerceIn(0f, 1f),
        green = (accent.green * shadowFactor).coerceIn(0f, 1f),
        blue = (accent.blue * shadowFactor).coerceIn(0f, 1f)
    )
    val depthShadowColor = if (isDark) Color.White.copy(alpha = 0.35f) else darkAccent

    val borderAccent = accent.copy(
        red = (accent.red * 0.5f).coerceIn(0f, 1f),
        green = (accent.green * 0.5f).coerceIn(0f, 1f),
        blue = (accent.blue * 0.5f).coerceIn(0f, 1f)
    )
    val stColor = borderAccent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(53.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(accent)
            .border(2.dp, stColor, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .then(
                if (isClickable) {
                    Modifier.clickable(
                        enabled = true
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick?.invoke()
                    }
                } else Modifier
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = label,
                color      = contentColor,
                fontSize   = 16.sp, // Slightly increased for pixel font
                fontWeight = FontWeight.Normal,
                fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}
