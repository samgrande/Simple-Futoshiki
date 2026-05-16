package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.Midorima
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun NumberPad(
    size: Int,
    buttonSizeDp: Dp,
    spacingDp: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onNumber: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacingDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (n in 1..size) {
            val num = n
            key(num) {
                NumberButton(
                    label   = num.toString(),
                    sizeDp  = buttonSizeDp,
                    enabled = enabled,
                    onClick = remember(num, onNumber) { { onNumber(num) } }
                )
            }
        }
    }
}

@Composable
private fun NumberButton(label: String, sizeDp: Dp, enabled: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val accent = com.hexcorp.futoshiki.ui.theme.accentColor()
    val baseBg = FutoshikiColors.numberPadBg()
    val baseText = FutoshikiColors.onSurface()

    val bgColor by animateColorAsState(
        targetValue = if (isPressed && enabled) accent else baseBg,
        animationSpec = tween(150),
        label = "numBtnBg"
    )

    val textColor by animateColorAsState(
        targetValue = baseText,
        animationSpec = tween(150),
        label = "numBtnText"
    )
    
    Box(
        modifier = Modifier
            .size(sizeDp)
            .background(bgColor, CircleShape)
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
            fontSize   = (sizeDp.value * 0.32f).sp,
            fontWeight = FontWeight.Normal,
            fontFamily = Midorima,
            color      = if (enabled) textColor else textColor.copy(alpha = 0.3f),
            textAlign  = androidx.compose.ui.text.style.TextAlign.Center,
            style      = androidx.compose.ui.text.TextStyle(
                platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}
