package com.hexcorp.futoshiki.ui.components.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.PixelF

@Composable
fun SolutionBanner(
    isDark: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDark) Color.White else Color.Black
    val textColor = if (isDark) Color.Black else Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(182.dp)
            .background(bgColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Large Kanji Background
        Text(
            text = "解",
            fontSize = 96.sp,
            color = accentColor,
            modifier = Modifier
                .alpha(0.73f)
                .align(Alignment.Center)
                .offset(y = (-10).dp),
            fontWeight = FontWeight.Normal
        )

        // SOLUTION Label
        Text(
            text = "SOLUTION",
            color = textColor,
            fontSize = 16.sp,
            letterSpacing = 1.sp,
            fontFamily = PixelF,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
