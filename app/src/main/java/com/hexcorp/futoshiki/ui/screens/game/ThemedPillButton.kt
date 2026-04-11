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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor

@Composable
fun ThemedPillButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val accent = accentColor()
    val isDark = LocalIsDark.current

    val btnOffset by animateDpAsState(if (isPressed) 2.dp else 0.dp, tween(80), label = "btnOffset")

    val contentColor = Color.Black

    val borderColor = if (isDark) {
        Color.Black.copy(alpha = 0.3f)
    } else {
        Color.Black.copy(alpha = if (enabled) 1f else 0.3f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(53.dp)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) accent else accent.copy(alpha = 0.45f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    ) {
        // Single path for internal shadow to avoid overlap and handle rounding correctly
        if (isPressed && enabled) {
            val shadowColor = Color.Black.copy(alpha = if (isDark) 0.18f else 0.15f)
            val shadowColorDeep = Color.Black.copy(alpha = if (isDark) 0.22f else 0.18f)
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = 6.dp.toPx()
                val sw = 3.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, r)
                    arcTo(
                        rect = Rect(0f, 0f, r * 2, r * 2),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(size.width, 0f)
                }
                
                // Outer subtle shadow part
                drawPath(
                    path = path,
                    color = shadowColor,
                    style = Stroke(width = sw * 2)
                )
                
                // Inner "crease" for more depth feel - made thinner and more subtle
                drawPath(
                    path = path,
                    color = shadowColorDeep,
                    style = Stroke(width = 0.5.dp.toPx() * 2)
                )
            }

            // Extra "pressed" overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.02f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = btnOffset, y = btnOffset),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = label,
                color      = contentColor,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}
