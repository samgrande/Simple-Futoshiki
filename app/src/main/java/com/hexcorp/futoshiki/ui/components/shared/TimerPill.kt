package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimerPill(
    seconds: Int,
    won: Boolean,
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    textColor: Color = Color.Black,
    showPill: Boolean = false
) {
    val bgColor = if (showPill) {
        val pillBg = if (com.hexcorp.futoshiki.ui.theme.LocalIsDark.current) Color.White else Color.Black
        pillBg
    } else Color.Unspecified

    val pillTextColor = if (showPill) {
        if (com.hexcorp.futoshiki.ui.theme.LocalIsDark.current) Color.Black else Color.White
    } else textColor

    Row(
        modifier = modifier
            .then(if (!enabled) Modifier.alpha(0.38f) else Modifier)
            .then(
                if (showPill) Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label ?: formatTimer(seconds),
            color = pillTextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
            letterSpacing = 1.sp
        )
    }
}
