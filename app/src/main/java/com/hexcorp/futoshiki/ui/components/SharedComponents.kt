package com.hexcorp.futoshiki.ui.components

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
import androidx.compose.ui.draw.clipToBounds
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
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import com.hexcorp.futoshiki.R
import androidx.compose.foundation.Image
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
        ArrowDirection.UP    -> 270f
        ArrowDirection.RIGHT -> 0f
        ArrowDirection.DOWN  -> 90f
        ArrowDirection.LEFT  -> 180f
    }

    val accent = accentColor()
    Canvas(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer { rotationZ = degrees }
    ) {
        // Original SVG: width="12" height="19"
        // Scale to fit sizeDp
        val scaleX = size.width / 12f
        val scaleY = size.height / 19f
        val scale = minOf(scaleX, scaleY)

        // Center the path
        val offsetX = (size.width - (12f * scale)) / 2f
        val offsetY = (size.height - (19f * scale)) / 2f

        val path = Path().apply {
            moveTo(10.4244f * scale + offsetX, 7.61734f * scale + offsetY)
            cubicTo(
                11.4005f * scale + offsetX, 8.59361f * scale + offsetY,
                11.4005f * scale + offsetX, 10.1762f * scale + offsetY,
                10.4244f * scale + offsetX, 11.1525f * scale + offsetY
            )
            lineTo(3.88824f * scale + offsetX, 17.6886f * scale + offsetY)
            cubicTo(
                3.11313f * scale + offsetX, 18.4635f * scale + offsetY,
                1.85671f * scale + offsetX, 18.4635f * scale + offsetY,
                1.0816f * scale + offsetX, 17.6886f * scale + offsetY
            )
            cubicTo(
                0.306442f * scale + offsetX, 16.9135f * scale + offsetY,
                0.306442f * scale + offsetX, 15.6562f * scale + offsetY,
                1.0816f * scale + offsetX, 14.881f * scale + offsetY
            )
            lineTo(5.51715f * scale + offsetX, 10.4455f * scale + offsetY)
            cubicTo(
                6.10287f * scale + offsetX, 9.85969f * scale + offsetY,
                6.10287f * scale + offsetX, 8.91015f * scale + offsetY,
                5.51715f * scale + offsetX, 8.32437f * scale + offsetY
            )
            lineTo(1.0816f * scale + offsetX, 3.88882f * scale + offsetY)
            cubicTo(
                0.306442f * scale + offsetX, 3.11366f * scale + offsetY,
                0.306443f * scale + offsetX, 1.85637f * scale + offsetY,
                1.0816f * scale + offsetX, 1.08121f * scale + offsetY
            )
            cubicTo(
                1.85672f * scale + offsetX, 0.306306f * scale + offsetY,
                3.11313f * scale + offsetX, 0.306306f * scale + offsetY,
                3.88824f * scale + offsetX, 1.08121f * scale + offsetY
            )
            lineTo(10.4244f * scale + offsetX, 7.61734f * scale + offsetY)
            close()
        }

        drawPath(path, color = accent)
        drawPath(path, color = Color.Black, style = Stroke(width = 1.5f * scale))
    }
}

// ── Wavy underline (SVG path approximation via Canvas) ────────────────────────

@Composable
fun WavyUnderline(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val accent = accentColor()
    Canvas(
        modifier = modifier
            .size(width, height)
            .clipToBounds()
    ) {
        val w = size.width
        val h = size.height
        val midY = h / 2f
        val segW = w / 7f
        val period = segW * 2f
        val offsetX = -phase * period

        val path = Path()
        path.moveTo(offsetX, midY)

        // Draw enough segments to cover width + period
        val segmentsNeeded = (w / segW).toInt() + 4
        for (i in 0 until segmentsNeeded) {
            val currentSegX = offsetX + i * segW
            val ctrlX = currentSegX + segW * 0.5f
            val ctrlY = if (i % 2 == 0) 0f else h
            val endX  = currentSegX + segW
            path.quadraticBezierTo(ctrlX, ctrlY, endX, midY)
        }

        drawPath(
            path = path,
            color = accent.copy(alpha = 0.5f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
    }
}

// ── Logo mark ─────────────────────────────────────────────────────────────────

@Composable
fun LogoMark(size: Dp = 96.dp) {
    val isDark = com.hexcorp.futoshiki.ui.theme.LocalIsDark.current
    Image(
        painter = painterResource(id = if (isDark) R.drawable.futo_logo_dark else R.drawable.futo_logo),
        contentDescription = "Futoshiki Logo",
        modifier = Modifier.size(size)
    )
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

// ── Timer pill ────────────────────────────────────────────────────────────────

@Composable
fun TimerPill(
    seconds: Int,
    won: Boolean,
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.hexcorp.futoshiki.ui.theme.LocalIsDark.current
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
                drawPath(path, color = if (isDark) Color(0xFF111111) else Color.Black.copy(alpha = 0.6f))
            }
        } else {
            // Pause icon (two rectangles)
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

// ── Big pill button (primary = themed accent, secondary = outlined) ──────────────────

@Composable
fun BigButton(
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val btnOffset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(80),
        label = "btnOffset"
    )

    val bgColor = FutoshikiColors.bigButtonBg(primary)
    val textColor = FutoshikiColors.bigButtonText(primary)
    val borderColor = FutoshikiColors.bigButtonBorder()

    // Outer box provides room for the 2dp hard shadow bleed
    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(54.dp)
            .offset(y = btnOffset)
    ) {
        // Hard offset shadow (hidden when pressed)
        if (!isPressed) {
            val shadowColor = if (isDark) Color(0xFF929292).copy(alpha = 0.60f) else Color(0xB23B3B3B)
            Box(
                modifier = Modifier
                    .offset(x = 2.dp, y = 2.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(shadowColor)
            )
        }
        // Button face
        Box(
            modifier = Modifier
                .offset(x = btnOffset, y = btnOffset)
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(bgColor)
                .border(
                    width = 1.5.dp,
                    color = if (primary && !isDark) Color.Transparent else borderColor,
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
                color = textColor,
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
            color = FutoshikiColors.onSurface(),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Futoshiki (Japanese for \"inequality\") is a logic puzzle played on a square grid—usually 5×5. The goal is to fill the board so that every row and column contains a unique set of digits (1–5).",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = ReemKufi,
            color = if (com.hexcorp.futoshiki.ui.theme.LocalIsDark.current) Color(0xFFBBBBBB) else Color(0xFF444444),
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
    val isDark = com.hexcorp.futoshiki.ui.theme.LocalIsDark.current
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
