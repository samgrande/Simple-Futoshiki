package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.hexcorp.futoshiki.ui.theme.Midorima
import com.hexcorp.futoshiki.ui.theme.accentColor

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FutoshikiTitle(
    size: Int = 0,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    fontSize: TextUnit = 36.sp,
    isSolved: Boolean = false,
    showUnderline: Boolean = true,
    isSmallScreen: Boolean = false
) {
    val accent = accentColor()
    Box(
        modifier = if (onClick != null || onLongClick != null) {
            Modifier.combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick ?: {},
                onLongClick = onLongClick
            )
        } else Modifier
    ) {
        Column {
            Text(
                 text = "Futoshiki",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = Midorima,
                color = FutoshikiColors.onSurface(),
                letterSpacing = 4.sp,
                lineHeight = fontSize
            )
            if (showUnderline) {
                val waveWidth = if (isSolved) (fontSize.value * 4.8f).dp else (fontSize.value * 5.0f).dp
                val waveHeight = (fontSize.value * 0.32f).dp
                WavyUnderline(
                    width = waveWidth,
                    height = waveHeight,
                    modifier = Modifier.padding(top = (fontSize.value * 0.1f).dp)
                )
            }
        }

        // Grid Size Pill (only shown if size is provided)
        if (size > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = if (isSmallScreen) 35.dp else 45.dp, y = (-4).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Transparent)
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$size x $size",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Midorima,
                    color = FutoshikiColors.onSurface()
                )
            }
        }
    }
}
