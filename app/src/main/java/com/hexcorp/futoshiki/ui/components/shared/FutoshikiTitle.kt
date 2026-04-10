package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi

@Composable
fun FutoshikiTitle(fontSize: TextUnit = 36.sp) {
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
        val waveWidth  = (fontSize.value * 4.2f).dp
        val waveHeight = (fontSize.value * 0.32f).dp
        WavyUnderline(
            width  = waveWidth,
            height = waveHeight,
            modifier = Modifier.padding(top = (fontSize.value * 0.1f).dp)
        )
    }
}
