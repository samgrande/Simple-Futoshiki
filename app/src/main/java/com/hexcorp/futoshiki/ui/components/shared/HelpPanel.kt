package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.PixelF

@Composable
fun HelpPanel(
    modifier: Modifier = Modifier
) {
    var currentSection by remember { mutableIntStateOf(0) }
    var dragAccum by remember { mutableFloatStateOf(0f) }
    val isDark = LocalIsDark.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, FutoshikiColors.onSurface(), RoundedCornerShape(14.dp))
            .background(FutoshikiColors.surface())
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragAccum = 0f },
                    onDragEnd = {
                        val threshold = 60f
                        if (dragAccum < -threshold && currentSection < 1) {
                            currentSection++
                        } else if (dragAccum > threshold && currentSection > 0) {
                            currentSection--
                        }
                        dragAccum = 0f
                    },
                    onDragCancel = { dragAccum = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccum += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentSection,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInVertically(tween(300)) { it * direction } + fadeIn(tween(300)))
                    .togetherWith(
                        slideOutVertically(tween(300)) { -it * direction } + fadeOut(tween(200))
                    )
                    .using(SizeTransform(clip = false))
            },
            label = "helpSectionTransition"
        ) { section ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (section) {
                    0 -> HowToPlaySection()
                    1 -> CoreRulesSection()
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (section == 0) "↑ Swipe up for rules" else "↓ Swipe down for how to play",
                    fontSize = 10.sp,
                    fontFamily = PixelF,
                    color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun HowToPlaySection() {
    val isDark = LocalIsDark.current
    val bodyColor = if (isDark) Color(0xFFBBBBBB) else Color(0xFF444444)
    val accent = com.hexcorp.futoshiki.ui.theme.accentColor()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How to Play",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelF,
            color = FutoshikiColors.onSurface(),
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 16.dp)
                .width(32.dp)
                .height(2.dp)
                .background(FutoshikiColors.onSurface().copy(alpha = 0.3f), RoundedCornerShape(1.dp))
        )
        Text(
            text = "ふと指揮",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = com.hexcorp.futoshiki.ui.theme.Yuji,
            color = accent,
            letterSpacing = 2.sp
        )
        Text(
            text = "(Japanese for \"inequality\")",
            fontSize = 10.sp,
            fontFamily = PixelF,
            color = bodyColor.copy(alpha = 0.6f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(60.dp)
                        .background(accent.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Futoshiki is a logic puzzle on a square grid. Fill the board so every row and column contains each number exactly once.",
                    fontSize = 13.sp,
                    fontFamily = PixelF,
                    color = bodyColor,
                    lineHeight = 20.sp
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .background(accent.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Select a cell, choose a digit. Complete the puzzle when all cells are filled without breaking any rules.",
                    fontSize = 13.sp,
                    fontFamily = PixelF,
                    color = bodyColor,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun CoreRulesSection() {
    val isDark = LocalIsDark.current
    val cardBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF0F0F0)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Core Rules",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PixelF,
            color = FutoshikiColors.onSurface(),
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 12.dp)
                .width(32.dp)
                .height(2.dp)
                .background(FutoshikiColors.onSurface().copy(alpha = 0.3f), RoundedCornerShape(1.dp))
        )
        RuleCard(
            number = "01",
            title = "Unique Rows & Columns",
            body = "Every row and column contains each number exactly once, like Sudoku.",
            bgColor = cardBg
        )
        Spacer(Modifier.height(8.dp))
        RuleCard(
            number = "02",
            title = "Inequality Signs",
            body = "If A < B, then A must contain a smaller number than B.",
            bgColor = cardBg
        )
        Spacer(Modifier.height(8.dp))
        RuleCard(
            number = "03",
            title = "Pre-filled Cells",
            body = "Respect any digits already placed on the board at the start.",
            bgColor = cardBg
        )
    }
}

@Composable
private fun RuleCard(number: String, title: String, body: String, bgColor: Color) {
    val isDark = LocalIsDark.current
    val accent = com.hexcorp.futoshiki.ui.theme.accentColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelF,
                color = accent
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelF,
                color = FutoshikiColors.onSurface()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = body,
                fontSize = 12.sp,
                fontFamily = PixelF,
                color = if (isDark) Color(0xFFBBBBBB) else Color(0xFF555555),
                lineHeight = 17.sp
            )
        }
    }
}
