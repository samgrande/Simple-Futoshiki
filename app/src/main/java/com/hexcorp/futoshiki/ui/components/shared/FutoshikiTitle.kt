package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor

@Composable
fun FutoshikiTitle(
    size: Int? = null,
    showTabs: Boolean = false,
    onClick: (() -> Unit)? = null,
    fontSize: TextUnit = 36.sp
) {
    val accent = accentColor()
    Box(
        modifier = if (onClick != null) {
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
        } else Modifier
    ) {
        Column {
            Text(
                text = "Futoshiki",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi,
                color = FutoshikiColors.onSurface(),
                letterSpacing = (-0.5).sp,
                lineHeight = fontSize
            )
            val waveWidth = (fontSize.value * 4.2f).dp
            val waveHeight = (fontSize.value * 0.32f).dp
            WavyUnderline(
                width = waveWidth,
                height = waveHeight,
                modifier = Modifier.padding(top = (fontSize.value * 0.1f).dp)
            )
        }

        // Grid Size Pill (only shown if size is provided)
        if (size != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-4).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (showTabs) accent.copy(alpha = 0.2f) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$size x $size",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ReemKufi,
                    color = FutoshikiColors.onSurface()
                )
            }
        }
    }
}
