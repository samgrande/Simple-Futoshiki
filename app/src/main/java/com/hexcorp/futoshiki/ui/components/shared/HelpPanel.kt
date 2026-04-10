package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.ReemKufi

@Composable
fun HelpContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "How to Play Futoshiki",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            color = FutoshikiColors.onSurface(),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Futoshiki (Japanese for \"inequality\") is a logic puzzle played on a square grid—usually 5×5. The goal is to fill the board so that every row and column contains a unique set of digits (1–5).",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = ReemKufi,
            color = if (LocalIsDark.current) Color(0xFFBBBBBB) else Color(0xFF444444),
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "The Core Rules",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            color = FutoshikiColors.onSurface(),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HelpListItem(
            number = "1.",
            boldPart = "Unique Rows/Columns:",
            body = " Just like Sudoku, a number cannot repeat in any row or column."
        )
        Spacer(Modifier.height(6.dp))
        HelpListItem(
            number = "2.",
            boldPart = "Inequalities:",
            body = " The symbols < (less than) and > (greater than) between cells must be honored. For example, if A < B, the number in cell A must be smaller than the number in cell B."
        )
        Spacer(Modifier.height(6.dp))
        HelpListItem(
            number = "3.",
            boldPart = "Pre-filled Numbers:",
            body = " Respect any digits already provided at the start."
        )
    }
}

@Composable
fun HelpPanel(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, FutoshikiColors.onSurface(), RoundedCornerShape(14.dp))
            .background(FutoshikiColors.surface())
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        val contentModifier = if (scrollable) {
            Modifier.weight(1f).verticalScroll(rememberScrollState())
        } else {
            Modifier
        }
        HelpContent(modifier = contentModifier)
    }
}

@Composable
private fun HelpListItem(number: String, boldPart: String, body: String) {
    val isDark = LocalIsDark.current
    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
        Text(
            text = number,
            fontSize = 13.sp,
            fontFamily = ReemKufi,
            color = if (isDark) Color(0xFFBBBBBB) else Color(0xFF444444),
            lineHeight = 21.sp,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = FutoshikiColors.onSurface())) {
                    append(boldPart)
                }
                withStyle(SpanStyle(color = if (isDark) Color(0xFFBBBBBB) else Color(0xFF444444))) {
                    append(body)
                }
            },
            fontSize = 13.sp,
            fontFamily = ReemKufi,
            lineHeight = 21.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
