package com.hex.futoshiki.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hex.futoshiki.R
import com.hex.futoshiki.game.FutoshikiViewModel
import com.hex.futoshiki.game.Puzzle
import com.hex.futoshiki.game.Screen
import com.hex.futoshiki.ui.components.*
import com.hex.futoshiki.ui.theme.FutoshikiColors
import com.hex.futoshiki.ui.theme.ReemKufi
import kotlinx.coroutines.delay

// ── Helpers ───────────────────────────────────────────────────────────────────

fun formatTimer(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

// ── Puzzle Components ─────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PuzzleCell(
    value: Int,
    isGiven: Boolean,
    isSelected: Boolean,
    isRelated: Boolean,
    hasError: Boolean,
    sizeDp: Dp,
    animDelay: Int,
    gameKey: Int,
    r: Int,
    c: Int,
    onTap: (Int, Int) -> Unit,
    onClear: (Int, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Pop-in animation
    var triggered by remember(gameKey) { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0.55f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "cellPop"
    )
    LaunchedEffect(gameKey) {
        delay(animDelay.toLong())
        triggered = true
    }

    val bg = when {
        hasError   -> FutoshikiColors.ErrorBg
        isSelected -> FutoshikiColors.CellSelected
        isRelated  -> FutoshikiColors.CellRelated
        else       -> FutoshikiColors.CellDefault
    }
    val borderColor = if (hasError) FutoshikiColors.ErrorStroke else FutoshikiColors.OnSurface
    val borderWidth = if (isSelected) 2.5.dp else 1.5.dp
    val textColor   = if (hasError) FutoshikiColors.ErrorStroke else FutoshikiColors.OnSurface
    val cornerRadius = sizeDp * 0.27f

    val shadowColor = if (hasError) FutoshikiColors.ErrorStroke.copy(alpha = 0.22f)
                      else Color(0x42000000)

    Box(
        modifier = Modifier
            .size(sizeDp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Box(
            modifier = Modifier
                .offset(x = 2.dp, y = 2.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(shadowColor)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(bg)
                .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onTap(r, c) },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClear(r, c)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (value != 0) {
                Text(
                    text       = value.toString(),
                    color      = textColor,
                    fontSize   = (sizeDp.value * 0.38f).sp,
                    fontWeight = if (isGiven) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = ReemKufi
                )
            }
        }
    }
}

@Composable
fun PuzzleBoard(
    puzzle: Puzzle,
    grid: List<List<Int>>,
    size: Int,
    selected: Pair<Int, Int>?,
    errors: Set<String>,
    cellSizeDp: Dp,
    arrowSlotDp: Dp,
    gameKey: Int,
    onCellTap: (Int, Int) -> Unit,
    onCellClear: (Int, Int) -> Unit
) {
    val totalItems = (size * 2 - 1)
    
    val constraintsMap = remember(puzzle) {
        val map = mutableMapOf<String, com.hex.futoshiki.game.Constraint>()
        puzzle.constraints.forEach {
            map["${it.r1},${it.c1},${it.r2},${it.c2}"] = it
            map["${it.r2},${it.c2},${it.r1},${it.c1}"] = it
        }
        map
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (ri in 0 until totalItems) {
            key(ri) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (ci in 0 until totalItems) {
                        val isCell = ri % 2 == 0 && ci % 2 == 0
                        val isHCon = ri % 2 == 0 && ci % 2 == 1
                        val isVCon = ri % 2 == 1 && ci % 2 == 0

                        when {
                            isCell -> {
                                val r = ri / 2
                                val c = ci / 2
                                val val_ = grid[r][c]
                                val isGiven    = puzzle.initial[r][c] != 0
                                val isSelected = selected?.first == r && selected.second == c
                                val isRelated  = selected != null && !isSelected &&
                                                 (selected.first == r || selected.second == c)
                                val hasError   = errors.contains("$r,$c")
                                val cellIdx    = r * size + c
                                val delay      = cellIdx * 22

                                key(r, c) {
                                    PuzzleCell(
                                        value      = val_,
                                        isGiven    = isGiven,
                                        isSelected = isSelected,
                                        isRelated  = isRelated,
                                        hasError   = hasError,
                                        sizeDp     = cellSizeDp,
                                        animDelay  = delay,
                                        gameKey    = gameKey,
                                        r          = r,
                                        c          = c,
                                        onTap      = onCellTap,
                                        onClear    = onCellClear
                                    )
                                }
                            }

                            isHCon -> {
                                val r  = ri / 2
                                val c1 = (ci - 1) / 2
                                val c2 = (ci + 1) / 2
                                val cn = constraintsMap["$r,$c1,$r,$c2"]
                                Box(
                                    modifier = Modifier
                                        .width(arrowSlotDp)
                                        .height(cellSizeDp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cn != null) {
                                        val dir = if (cn.gt) ArrowDirection.RIGHT else ArrowDirection.LEFT
                                        key(cn) {
                                            ConstraintArrow(
                                                direction = dir,
                                                sizeDp    = arrowSlotDp * 0.9f
                                            )
                                        }
                                    }
                                }
                            }

                            isVCon -> {
                                val r1 = (ri - 1) / 2
                                val r2 = (ri + 1) / 2
                                val c  = ci / 2
                                val cn = constraintsMap["$r1,$c,$r2,$c"]
                                Box(
                                    modifier = Modifier
                                        .width(cellSizeDp)
                                        .height(arrowSlotDp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cn != null) {
                                        val dir = if (cn.gt) ArrowDirection.DOWN else ArrowDirection.UP
                                        key(cn) {
                                            ConstraintArrow(
                                                direction = dir,
                                                sizeDp    = arrowSlotDp * 0.9f
                                            )
                                        }
                                    }
                                }
                            }

                            else -> {
                                Spacer(Modifier.size(arrowSlotDp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Number pad ────────────────────────────────────────────────────────────────

@Composable
fun NumberPad(
    size: Int,
    buttonSizeDp: Dp,
    spacingDp: Dp,
    onNumber: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacingDp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (n in 1..size) {
            val num = n
            key(num) {
                NumberButton(
                    label   = num.toString(),
                    sizeDp  = buttonSizeDp,
                    onClick = remember(num, onNumber) { { onNumber(num) } }
                )
            }
        }
    }
}

@Composable
private fun NumberButton(label: String, sizeDp: Dp, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(80), label = "numBtnOffset"
    )

    Box(
        modifier = Modifier
            .size(sizeDp)
            .offset(x = offset, y = offset)
            .shadow(if (isPressed) 1.dp else 3.dp, CircleShape,
                ambientColor = Color(0x6B000000), spotColor = Color(0x6B000000))
            .clip(CircleShape)
            .background(FutoshikiColors.Background)
            .border(1.5.dp, FutoshikiColors.OnSurface, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = (sizeDp.value * 0.38f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            color      = FutoshikiColors.OnSurface
        )
    }
}


// ── Timer pill ────────────────────────────────────────────────────────────────

@Composable
fun TimerPill(
    seconds: Int,
    won: Boolean,
    isPaused: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isPaused) FutoshikiColors.Coral else FutoshikiColors.TimerBg,
        animationSpec = tween(300),
        label = "timerBg"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                enabled = !won,
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (!won) {
            // Pause icon (two rectangles)
            Box(Modifier.size(12.dp, 14.dp)) {
                Box(
                    Modifier
                        .width(3.5.dp).fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isPaused) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.55f))
                )
                Box(
                    Modifier
                        .width(3.5.dp).fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isPaused) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.55f))
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

// ── Bottom coral buttons ──────────────────────────────────────────────────────

@Composable
fun CoralPillButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Neo-brutalist: pressed shifts button onto shadow; released floats above it
    val btnOffset by animateDpAsState(if (isPressed) 4.dp else 0.dp, tween(80), label = "coralOffset")

    // Outer box provides space for the 4dp shadow bleed at bottom-right
    Box(modifier = modifier.height(48.dp)) {
        // Hard offset shadow layer
        if (!isPressed) {
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = 4.dp)
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xBF000000).copy(alpha = if (enabled) 0.75f else 0.3f))
            )
        }
        // Actual button face
        Box(
            modifier = Modifier
                .offset(x = if (enabled) btnOffset else 0.dp, y = if (enabled) btnOffset else 0.dp)
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(if (enabled) FutoshikiColors.ButtonPrimary else FutoshikiColors.ButtonPrimary.copy(alpha = 0.45f))
                .border(2.dp, FutoshikiColors.OnSurface.copy(alpha = if (enabled) 1f else 0.3f), RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = label,
                color      = FutoshikiColors.OnSurface,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi,
                modifier   = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

// ── Rain ──────────────────────────────────────────────────────────────────────

private data class RainDrop(
    val x: Float,
    val yOffset: Float,
    val speed: Float,
    val length: Float,
    val alpha: Float
)

@Composable
fun RainBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val drops = remember {
        List(80) {
            RainDrop(
                x = Random.nextFloat(),
                yOffset = Random.nextFloat(),
                speed = Random.nextFloat() * 1.2f + 1.8f,
                length = Random.nextFloat() * 40f + 40f,
                alpha = Random.nextFloat() * 0.35f + 0.1f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val slantX = -20f // Slant amount

        drops.forEach { drop ->
            val yPos = ((drop.yOffset + progress * drop.speed) % 1f) * (h + drop.length) - drop.length
            // Calculate x based on y to make it fall diagonally
            val xPos = (drop.x * (w + 40f)) - 20f + (yPos / h) * slantX
            
            if (yPos < h) {
                drawLine(
                    color = Color.White.copy(alpha = drop.alpha),
                    start = androidx.compose.ui.geometry.Offset(xPos, yPos),
                    // The line itself is also slanted
                    end = androidx.compose.ui.geometry.Offset(xPos + (drop.length / 150f) * slantX, (yPos + drop.length).coerceAtMost(h)),
                    strokeWidth = 2f
                )
            }

            // Collision splash at bottom margin
            if (yPos + drop.length >= h && yPos < h) {
                val splashW = 12f
                drawLine(
                    color = Color.White.copy(alpha = drop.alpha * 1.5f),
                    start = androidx.compose.ui.geometry.Offset(xPos - splashW, h - 1f),
                    end = androidx.compose.ui.geometry.Offset(xPos + splashW, h - 1f),
                    strokeWidth = 2f
                )
            }
        }
    }
}

// ── Win modal ─────────────────────────────────────────────────────────────────

@Composable
fun WinModal(
    timerSeconds: Int,
    onPlayAgain: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label = "winAlpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 300f),
        label = "winScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FutoshikiColors.Overlay.copy(alpha = FutoshikiColors.Overlay.alpha * alpha)),
        contentAlignment = Alignment.Center
    ) {
        if (visible) {
            RainBackground()
        }

        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .padding(horizontal = 24.dp)
                .graphicsLayer { 
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, FutoshikiColors.OnSurface, RoundedCornerShape(24.dp))
                .background(FutoshikiColors.Background)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Kanji SVG illustration
            Box(
                modifier = Modifier
                    .padding(bottom = 14.dp)
                    .width(120.dp)
                    .aspectRatio(755f / 1335f),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.kanji_congrats),
                    contentDescription = "Congratulations kanji",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text       = "Congratulations!",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi,
                color      = FutoshikiColors.OnSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = "Solved in ${formatTimer(timerSeconds)}",
                fontSize   = 14.sp,
                color      = Color(0xFF777777),
                fontFamily = ReemKufi
            )
            Spacer(Modifier.height(24.dp))
            BigButton(label = "Play Again", onClick = onPlayAgain, primary = true)
        }
    }
}

// ── Full game screen ──────────────────────────────────────────────────────────

@Composable
fun GameScreen(
    viewModel: FutoshikiViewModel,
    state: com.hex.futoshiki.game.GameState
) {
    val puzzle    = state.puzzle ?: return
    val size      = state.size
    val grid      = state.grid
    val selected  = state.selected
    val errors    = state.errors
    val won       = state.won
    val gameKey   = state.gameKey

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FutoshikiColors.Background)
    ) {
        val vw = maxWidth
        val vh = maxHeight

        // Responsive sizing
        val hPad       = 20.dp
        val usableW    = (vw - hPad * 2).coerceAtMost(380.dp)

        val headerH    = vh * 0.11f
        val tabH       = vh * 0.065f
        val numpadH    = vh * 0.095f
        val refreshH   = vh * 0.075f
        val gapTotal   = vh * 0.155f
        val boardBudgetH = vh - headerH - tabH - numpadH - refreshH - gapTotal

        val arrowRatio = 0.32f
        val boardUnits = size + arrowRatio * (size - 1)
        val cellSizeDp = minOf(boardBudgetH / boardUnits, usableW / boardUnits)
        val arrowSlotDp = cellSizeDp * arrowRatio

        val numpadBtnDp = minOf(usableW / (size + 0.6f), vh * 0.08f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 420.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = hPad, vertical = (vh * 0.018f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerH),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FutoshikiTitle(fontSize = 28.sp)
                TimerPill(
                    seconds = state.timerSeconds,
                    won     = won,
                    isPaused = state.screen == Screen.PAUSE,
                    onClick = { viewModel.pause() }
                )
            }

            Spacer(Modifier.height(vh * 0.02f))

            // ── Size tabs ─────────────────────────────────────────────────
            DraggableSizeTabs(
                currentSize    = size,
                onSizeChange   = { viewModel.changeSize(it) },
                height         = tabH,
                modifier       = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(vh * 0.03f))

            // ── Board ─────────────────────────────────────────────────────
            PuzzleBoard(
                puzzle      = puzzle,
                grid        = grid,
                size        = size,
                selected    = selected,
                errors      = errors,
                cellSizeDp  = cellSizeDp,
                arrowSlotDp = arrowSlotDp,
                gameKey     = gameKey,
                onCellTap   = { r, c -> viewModel.selectCell(r, c) },
                onCellClear = { r, c -> viewModel.clearCell(r, c) }
            )

            Spacer(Modifier.height(vh * 0.03f))

            // ── Number pad ────────────────────────────────────────────────
            NumberPad(
                size         = size,
                buttonSizeDp = numpadBtnDp,
                spacingDp    = usableW * 0.025f,
                onNumber     = { viewModel.inputNumber(it) }
            )

            // Push bottom buttons to the very bottom
            Spacer(Modifier.weight(1f))

            // ── Bottom buttons ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (vh * 0.01f)),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CoralPillButton(
                    label    = "NEW GAME",
                    onClick  = { viewModel.newGame(size) },
                    modifier = Modifier.weight(1f)
                )
                CoralPillButton(
                    label    = "CLEAR ALL",
                    onClick  = { viewModel.clearAll() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Pause overlay ─────────────────────────────────────────────────
        if (state.screen == Screen.PAUSE) {
            PauseOverlay(
                timerFormatted = formatTimer(state.timerSeconds),
                onResume   = { viewModel.resume() },
                onSolve    = { viewModel.solve() },
                onNewGame  = { viewModel.newGame(size) }
            )
        }

        // ── Win modal ─────────────────────────────────────────────────────
        if (state.showCongrats) {
            WinModal(
                timerSeconds = state.timerSeconds,
                onPlayAgain  = { viewModel.newGame(size) }
            )
        }
    }
}
