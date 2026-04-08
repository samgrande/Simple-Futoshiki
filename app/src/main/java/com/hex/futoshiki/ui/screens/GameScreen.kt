package com.hex.futoshiki.ui.screens

import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random
import androidx.compose.foundation.Image
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isShaking by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(800)
            isShaking = true
        } else {
            isShaking = false
        }
    }

    // Shake animation when held
    val shakeTransition = rememberInfiniteTransition(label = "shake")
    val shakeRotation by shakeTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeRot"
    )
    val shakeOffsetX by shakeTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeX"
    )
    val shakeOffsetY by shakeTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(60, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeY"
    )

    val currentRotation = if (isShaking) shakeRotation else 0f
    val currentOffsetX  = if (isShaking) shakeOffsetX  else 0f
    val currentOffsetY  = if (isShaking) shakeOffsetY  else 0f

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
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = currentRotation
                translationX = currentOffsetX
                translationY = currentOffsetY
            }
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
                    interactionSource = interactionSource,
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
            text       = label,
            fontSize   = (sizeDp.value * 0.38f).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            color      = FutoshikiColors.OnSurface
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
fun RainBackground(
    buttonBounds: Rect?
) {
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
        List(100) {
            RainDrop(
                x = Random.nextFloat(),
                yOffset = Random.nextFloat(),
                speed = Random.nextFloat() * 1.2f + 1.8f,
                length = Random.nextFloat() * 40f + 40f,
                alpha = Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val slantX = -20f

        drops.forEach { drop ->
            val totalTravel = h + drop.length
            val yPos = ((drop.yOffset + progress * drop.speed) % 1f) * totalTravel - drop.length
            val xPos = (drop.x * (w + 40f)) - 20f + (yPos / h) * slantX

            var collisionY: Float? = null
            var showSplash = false
            val checkX = xPos
            val checkYEnd = yPos + drop.length

            // 1. Check Button Occlusion/Collision
            if (buttonBounds != null && checkX >= buttonBounds.left && checkX <= buttonBounds.right) {
                // Any rain that reaches the button top is blocked
                if (checkYEnd >= buttonBounds.top) {
                    collisionY = buttonBounds.top
                    
                    // Only splash if in the middle and currently "hitting" the top edge
                    val edgeWidth = buttonBounds.width * 0.15f
                    if (checkX >= buttonBounds.left + edgeWidth && checkX <= buttonBounds.right - edgeWidth) {
                        if (yPos < buttonBounds.top) showSplash = true
                    }
                }
            }

            // 2. Check Floor Collision (if not already blocked by button)
            if (collisionY == null && checkYEnd >= h) {
                collisionY = h
                if (yPos < h) showSplash = true
            }

            // 3. Draw
            if (collisionY != null) {
                if (yPos < collisionY) {
                    val visibleLength = (collisionY - yPos).coerceAtMost(drop.length)
                    drawLine(
                        color = Color.White.copy(alpha = drop.alpha),
                        start = Offset(xPos, yPos),
                        end = Offset(xPos + (visibleLength / 150f) * slantX, yPos + visibleLength),
                        strokeWidth = 2.5f
                    )
                }
                if (showSplash) {
                    val splashW = 10f
                    drawLine(
                        color = Color.White.copy(alpha = drop.alpha * 1.5f),
                        start = Offset(xPos - splashW, collisionY - 1f),
                        end = Offset(xPos + splashW, collisionY - 1f),
                        strokeWidth = 2f
                    )
                }
            } else if (yPos < h) {
                drawLine(
                    color = Color.White.copy(alpha = drop.alpha),
                    start = Offset(xPos, yPos),
                    end = Offset(xPos + (drop.length / 150f) * slantX, yPos + drop.length),
                    strokeWidth = 2.5f
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
        animationSpec = tween(1000, easing = LinearOutSlowInEasing),
        label = "winAlpha"
    )

    var buttonBounds by remember { mutableStateOf<Rect?>(null) }

    // Shake logic for logo on tap-hold
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isShaking by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isShaking = true
        } else {
            isShaking = false
        }
    }

    val shakeTransition = rememberInfiniteTransition(label = "kanjiShake")
    val shakeAnim by shakeTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
    ) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
            RainBackground(buttonBounds)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1.3f))

                // Large Kanji logo
                Image(
                    painter = painterResource(id = R.drawable.kanji_congrats),
                    contentDescription = "Congratulations",
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .aspectRatio(180f / 312f)
                        .graphicsLayer {
                            translationX = if (isShaking) shakeAnim else 0f
                            rotationZ = if (isShaking) shakeAnim * 0.5f else 0f
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { }
                )

                Spacer(modifier = Modifier.height(72.dp))

                // Solved time text
                Text(
                    text = "SOLVED IN ${formatTimer(timerSeconds)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontFamily = ReemKufi,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // PLAY AGAIN Button (Outline design)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp)
                        .onGloballyPositioned { buttonBounds = it.boundsInRoot() }
                        .border(2.dp, FutoshikiColors.Coral, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPlayAgain() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PLAY AGAIN",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ReemKufi,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
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

    var pillCenter by remember { mutableStateOf(Offset.Zero) }
    var pillOffset by remember { mutableStateOf(Offset.Zero) }
    var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val isPaused = state.screen == Screen.PAUSE

    // Pause on back button/gesture
    BackHandler(enabled = !isPaused && !won) {
        viewModel.pause()
    }

    // Pause on lifecycle events (suspend/minimise)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FutoshikiColors.Background)
            .onGloballyPositioned { containerCoordinates = it }
            .pointerInput(Unit) {
                detectTapGestures {
                    viewModel.deselectCell()
                }
            }
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
        val gapTotal   = vh * 0.18f
        val boardBudgetH = vh - headerH - tabH - numpadH - refreshH - gapTotal

        val arrowRatio = 0.32f
        val boardUnits = size + arrowRatio * (size - 1)
        val cellSizeDp = minOf(boardBudgetH / boardUnits, usableW / boardUnits)
        val arrowSlotDp = cellSizeDp * arrowRatio

        val numpadSpacing = 8.dp
        val numpadBtnDp = minOf((usableW - numpadSpacing * (size - 1)) / size, vh * 0.08f)

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
                    isPaused = isPaused,
                    onClick = { viewModel.pause() },
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            containerCoordinates?.let { container ->
                                if (container.isAttached && coords.isAttached) {
                                    val localPos = container.localPositionOf(coords, Offset.Zero)
                                    pillOffset = localPos
                                    pillCenter = Offset(localPos.x + coords.size.width / 2f, localPos.y + coords.size.height / 2f)
                                }
                            }
                        }
                        .graphicsLayer { alpha = if (isPaused) 0f else 1f }
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

            Spacer(Modifier.height(vh * 0.05f))

            // ── Number pad ────────────────────────────────────────────────
            NumberPad(
                size         = size,
                buttonSizeDp = numpadBtnDp,
                spacingDp    = numpadSpacing,
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
        if (isPaused && pillCenter != Offset.Zero) {
            PauseOverlay(
                revealCenter = pillCenter,
                pillOffset = pillOffset,
                seconds = state.timerSeconds,
                onResume   = { viewModel.resume() },
                onMainMenu = { viewModel.goToMainMenu() },
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
