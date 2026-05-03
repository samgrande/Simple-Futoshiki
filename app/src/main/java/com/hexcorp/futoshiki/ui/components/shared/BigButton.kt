package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.Midorima
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

private val ShadowDepth = 4.dp

// ── Big pill button (primary = themed accent, secondary = outlined) ──────────────────

@Composable
fun BigButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    inverted: Boolean = false,
    isDark: Boolean = LocalIsDark.current,
    modifier: Modifier = Modifier
) {

    val haptic = LocalHapticFeedback.current

    // Determine colors based on design specifications
    val (faceColor, shadowColor, textColor, strokeColor) = if (primary) {
        val accent = com.hexcorp.futoshiki.ui.theme.accentColor()
        val shadowFactor = if (isDark) 0.85f else 0.7f
        val darkAccent = accent.copy(
            red = (accent.red * shadowFactor).coerceIn(0f, 1f),
            green = (accent.green * shadowFactor).coerceIn(0f, 1f),
            blue = (accent.blue * shadowFactor).coerceIn(0f, 1f)
        )
        val sColor = if (isDark) Color.White else Color.Black
        androidx.compose.runtime.remember(accent, darkAccent, sColor) {
            android.util.Log.d("BigButton", "Primary colors updated")
            // Just return them as a tuple
            kotlin.math.log2(1f) // dummy
        }
        // Actually just calculate them directly to avoid Triple/Quad issues if not defined
        val fc = accent
        val sc = darkAccent
        val tc = if (isDark) Color.White else Color.White
        val borderAccent = accent.copy(
            red = (accent.red * 0.5f).coerceIn(0f, 1f),
            green = (accent.green * 0.5f).coerceIn(0f, 1f),
            blue = (accent.blue * 0.5f).coerceIn(0f, 1f)
        )
        val stc = if (isDark) borderAccent else Color.Black
        listOf(fc, sc, tc, stc)
    } else {
        if (!isDark) {
            // Light Mode
            if (inverted) {
                listOf(Color(0xFF1C1C1C), Color(0xFF000000), Color.White, Color.White)
            } else {
                listOf(Color.White, Color(0xFFB6B6B6), Color.Black, Color.Black)
            }
        } else {
            // Dark Mode
            if (inverted) {
                listOf(Color(0xFFEAEAEA), Color(0xFF999999), Color.Black, Color.Black)
            } else {
                // Non-accented, totally dark inside
                listOf(Color.Black, Color.Black, Color.White, Color.White)
            }
        }
    }

    val fColor = faceColor as Color
    val sColor = shadowColor as Color
    val tColor = textColor as Color
    val stColor = strokeColor as Color

    @Suppress("DEPRECATION")
    val customRipple = androidx.compose.material3.ripple(color = Color.White)
    val currentIndication = androidx.compose.foundation.LocalIndication.current
    val btnIndication = if ((isDark && !primary && !inverted) || (!isDark && inverted)) customRipple else currentIndication

    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(64.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(fColor)
            .border(2.dp, stColor, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = btnIndication
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = tColor,
            fontSize = 18.sp, // Slightly increased for pixel font clarity
            fontWeight = FontWeight.Normal,
            fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
            letterSpacing = 1.sp
        )
    }
}
