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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi

private val ShadowDepth = 4.dp

// ── Big pill button (primary = themed accent, secondary = outlined) ──────────────────

@Composable
fun BigButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    // On press the button face slides down into the shadow, consuming the depth
    val btnOffset by animateDpAsState(
        targetValue = if (isPressed) ShadowDepth else 0.dp,
        animationSpec = tween(80),
        label = "btnOffset"
    )

    val bgColor    = FutoshikiColors.bigButtonBg(primary)
    val textColor  = FutoshikiColors.bigButtonText(primary)
    val borderColor = FutoshikiColors.bigButtonBorder()

    // Outer box height = button face + shadow depth so shadow never clips
    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(52.dp + ShadowDepth)
    ) {
        // Hard shadow — same shape as button, pinned to bottom, never moves
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(52.dp)
                .background(FutoshikiColors.shadowColor(), RoundedCornerShape(26.dp))
        )

        // Button face — slides down on press to close the gap with the shadow
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(52.dp)
                .offset(y = btnOffset)
                .clip(RoundedCornerShape(26.dp))
                .background(bgColor)
                .border(
                    width = 1.5.dp,
                    color = if (primary && !isDark) Color.Transparent else borderColor,
                    shape = RoundedCornerShape(26.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi,
                letterSpacing = 0.5.sp
            )
        }
    }
}
