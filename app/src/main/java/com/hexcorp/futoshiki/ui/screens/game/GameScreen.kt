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

    BackHandler(enabled = !isPaused && !won) {
        viewModel.pause()
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

            DraggableSizeTabs(
                currentSize  = size,
                onSizeChange = { viewModel.changeSize(it) },
                height       = tabH,
                modifier     = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(vh * 0.03f))

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

            NumberPad(
                size         = size,
                buttonSizeDp = numpadBtnDp,
                spacingDp    = numpadSpacing,
                onNumber     = { viewModel.inputNumber(it) }
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (vh * 0.06f)),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemedPillButton(
                    label    = "NEW GAME",
                    onClick  = { viewModel.newGame(size) },
                    modifier = Modifier.weight(1f)
                )
                ThemedPillButton(
                    label    = "CLEAR",
                    onClick  = { viewModel.clearAll() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (isPaused && pillCenter != Offset.Zero) {
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
