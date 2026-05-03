package com.hexcorp.futoshiki.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hexcorp.futoshiki.game.FutoshikiViewModel
import com.hexcorp.futoshiki.game.Screen
import com.hexcorp.futoshiki.ui.screens.pause.PauseOverlay
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.korge.KorGEView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GameScreen(
    viewModel: FutoshikiViewModel,
    state: com.hexcorp.futoshiki.game.GameState,
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
    var showTabs by rememberSaveable { mutableStateOf(false) }

    // Countdown: observe the ninja signal
    val ninjaRunning by viewModel.korgeManager.runningStarted.collectAsStateWithLifecycle()

    // showCountdown is a local var — true for the very first game intro, false for repeated games.
    // It only flips false via onDone (called after GO! + brief hold in CountdownOverlay).
    var showCountdown by remember { mutableStateOf(!viewModel.korgeManager.introFinished) }

    // When gameKey changes (newGame) and the intro is NOT being skipped, re-show the countdown
    LaunchedEffect(gameKey) {
        showCountdown = !viewModel.korgeManager.introFinished
    }

    // Screen entrance: fade in when first composed
    var screenAlpha by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        // Let the first frame render, then animate in
        delay(50)
        screenAlpha = 1f
    }
    val animatedScreenAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = screenAlpha,
        animationSpec = tween(450),
        label = "screenEntrance"
    )

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
    val bgColor = FutoshikiColors.background()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .graphicsLayer { alpha = animatedScreenAlpha }
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

        val korgeHeight = headerH + 16.dp + 150.dp

        // Animated alpha for KorGE scene fade-in (starts at 0, fades in when ninja starts running)
        val korgeAlpha by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (ninjaRunning) 1f else 0f,
            animationSpec = tween(600),
            label = "korgeAlpha"
        )

        if (!state.isSolved) {
            key(state.gameKey) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(korgeHeight)
                ) {
                    // The KorGE scene itself, fades in when the ninja starts running
                    KorGEView(
                        manager = viewModel.korgeManager,
                        isPaused = isPaused,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(korgeHeight)
                            .graphicsLayer { alpha = korgeAlpha }
                    )
                    // Black overlay on top, fades away as KorGE fades in
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(korgeHeight)
                            .background(bgColor.copy(alpha = 1f - korgeAlpha))
                    )
                }
            }

            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(korgeHeight))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(bgColor)
                )
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(bgColor)
            )
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
                    Spacer(Modifier.height(170.dp))
                }

                DisposableEffect(Unit) {
                    onDispose { /* bounds are cleared by FutoshikiApp when leaving GAME */ }
                }

                Spacer(Modifier.height(35.dp))

                val boardKey = remember(state.isSolved, gameKey) {
                    if (state.isSolved) 9999 + gameKey else gameKey
                }

                if (state.showCongrats) {
                    CongratsView(
                        timerSeconds = state.timerSeconds,
                        onPlayAgain = { viewModel.newGame(size) },
                        modifier = Modifier.padding(horizontal = hPad)
                    )
                } else {
                    // The board slot: either countdown or puzzle board (same height, fades between them)
                    val boardH = cellSizeDp * (size + 0.32f * (size - 1))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(boardH),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = showCountdown,
                            transitionSpec = {
                                fadeIn(tween(350)) togetherWith fadeOut(tween(250))
                            },
                            label = "boardOrCountdown"
                        ) { counting ->
                            if (counting) {
                                CountdownOverlay(
                                    ninjaRunning = ninjaRunning,
                                    onDone       = { showCountdown = false },
                                    modifier     = Modifier.fillMaxSize()
                                )
                            } else {
                                PuzzleBoard(
                                    puzzle       = puzzle,
                                    grid         = grid,
                                    size         = size,
                                    selected     = selected,
                                    errors       = errors,
                                    cellSizeDp   = cellSizeDp,
                                    arrowSlotDp  = arrowSlotDp,
                                    gameKey      = boardKey,
                                    isSolved     = state.isSolved,
                                    onCellTap    = { r, c -> if (!state.isSolved) viewModel.selectCell(r, c) },
                                    onCellClear  = { r, c -> if (!state.isSolved) viewModel.clearCell(r, c) },
                                    modifier     = Modifier.padding(horizontal = hPad)
                                )
                            }
                        }
                    }

                    // Numpad always lives OUTSIDE the AnimatedContent so it never shifts
                    if (!state.isSolved && !showCountdown) {
                        Spacer(Modifier.height(vh * 0.025f))
                        NumberPad(
                            size         = size,
                            buttonSizeDp = numpadBtnDp,
                            spacingDp    = numpadSpacing,
                            onNumber     = { viewModel.inputNumber(it) },
                            modifier     = Modifier.padding(horizontal = hPad)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                GameFooter(
                    isSolved      = state.isSolved || state.won || state.showCongrats,
                    showCountdown = showCountdown,
                    onNewGame     = { viewModel.newGame(size) },
                    onClearAll    = { viewModel.clearAll() },
                    hPad          = hPad
                )
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
                    GameHeader(
                        size = size,
                        timerSeconds = state.timerSeconds,
                        won = won,
                        isPaused = isPaused,
                        showTabs = showTabs,
                        onTitleClick = { showTabs = !showTabs },
                        onTimerClick = {
                            showTabs = false
                            viewModel.pause()
                        },
                        onSizeChange = { viewModel.changeSize(it) },
                        animatedBg = animatedBg,
                        animatedBorder = animatedBorder,
                        headerH = headerH,
                        tabH = tabH,
                        containerCoordinates = containerCoordinates,
                        onPillPositioned = { offset, center ->
                            pillOffset = offset
                            pillCenter = center
                        },
                        hideGameContent = hideGameContent
                    )
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

        /* WinModal removed, now using inline CongratsView */
    }
}