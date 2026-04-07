package com.hex.futoshiki.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hex.futoshiki.ui.theme.FutoshikiColors
import com.hex.futoshiki.ui.theme.ReemKufi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.material3.ripple

// ── Helpers ───────────────────────────────────────────────────────────────────

fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

// ── Direction arrow indicator ─────────────────────────────────────────────────

enum class ArrowDirection { UP, RIGHT, DOWN, LEFT }

@Composable
fun ConstraintArrow(
    direction: ArrowDirection,
    sizeDp: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    val degrees = when (direction) {
        ArrowDirection.UP    -> 0f
        ArrowDirection.RIGHT -> 90f
        ArrowDirection.DOWN  -> 180f
        ArrowDirection.LEFT  -> 270f
    }

    Canvas(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer { rotationZ = degrees }
    ) {
        val r = size.minDimension / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f

        // coral circle
        drawCircle(color = FutoshikiColors.Coral, radius = r)
        // black stroke ring
        drawCircle(
            color = Color.Black,
            radius = r,
            style = Stroke(width = 1.5f)
        )
        // white triangle pointing UP
        val triTop    = cy - r * 0.42f
        val triBottom = cy + r * 0.38f
        val triHalf   = r * 0.42f
        val triPath = Path().apply {
            moveTo(cx,            triTop)
            lineTo(cx + triHalf,  triBottom)
            lineTo(cx - triHalf,  triBottom)
            close()
        }
        drawPath(triPath, color = Color.White)
    }
}

// ── Wavy underline (SVG path approximation via Canvas) ────────────────────────

@Composable
fun WavyUnderline(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width, height)) {
        val w = size.width
        val h = size.height
        val midY = h / 2f
        val segW = w / 7f

        val path = Path()
        path.moveTo(0f, midY)
        for (i in 0 until 7) {
            val ctrlX = segW * (i + 0.5f)
            val ctrlY = if (i % 2 == 0) 0f else h
            val endX  = segW * (i + 1)
            path.quadraticBezierTo(ctrlX, ctrlY, endX, midY)
        }
        drawPath(
            path = path,
            color = FutoshikiColors.CoralLight,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )
    }
}

// ── Logo mark ─────────────────────────────────────────────────────────────────

@Composable
fun LogoMark(size: Dp = 96.dp) {
    val cellDp  = size * 0.38f
    val gapDp   = size * 0.06f

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Top-left cell
        Box(
            Modifier
                .align(Alignment.TopStart)
                .size(cellDp)
                .border(2.dp, FutoshikiColors.OnSurface, RoundedCornerShape(6.dp))
                .background(FutoshikiColors.LogoCellBg, RoundedCornerShape(6.dp))
        )
        // Top-right cell
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(cellDp)
                .border(2.dp, FutoshikiColors.OnSurface, RoundedCornerShape(6.dp))
                .background(FutoshikiColors.LogoCellBg, RoundedCornerShape(6.dp))
        )
        // Bottom-left cell
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .size(cellDp)
                .border(2.dp, FutoshikiColors.OnSurface, RoundedCornerShape(6.dp))
                .background(FutoshikiColors.LogoCellBg, RoundedCornerShape(6.dp))
        )
        // Bottom-right cell — dark with kanji
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(cellDp)
                .border(2.dp, FutoshikiColors.OnSurface, RoundedCornerShape(6.dp))
                .background(FutoshikiColors.LogoKanjiTile, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(cellDp * 0.72f)
                    .background(FutoshikiColors.LogoCellBg, RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "数",
                    color = Color(0xFFD4282C),
                    fontSize = (cellDp.value * 0.44f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ReemKufi,
                    lineHeight = (cellDp.value * 0.44f).sp
                )
            }
        }
        // < symbol (top centre)
        Text(
            text = "<",
            color = FutoshikiColors.OnSurface,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.13f * 1.3f).sp,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        // > symbol (bottom centre)
        Text(
            text = ">",
            color = FutoshikiColors.OnSurface,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.13f * 1.3f).sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Futoshiki title + wavy underline ─────────────────────────────────────────

@Composable
fun FutoshikiTitle(fontSize: TextUnit = 36.sp) {
    Column {
        Text(
            text = "Futoshiki",
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            color = FutoshikiColors.OnSurface,
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

// ── Timer pill ────────────────────────────────────────────────────────────────

@Composable
fun TimerPill(
    seconds: Int,
    won: Boolean,
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isPaused) FutoshikiColors.Coral else FutoshikiColors.TimerBg,
        animationSpec = tween(300),
        label = "timerBg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                enabled = true,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (isPaused) {
            // Play icon (Triangle)
            Canvas(Modifier.size(12.dp, 14.dp)) {
                val path = Path().apply {
                    moveTo(1.5f.dp.toPx(), 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(1.5f.dp.toPx(), size.height)
                    close()
                }
                drawPath(path, color = Color.Black.copy(alpha = 0.6f))
            }
        } else {
            // Pause icon (two rectangles)
            Box(Modifier.size(12.dp, 14.dp)) {
                Box(
                    Modifier
                        .width(3.5.dp).fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                )
                Box(
                    Modifier
                        .width(3.5.dp).fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                )
            }
        }
        Text(
            text       = formatTimer(seconds),
            color      = if (isPaused) FutoshikiColors.OnSurface else FutoshikiColors.TimerText,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            letterSpacing = 1.5.sp
        )
    }
}

// ── Big pill button (primary = coral, secondary = outlined) ──────────────────

@Composable
fun BigButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val btnOffset by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = tween(80),
        label = "btnOffset"
    )

    // Outer box provides room for the 4dp hard shadow bleed
    Box(modifier = modifier.height(56.dp)) {
        // Hard offset shadow (hidden when pressed)
        if (!isPressed) {
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 4.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0x85000000))
            )
        }
        // Button face
        Box(
            modifier = Modifier
                .offset(x = btnOffset, y = btnOffset)
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    if (primary) FutoshikiColors.OnSurface   // #111 black
                    else Color(0xFFF4F4F4)
                )
                .border(
                    width = 1.5.dp,
                    color = FutoshikiColors.OnSurface,
                    shape = RoundedCornerShape(26.dp)
                )
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
                text = label,
                color = if (primary) Color.White else FutoshikiColors.OnSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ── Help panel ────────────────────────────────────────────────────────────────

@Composable
fun HelpContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "How to Play Futoshiki",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            color = FutoshikiColors.OnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Futoshiki (Japanese for \"inequality\") is a logic puzzle played on a square grid—usually 5×5. The goal is to fill the board so that every row and column contains a unique set of digits (1–5).",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = ReemKufi,
            color = Color(0xFF444444),
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "The Core Rules",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            color = FutoshikiColors.OnSurface,
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
            .border(1.5.dp, FutoshikiColors.OnSurface, RoundedCornerShape(14.dp))
            .background(Color.White)
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
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = number,
            fontSize = 13.sp,
            fontFamily = ReemKufi,
            color = Color(0xFF444444),
            lineHeight = 21.sp,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = FutoshikiColors.OnSurface)) {
                    append(boldPart)
                }
                withStyle(SpanStyle(color = Color(0xFF444444))) {
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
