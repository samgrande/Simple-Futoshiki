package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor

@Composable
fun TimerPill(
    seconds: Int,
    won: Boolean,
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val bgColor by animateColorAsState(
        targetValue = if (isPaused) accentColor() else FutoshikiColors.timerBg(),
        animationSpec = tween(300),
        label = "timerBg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                enabled = true,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (isPaused) {
            Canvas(Modifier.size(12.dp, 14.dp)) {
                val path = Path().apply {
                    moveTo(1.5f.dp.toPx(), 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(1.5f.dp.toPx(), size.height)
                    close()
                }
                drawPath(path, color = if (isDark) Color(0xFF111111) else Color.Black.copy(alpha = 0.6f))
            }
        } else {
            val iconColor = if (isDark) Color(0xFF111111) else Color.White.copy(alpha = 0.55f)
            Box(Modifier.size(12.dp, 14.dp)) {
                Box(
                    Modifier
                        .width(3.5.dp).fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(1.dp))
                        .background(iconColor)
                )
                Box(
                    Modifier
                        .width(3.5.dp).fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(1.dp))
                        .background(iconColor)
                )
            }
        }
        Text(
            text       = formatTimer(seconds),
            color      = if (isPaused) FutoshikiColors.onSurface() else FutoshikiColors.timerText(),
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            letterSpacing = 1.5.sp
        )
    }
}
