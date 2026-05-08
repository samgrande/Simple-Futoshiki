package com.hexcorp.futoshiki.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
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
    showKorge: Boolean = true,
) {
    val puzzle    = state.puzzle ?: return
    val size      = state.size
    val grid      = state.grid
    val selected  = state.selected
    val errors    = state.errors
    val won       = state.won
    val gameKey   = state.gameKey

    val pillCenter = Offset(state.pillCenterX, state.pillCenterY)
    val pillOffset = Offset(state.pillOffsetX, state.pillOffsetY)
    var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var forceQuitInPause by rememberSaveable { mutableStateOf(false) }

    // Countdown: observe the ninja signal
    val ninjaRunning by viewModel.korgeManager.runningStarted.collectAsStateWithLifecycle()
    val sceneLoaded by viewModel.korgeManager.sceneLoaded.collectAsStateWithLifecycle()

    // showCountdown is a local var — true for the very first game intro, false for repeated games.
    // It only flips false via onDone (called after GO! + brief hold in CountdownOverlay).
    var showCountdown by remember { mutableStateOf(!viewModel.korgeManager.introFinished) }

    // When gameKey changes (newGame) and the intro is NOT being skipped, re-show the countdown
    LaunchedEffect(gameKey) {
        showCountdown = !viewModel.korgeManager.introFinished
    }

    // Hold the timer while the countdown overlay is active
    LaunchedEffect(showCountdown) {
        if (showCountdown) {
            viewModel.pauseTimer()
        } else {
            delay(300)
            viewModel.resumeTimer()
        }
    }

    val isPaused = state.screen == Screen.PAUSE
    val hideGameContent = state.screen != Screen.GAME

    var keepPauseOverlayVisible by remember { mutableStateOf(false) }
    var isSolveMode by remember { mutableStateOf(false) }
    
    // New Game states for the solution screen footer
    var newGameExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedSize by remember { mutableIntStateOf(state.size) }
    var selectedDifficulty by remember { mutableStateOf(state.difficulty) }

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


    val canGoBack = !won || state.isSolved || won // Always true if won
    BackHandler(enabled = canGoBack) {
        if (showCountdown) return@BackHandler
        if (isPaused) {
            forceQuitInPause = false
            viewModel.resume()
        } else if (state.isSolved || won) {
            forceQuitInPause = true
            viewModel.pause()
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

    // Screen entrance handled by MainActivity's circular reveal
    val bgColor = FutoshikiColors.background()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { containerCoordinates = it }
    ) {
        val vw = maxWidth
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val vh = maxHeight - navBarBottom

        val hPad = 20.dp
        val usableW = (vw - hPad * 2).coerceAtMost(450.dp)

        val isSmallScreen = vh < 720.dp
        val headerH = if (isSmallScreen) vh * 0.07f else vh * 0.09f
        val ninjaH = if (isSmallScreen) 80.dp else 120.dp
        val footerBtnH = 64.dp
        val numpadBtnMaxH = if (isSmallScreen) vh * 0.06f else vh * 0.075f
        
        val gridTopSpaceTarget = if (isSmallScreen) 12.dp else 24.dp
        val gridTopSpace by animateDpAsState(
            targetValue = gridTopSpaceTarget,
            animationSpec = tween(400),
            label = "gridTopSpace"
        )

        val commonSpacing = if (isSmallScreen) 16.dp else 50.dp
        val footerTotalH = footerBtnH + 20.dp // 12dp row bottom + 8dp button bottom
        
        // Maximize board budget:
        // Subtract only the components that are actually above/below it
        val boardBudgetH = vh - headerH - ninjaH - gridTopSpaceTarget - numpadBtnMaxH - footerTotalH - 24.dp
        val totalTopSpace = vh * 0.26f

        val korgeHeight = headerH + 16.dp + ninjaH

        // Cover alpha: 1 until scene is loaded, then fades to 0. Hides the green flash from
        // KorGE's uninitialized background color before assets are ready.
        val sceneCoverAlpha by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (sceneLoaded) 0f else 1f,
            animationSpec = tween(900),
            label = "sceneCover"
        )

        key(state.gameKey) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(korgeHeight)
                    .zIndex(2f)
            ) {
                // KorGEView removed - managed by MainActivity
                
                // Cover fades away once the scene signals it's loaded
                if (sceneCoverAlpha > 0f && showKorge) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(korgeHeight)
                            .background(bgColor.copy(alpha = sceneCoverAlpha))
                    )
                }

                // Solution overlay for the Ninja area
                AnimatedVisibility(
                    visible = state.isSolved && !state.showCongrats,
                    enter = fadeIn(tween(600)),
                    exit = fadeOut(tween(400))
                ) {
                    val overlayBg = if (state.isDark) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                    val overlayText = if (state.isDark) Color.White else Color.Black
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(overlayBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S O L U T I O N",
                            color = overlayText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }
        }

        // Fill background for the rest of the screen
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(korgeHeight))
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(bgColor)
            )
        }

        val arrowRatio = 0.32f
        val boardUnits = size + arrowRatio * (size - 1)
        val cellSizeDp = minOf(boardBudgetH / boardUnits, usableW / boardUnits)
        val arrowSlotDp = cellSizeDp * arrowRatio

        val numpadHPad = hPad * 0.9f
        val numpadSpacing = arrowSlotDp * 0.8f
        val numpadBtnDp = cellSizeDp

        val isDark = LocalIsDark.current

        val gridAlpha by animateFloatAsState(
            targetValue = if (hideGameContent || showCountdown || newGameExpanded) 0f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "gridAlpha"
        )
        val gridScale by animateFloatAsState(
            targetValue = if (hideGameContent || showCountdown) 0.92f else 1f,
            animationSpec = tween(300),
            label = "gridScale"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 420.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    scaleX = gridScale
                    scaleY = gridScale
                }
        ) {
            val gridBoxScope = this
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            viewModel.deselectCell()
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Keep grid position consistent by always using the same spacers
                Spacer(Modifier.height(headerH + 16.dp))
                Spacer(Modifier.height(ninjaH))

                Spacer(Modifier.height(gridTopSpace)) // Lowered the grid

                if (state.showCongrats) {
                    // Win state: replaces the grid and numpad with CongratsView
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CongratsView(
                            timerSeconds = state.timerSeconds,
                            onPlayAgain = { viewModel.newGame(size) },
                            isExpanded = newGameExpanded,
                            modifier = Modifier.padding(horizontal = hPad)
                        )
                    }
                } else {
                    // Playing state: shows the grid and numpad (or attempt info if solved via menu)
                    Spacer(Modifier.weight(1f))

                    val boardKey = remember(state.isSolved, gameKey, showCountdown) {
                        // Changing the key when countdown ends triggers the staggered pop animations in PuzzleBoard
                        if (state.isSolved) 9999 + gameKey 
                        else if (showCountdown) -1 
                        else gameKey
                    }

                    val boardH = cellSizeDp * (size + 0.32f * (size - 1))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(boardH)
                            .graphicsLayer { alpha = gridAlpha },
                        contentAlignment = Alignment.Center
                    ) {
                        PuzzleBoard(
                            puzzle       = puzzle,
                            grid         = grid,
                            size         = size,
                            selected     = selected,
                            errors       = errors,
                            cellSizeDp   = cellSizeDp,
                            arrowSlotDp  = arrowSlotDp,
                            gameKey      = boardKey,
                            isSolved     = state.isSolved || state.won,
                            onCellTap    = { r, c -> if (!state.isSolved && !state.won) viewModel.selectCell(r, c) },
                            onCellClear  = { r, c -> if (!state.isSolved && !state.won) viewModel.clearCell(r, c) },
                            modifier     = Modifier.padding(horizontal = hPad)
                        )
                    }

                    Spacer(Modifier.height(commonSpacing))

                    // Preserve space and show attempt info when solved via cheat
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(numpadBtnDp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isSolved) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "You tried for ${state.timerSeconds} seconds before giving up",
                                    color = FutoshikiColors.onSurface().copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Want to try again ?",
                                    color = FutoshikiColors.onSurface(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (!showCountdown) {
                            NumberPad(
                                size         = size,
                                buttonSizeDp = numpadBtnDp,
                                spacingDp    = numpadSpacing,
                                onNumber     = { viewModel.inputNumber(it) },
                                modifier     = Modifier.padding(horizontal = numpadHPad).graphicsLayer { alpha = gridAlpha }
                            )
                        }
                    }
                    Spacer(Modifier.height(commonSpacing))
                }
                Spacer(Modifier.height(footerTotalH))
            } // End Column

            // Invisible scrim to dismiss solve mode when clicking outside the button
            if (isSolveMode || newGameExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(20f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { 
                                isSolveMode = false 
                                newGameExpanded = false
                            }
                        )
                )
            }

            AnimatedVisibility(
                visible = !showCountdown,
                enter = fadeIn(tween(600, delayMillis = 500)) + 
                        slideInVertically(tween(600, delayMillis = 500)) { it / 2 },
                label = "footerEntrance",
                modifier = with(gridBoxScope) { 
                    Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(30f)
                        .navigationBarsPadding()
                }
            ) {
                GameFooter(
                    isSolved      = state.isSolved || state.won || state.showCongrats,
                    showCountdown = showCountdown,
                    onClearAll    = { viewModel.clearAll() },
                    onSolve       = { viewModel.solve() },
                    isSolveMode   = isSolveMode,
                    onSolveModeChange = { isSolveMode = it },
                    hPad          = hPad,
                    // New parameters for solution screen new game
                    newGameExpanded = newGameExpanded,
                    onNewGameExpandedChange = { newGameExpanded = it },
                    selectedSize = selectedSize,
                    onSizeSelected = { selectedSize = it },
                    selectedDifficulty = selectedDifficulty,
                    onDifficultyChange = { selectedDifficulty = it },
                    onNewGame = { s, d -> viewModel.newGame(s, d) },
                    buttonHeight = footerBtnH,
                    isDark = isDark
                )
            }




        }

        // Countdown overlay at zIndex 1.5 — below KorGE (2f) so corgi is always visible
        AnimatedVisibility(
            visible = showCountdown,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(180)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1.5f)
        ) {
            CountdownOverlay(
                ninjaRunning = ninjaRunning,
                onDone       = { showCountdown = false },
                modifier     = Modifier.fillMaxSize()
            )
        }

        // GameHeader lives in the outer BoxWithConstraints so it always renders above
        // the countdown overlay (which is inside the game content Box).
        if (!hideGameContent) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .align(Alignment.TopCenter)
                    .zIndex(3f)
            ) {
                if (!state.isSolved) {
                    val isCustomMonoNight = state.themeMode == com.hexcorp.futoshiki.ui.theme.ThemeMode.CUSTOM && !state.customMonoAccent && state.customDayNight
                    CompositionLocalProvider(LocalIsDark provides if (isCustomMonoNight) false else LocalIsDark.current) {
                        GameHeader(
                            size = size,
                            timerSeconds = state.timerSeconds,
                            won = won,
                            isPaused = isPaused,
                            showCountdown = showCountdown,
                            onTitleClick = { },
                            onTimerClick = {
                                if (!isPaused) viewModel.pause() else viewModel.resume()
                            },
                            onTimerLongClick = {
                                if (!won) {
                                    forceQuitInPause = true
                                    viewModel.pause()
                                }
                            },
                            onSizeChange = { newSize ->
                                viewModel.changeSize(newSize)
                            },
                            onTitleLongClick = { /* Handle if needed */ },
                            headerH = headerH,
                            containerCoordinates = containerCoordinates,
                            onPillPositioned = { offset, center ->
                                viewModel.updatePillPosition(offset, center)
                            },
                            hideGameContent = hideGameContent,
                            isSmallScreen = isSmallScreen,
                            isExpanded = newGameExpanded
                        )
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
                            // Logo removed in solution screen as requested
                        }
                    }
                }
            }
        }

        if (showScreenShield) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(vh - korgeHeight)
                    .align(Alignment.BottomCenter)
                    .background(FutoshikiColors.background())
                    .zIndex(9f)
            )
        }

        if (showPauseOverlay) {
            PauseOverlay(
                revealCenter = pillCenter,
                pillOffset = pillOffset,
                seconds = state.timerSeconds,
                won = won,
                currentSize = size,
                currentDifficulty = state.difficulty,
                onResume = {
                    forceQuitInPause = false
                    viewModel.resume()
                },
                onMainMenu = { viewModel.goToMainMenu() },
                onNewGame = { s, d -> viewModel.newGame(s, d) },
                onTheming = { viewModel.goToThemingFromGame() },
                korgeManager = viewModel.korgeManager,
                isDark = isDark,
                themeMode = state.themeMode,
                customMonoAccent = state.customMonoAccent,
                customDayNight = state.customDayNight,
                modifier = Modifier.zIndex(10f),
                startWithQuitConfirm = forceQuitInPause
            )
        }

    }
}