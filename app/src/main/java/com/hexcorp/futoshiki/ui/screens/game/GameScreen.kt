package com.hexcorp.futoshiki.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hexcorp.futoshiki.game.FutoshikiViewModel
import com.hexcorp.futoshiki.game.Screen
import com.hexcorp.futoshiki.ui.components.shared.DraggableSizeTabs
import com.hexcorp.futoshiki.ui.screens.pause.PauseOverlay
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.components.shared.TimerPill
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors

@Composable
fun GameScreen(
    viewModel: FutoshikiViewModel,
    state: com.hexcorp.futoshiki.game.GameState
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

    // Keep the pause overlay visible while transitioning away to another screen (e.g. THEMING).
    // Only dismiss it when the screen explicitly returns to GAME (i.e. the user resumed).
    var showPauseOverlay by remember { mutableStateOf(isPaused) }
    LaunchedEffect(state.screen) {
        when (state.screen) {
            Screen.PAUSE -> showPauseOverlay = true
            Screen.GAME  -> showPauseOverlay = false
            else         -> { /* navigating away — keep overlay until fade completes */ }
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
            .background(FutoshikiColors.background())
            .onGloballyPositioned { containerCoordinates = it }
            .pointerInput(Unit) {
                detectTapGestures {
                    viewModel.deselectCell()
                }
            }
    ) {
        val vw = maxWidth
        val vh = maxHeight

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
                    isSolved = state.isSolved
                )
                if (!state.isSolved) {
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
            }

            if (!state.isSolved) {
                Spacer(Modifier.height(vh * 0.02f))

                DraggableSizeTabs(
                    currentSize = size,
                    onSizeChange = {
                        viewModel.changeSize(it)
                    },
                    height = tabH,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(vh * 0.03f))
            } else {
                Spacer(Modifier.height(vh * 0.02f))
                ThemedPillButton(
                    label = "S O L U T I O N",
                    onClick = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tabH)
                )
                Spacer(Modifier.height(vh * 0.03f))
            }

            val boardKey = remember(state.isSolved, gameKey) { if (state.isSolved) 9999 + gameKey else gameKey }
            PuzzleBoard(
                puzzle      = puzzle,
                grid        = grid,
                size        = size,
                selected    = selected,
                errors      = errors,
                cellSizeDp  = cellSizeDp,
                arrowSlotDp = arrowSlotDp,
                gameKey     = boardKey,
                isSolved    = state.isSolved,
                onCellTap   = { r, c -> if (!state.isSolved) viewModel.selectCell(r, c) },
                onCellClear = { r, c -> if (!state.isSolved) viewModel.clearCell(r, c) }
            )

            if (!state.isSolved) {
                Spacer(Modifier.height(vh * 0.05f))

                NumberPad(
                    size         = size,
                    buttonSizeDp = numpadBtnDp,
                    spacingDp    = numpadSpacing,
                    onNumber     = { viewModel.inputNumber(it) }
                )
            } else {
                Spacer(Modifier.height(vh * 0.05f))
                NumberPad(
                    size         = size,
                    buttonSizeDp = numpadBtnDp,
                    spacingDp    = numpadSpacing,
                    onNumber     = { /* Disabled in solution mode */ },
                    enabled      = false
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (vh * 0.06f)),
                horizontalArrangement = if (state.isSolved) Arrangement.Center else Arrangement.spacedBy(12.dp)
            ) {
                ThemedPillButton(
                    label    = "NEW GAME",
                    onClick  = { viewModel.newGame(size) },
                    modifier = if (state.isSolved) Modifier.fillMaxWidth(0.6f) else Modifier.weight(1f)
                )
                if (!state.isSolved) {
                    ThemedPillButton(
                        label    = "CLEAR",
                        onClick  = { viewModel.clearAll() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (showPauseOverlay && pillCenter != Offset.Zero) {
            PauseOverlay(
                revealCenter = pillCenter,
                pillOffset = pillOffset,
                seconds = state.timerSeconds,
                onResume   = { viewModel.resume() },
                onMainMenu = { viewModel.goToMainMenu() },
                onSolve    = { viewModel.solve() },
                onNewGame  = { viewModel.newGame(size) },
                onTheming  = { viewModel.goToThemingFromGame() }
            )
        }

        if (state.showCongrats) {
            WinModal(
                timerSeconds = state.timerSeconds,
                onPlayAgain  = { viewModel.newGame(size) }
            )
        }
    }
}
