package com.hexcorp.futoshiki.ui.screens.game

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi

@Composable
fun NumberPad(
    size: Int,
    buttonSizeDp: Dp,
    spacingDp: Dp,
    onNumber: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacingDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (n in 1..size) {
            val num = n
            key(num) {
                NumberButton(
                    label   = num.toString(),
                    sizeDp  = buttonSizeDp,
                    onClick = remember(num, onNumber) { { onNumber(num) } }
                )
            }
        }
    }
}

@Composable
private fun NumberButton(label: String, sizeDp: Dp, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(80), label = "numBtnOffset"
    )

    Box(
        modifier = Modifier
            .size(sizeDp)
            .offset(x = offset, y = offset)
            .shadow(if (isPressed) 1.dp else 3.dp, CircleShape,
                ambientColor = Color(0x6B000000), spotColor = Color(0x6B000000))
            .clip(CircleShape)
            .background(FutoshikiColors.background())
            .border(1.5.dp, FutoshikiColors.onSurface(), CircleShape)
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
            text       = label,
            fontSize   = (sizeDp.value * 0.38f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            color      = FutoshikiColors.onSurface()
        )
    }
}
