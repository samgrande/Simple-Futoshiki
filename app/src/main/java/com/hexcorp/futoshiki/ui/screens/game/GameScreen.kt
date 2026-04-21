package com.hexcorp.futoshiki.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hexcorp.futoshiki.GodotBounds
import com.hexcorp.futoshiki.LocalGodotBounds
import com.hexcorp.futoshiki.game.FutoshikiViewModel
import com.hexcorp.futoshiki.game.Screen
import org.godotengine.godot.GodotFragment
import com.hexcorp.futoshiki.ui.components.shared.DraggableSizeTabs
import com.hexcorp.futoshiki.ui.screens.pause.PauseOverlay
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.components.shared.TimerPill
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark

@Composable
fun GameScreen(
    viewModel: FutoshikiViewModel,
    state: com.hexcorp.futoshiki.game.GameState,
    godotFragment: GodotFragment? = null
) {
    val puzzle    = state.puzzle ?: return
    val size      = state.size
    val grid      = state.grid
    val selected  = state.selected
    val errors    = state.errors
    val won       = state.won
    val gameKey   = state.gameKey

    LaunchedEffect(errors) {
        if (errors.isNotEmpty() && godotFragment != null) {
            godotFragment.getGodot()?.runOnRenderThread {
                // godotFragment.getGodot()?.nativeCall("update_aggression", 1.0)
            }
        }
    }

    var pillCenter by remember { mutableStateOf(Offset.Zero) }
    var pillOffset by remember { mutableStateOf(Offset.Zero) }
    var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var showTabs by rememberSaveable { mutableStateOf(false) }

    val isPaused = state.screen == Screen.PAUSE
    val hideGameContent = state.screen != Screen.GAME

    var keepPauseOverlayVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.screen) {
        if (state.screen == Screen.PAUSE) {
            keepPauseOverlayVisible = true
        } else if (keepPauseOverlayVisible) {
            delay(220)
            keepPauseOverlayVisible = false
        }
    }

    val showPauseOverlay = state.screen == Screen.PAUSE || keepPauseOverlayVisible
    val showScreenShield = state.screen != Screen.GAME || keepPauseOverlayVisible

    LaunchedEffect(state.screen) {
        if (state.screen != Screen.GAME && state.screen != Screen.PAUSE) {
            showTabs = false
        }
    }

    val canGoBack = !won || state.isSolved
    BackHandler(enabled = canGoBack) {
        if (isPaused) {
            viewModel.resume()
        } else if (state.isSolved) {
            viewModel.newGame(size)
        } else {
            viewModel.pause()
        }
    }

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
            .onGloballyPositioned { containerCoordinates = it }
    ) {
        val vw = maxWidth
        val vh = maxHeight

        val hPad = 20.dp
        val usableW = (vw - hPad * 2).coerceAtMost(380.dp)

        val headerH = vh * 0.11f
        val tabH = vh * 0.065f
        val numpadH = vh * 0.095f
        val refreshH = vh * 0.075f
        val gapTotal = vh * 0.18f
        val boardBudgetH = vh - headerH - tabH - numpadH - refreshH - gapTotal
        val totalTopSpace = vh * 0.26f

        val bgColor = FutoshikiColors.background()

        if (!state.isSolved) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(headerH + 16.dp)
                        .background(bgColor)
                )
                Spacer(Modifier.height(totalTopSpace - headerH - 16.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(bgColor)
                )
            }
        } else {
            Box(Modifier.fillMaxSize().background(bgColor))
        }

        val arrowRatio = 0.32f
        val boardUnits = size + arrowRatio * (size - 1)
        val cellSizeDp = minOf(boardBudgetH / boardUnits, usableW / boardUnits)
        val arrowSlotDp = cellSizeDp * arrowRatio

        val numpadSpacing = 8.dp
        val numpadBtnDp = minOf((usableW - numpadSpacing * (size - 1)) / size, vh * 0.08f)

        val isDark = LocalIsDark.current
        val targetCardBg = if (showTabs) {
            if (isDark) Color(0xFF1E1E1E) else Color(0xFFE0E0E0)
        } else {
            Color.Transparent
        }
        val targetCardBorder = if (showTabs) Color.Black else Color.Transparent

        val animatedBg by animateColorAsState(targetCardBg, tween(400), label = "cardBg")
        val animatedBorder by animateColorAsState(targetCardBorder, tween(400), label = "cardBorder")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 420.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer { alpha = if (hideGameContent) 0f else 1f }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            viewModel.deselectCell()
                            showTabs = false
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.isSolved) {
                    val pillHeight = tabH
                    val pillSpacing = vh * 0.08f
                    val topPadding = (totalTopSpace - pillHeight - pillSpacing).coerceAtLeast(0.dp)

                    Spacer(Modifier.height(topPadding))
                    ThemedPillButton(
                        label = "S O L U T I O N",
                        onClick = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = hPad)
                            .height(pillHeight)
                    )
                    Spacer(Modifier.height(pillSpacing))
                } else {
                    Spacer(Modifier.height(headerH + 16.dp))
                    val setGodotBounds = LocalGodotBounds.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(totalTopSpace - headerH - 16.dp)
                            .onGloballyPositioned { coords ->
                                if (coords.isAttached) {
                                    val pos = coords.positionInWindow()
                                    setGodotBounds(
                                        GodotBounds(
                                            left = pos.x.toInt(),
                                            top = pos.y.toInt(),
                                            width = coords.size.width,
                                            height = coords.size.height
                                        )
                                    )
                                }
                            }
                    )
                }

                DisposableEffect(Unit) {
                    onDispose { /* bounds are cleared by FutoshikiApp when leaving GAME */ }
                }

                Spacer(Modifier.height(35.dp))

                val boardKey = remember(state.isSolved, gameKey) {
                    if (state.isSolved) 9999 + gameKey else gameKey
                }

                PuzzleBoard(
                    puzzle = puzzle,
                    grid = grid,
                    size = size,
                    selected = selected,
                    errors = errors,
                    cellSizeDp = cellSizeDp,
                    arrowSlotDp = arrowSlotDp,
                    gameKey = boardKey,
                    isSolved = state.isSolved,
                    onCellTap = { r, c -> if (!state.isSolved) viewModel.selectCell(r, c) },
                    onCellClear = { r, c -> if (!state.isSolved) viewModel.clearCell(r, c) },
                    modifier = Modifier.padding(horizontal = hPad)
                )

                if (!state.isSolved) {
                    Spacer(Modifier.height(vh * 0.025f))

                    NumberPad(
                        size = size,
                        buttonSizeDp = numpadBtnDp,
                        spacingDp = numpadSpacing,
                        onNumber = { viewModel.inputNumber(it) },
                        modifier = Modifier.padding(horizontal = hPad)
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = hPad)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = if (state.isSolved) {
                        Arrangement.Center
                    } else {
                        Arrangement.spacedBy(12.dp)
                    }
                ) {
                    ThemedPillButton(
                        label = "NEW GAME",
                        onClick = { viewModel.newGame(size) },
                        modifier = if (state.isSolved) Modifier.fillMaxWidth(0.6f) else Modifier.weight(1f)
                    )
                    if (!state.isSolved) {
                        ThemedPillButton(
                            label = "CLEAR",
                            onClick = { viewModel.clearAll() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showTabs && !state.isSolved,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.zIndex(0.5f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .pointerInput(Unit) {
                            detectTapGestures { showTabs = false }
                        }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                if (!state.isSolved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(animatedBg)
                            .border(
                                width = 1.dp,
                                color = animatedBorder,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .animateContentSize(animationSpec = tween(400))
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(headerH),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FutoshikiTitle(
                                    size = size,
                                    fontSize = 28.sp,
                                    isSolved = false,
                                    showTabs = showTabs,
                                    onClick = { showTabs = !showTabs },
                                    showUnderline = false
                                )

                                TimerPill(
                                    seconds = state.timerSeconds,
                                    won = won,
                                    isPaused = isPaused,
                                    onClick = {
                                        showTabs = false
                                        viewModel.pause()
                                    },
                                    modifier = Modifier
                                        .onGloballyPositioned { coords ->
                                            containerCoordinates?.let { container ->
                                                if (container.isAttached && coords.isAttached) {
                                                    val localPos = container.localPositionOf(coords, Offset.Zero)
                                                    pillOffset = localPos
                                                    pillCenter = Offset(
                                                        localPos.x + coords.size.width / 2f,
                                                        localPos.y + coords.size.height / 2f
                                                    )
                                                }
                                            }
                                        }
                                        .graphicsLayer { alpha = if (hideGameContent) 0f else 1f }
                                )
                            }

                            AnimatedVisibility(
                                visible = showTabs,
                                enter = fadeIn(animationSpec = tween(400)),
                                exit = fadeOut(animationSpec = tween(400))
                            ) {
                                Column {
                                    Spacer(Modifier.height(vh * 0.02f))
                                    DraggableSizeTabs(
                                        currentSize = size,
                                        onSizeChange = { viewModel.changeSize(it) },
                                        height = tabH,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(vh * 0.015f))
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(headerH),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FutoshikiTitle(
                                size = size,
                                fontSize = 28.sp,
                                isSolved = true,
                                onClick = null,
                                showUnderline = false
                            )
                        }
                    }
                }
            }
        }

        if (showScreenShield) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FutoshikiColors.background())
                    .zIndex(9f)
            )
        }

        if (showPauseOverlay) {
            PauseOverlay(
                revealCenter = pillCenter,
                pillOffset = pillOffset,
                seconds = state.timerSeconds,
                onResume = { viewModel.resume() },
                onMainMenu = { viewModel.goToMainMenu() },
                onSolve = { viewModel.solve() },
                onNewGame = { viewModel.newGame(size) },
                onTheming = { viewModel.goToThemingFromGame() },
                modifier = Modifier.zIndex(10f)
            )
        }

        if (state.showCongrats) {
            WinModal(
                timerSeconds = state.timerSeconds,
                onPlayAgain = { viewModel.newGame(size) }
            )
        }
    }
}