package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
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

    val btnOffset by animateDpAsState(if (isPressed) 2.dp else 0.dp, tween(80), label = "coralOffset")

    Box(modifier = modifier.height(75.dp)) {
        if (!isPressed) {
            val shadowColor = if (isDark) {
                accent.copy(alpha = 0.25f)
            } else {
                Color(0xBF000000).copy(alpha = if (enabled) 0.75f else 0.3f)
            }
            Box(
                modifier = Modifier
                    .offset(x = 2.dp, y = 2.dp)
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shadowColor)
            )
        }

        val contentColor = if (isDark) Color.Black else Color.Black

        Box(
            modifier = Modifier
                .offset(x = if (enabled) btnOffset else 0.dp, y = if (enabled) btnOffset else 0.dp)
                .fillMaxWidth()
                .height(53.dp)
                .border(2.dp, Color.Black.copy(alpha = if (enabled) 1f else 0.3f), RoundedCornerShape(8.dp))
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
                },
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
