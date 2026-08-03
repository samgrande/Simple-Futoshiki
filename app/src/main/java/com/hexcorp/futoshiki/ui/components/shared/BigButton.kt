package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.audio.Sound
import com.hexcorp.futoshiki.audio.SoundManager
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.ripple
import androidx.compose.foundation.indication

private val ShadowDepth = 4.dp

// ── Big pill button (primary = themed accent, secondary = outlined) ──────────────────

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BigButton(
    label: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    primary: Boolean = false,
    inverted: Boolean = false,
    bordered: Boolean = true,
    isDark: Boolean = LocalIsDark.current,
    height: androidx.compose.ui.unit.Dp = 64.dp,
    monochrome: Boolean = false,
    rippleColor: Color? = null,
    modifier: Modifier = Modifier,
    sound: Sound = Sound.BUTTON
) {
    val haptic = LocalHapticFeedback.current

    // Determine colors based on design specifications
    val (faceColor, shadowColor, textColor, strokeColor) = if (primary) {
        if (monochrome) {
            val fc = if (isDark) Color.White else Color.Black
            val tc = if (isDark) Color.Black else Color.White
            val sc = if (isDark) Color(0xFFD1D1D1) else Color(0xFF333333)
            val stc = if (isDark) Color.White else Color.Black
            listOf(fc, sc, tc, stc)
        } else {
            val accent = com.hexcorp.futoshiki.ui.theme.accentColor()
            val shadowFactor = if (isDark) 0.85f else 0.7f
            val darkAccent = accent.copy(
                red = (accent.red * shadowFactor).coerceIn(0f, 1f),
                green = (accent.green * shadowFactor).coerceIn(0f, 1f),
                blue = (accent.blue * shadowFactor).coerceIn(0f, 1f)
            )
            val fc = accent
            val sc = darkAccent
            val tc = Color.White
            val borderAccent = accent.copy(
                red = (accent.red * 0.5f).coerceIn(0f, 1f),
                green = (accent.green * 0.5f).coerceIn(0f, 1f),
                blue = (accent.blue * 0.5f).coerceIn(0f, 1f)
            )
            val stc = if (isDark) borderAccent else Color.Black
            listOf(fc, sc, tc, stc)
        }
    } else {
        if (!isDark) {
            // Light Mode
            if (inverted) {
                // Secondary action (e.g. YES): Black fill, white text
                listOf(Color.Black, Color(0xFF333333), Color.White, Color.Black)
            } else {
                listOf(Color.White, Color(0xFFB6B6B6), Color.Black, Color.Black)
            }
        } else {
            // Dark Mode
            if (inverted) {
                // Secondary action (e.g. YES): White fill, black text
                listOf(Color.White, Color(0xFFD1D1D1), Color.Black, Color.White)
            } else {
                // Non-accented, totally dark inside
                listOf(Color.Black, Color.Black, Color.White, Color.White)
            }
        }
    }

    val fColor by androidx.compose.animation.animateColorAsState(faceColor as Color, tween(300), label = "fColor")
    val sColor by androidx.compose.animation.animateColorAsState(shadowColor as Color, tween(300), label = "sColor")
    val tColor by androidx.compose.animation.animateColorAsState(textColor as Color, tween(300), label = "tColor")
    val stColor by androidx.compose.animation.animateColorAsState(strokeColor as Color, tween(300), label = "stColor")

    // Use custom ripple color if provided, otherwise use default
    val indication = if (rippleColor != null) {
        ripple(bounded = true, color = rippleColor)
    } else {
        null
    }

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(fColor)
            .then(
                if (bordered) Modifier.border(2.dp, stColor, RoundedCornerShape(12.dp))
                else Modifier
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = indication,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    SoundManager.play(sound)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    SoundManager.play(Sound.BUTTON)
                    onLongClick?.invoke()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState = label,
            transitionSpec = {
                androidx.compose.animation.fadeIn(tween(300)) togetherWith androidx.compose.animation.fadeOut(tween(300))
            },
            label = "buttonLabel"
        ) { text ->
            Text(
                text = text,
                color = tColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = PixelF,
                letterSpacing = 1.sp
            )
        }
    }
}