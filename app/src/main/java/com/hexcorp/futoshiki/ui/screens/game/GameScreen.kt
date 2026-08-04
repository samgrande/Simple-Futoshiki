package com.hexcorp.futoshiki.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GameScreen(
    viewModel: FutoshikiViewModel,
    state: com.hexcorp.futoshiki.game.GameState,
    showKorge: Boolean = true,
) {
    val puzzle    = state.puzzle
    if (puzzle == null && !state.isGenerating) return
    val size      = state.size
    val grid      = state.grid
    val selected  = state.selected
    val errors    = state.errors
    val won       = state.won
    val gameKey   = state.gameKey

    val pillCenter = Offset(state.pillCenterX, state.pillCenterY)
    val pillOffset = Offset(state.pillOffsetX, state.pillOffsetY)
    // Pill positioning moved to MainActivity (above KorGE z-layer)
    // Countdown: observe the ninja signal
    val ninjaRunning by viewModel.korgeManager.runningStarted.collectAsStateWithLifecycle()
    val sceneLoaded by viewModel.korgeManager.sceneLoaded.collectAsStateWithLifecycle()
    val showCountdownTrigger by viewModel.korgeManager.showCountdownOnResume.collectAsStateWithLifecycle()
    val freshGameStart by viewModel.korgeManager.freshGameStart.collectAsStateWithLifecycle()

    // Track previous gameKey to detect actual new games
    var previousGameKey by remember { mutableIntStateOf(0) }
    
    // showCountdown is a local var — true for the very first game intro, false for repeated games.
    // It only flips false via onDone (called after GO! + brief hold in CountdownOverlay).
    var showCountdown by remember { mutableStateOf(false) }

    // Track if countdown has actually started (for fresh game to ensure 3-2-1 plays)
    var countdownStarted by remember { mutableStateOf(false) }
    var countdownFinished by remember { mutableStateOf(false) }

    // Track if this is a fresh game from main menu (for full countdown)
    var isFreshGame by remember { mutableStateOf(false) }

    var keepPauseOverlayVisible by remember { mutableStateOf(false) }
    var isSolveMode by remember { mutableStateOf(false) }
    var newGameExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedSize by remember { mutableIntStateOf(state.size) }
    var selectedDifficulty by remember { mutableStateOf(state.difficulty) }


    // When gameKey changes (newGame), show countdown only for actual game starts
    LaunchedEffect(gameKey) {
        // Only show countdown if gameKey actually changed (new game started)
        // This prevents countdown from showing on pause/resume
        if (gameKey != previousGameKey) {
            newGameExpanded = false
            isSolveMode = false
            if (freshGameStart || showCountdownTrigger) {
                countdownStarted = false
                // Both fresh game from main menu and new game from pause get full countdown
                isFreshGame = true
                showCountdown = true
                viewModel.korgeManager.clearFreshGameStart()
                viewModel.korgeManager.clearShowCountdown()
            }
        }
        previousGameKey = gameKey
    }

    // For fresh game starts, we need to track when countdown actually starts
    LaunchedEffect(showCountdown) {
        if (showCountdown && !countdownStarted) {
            countdownStarted = true
        }
    }

    // Hold the timer while the countdown overlay is active.
    // prevShowCountdown skips the initial composition (generation/PREPARING phase)
    // so the timer only starts once the countdown has actually run.
    var prevShowCountdown by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(showCountdown) {
        val prev = prevShowCountdown
        prevShowCountdown = showCountdown
        if (prev == null) return@LaunchedEffect
        viewModel.setCountdownActive(showCountdown)
        if (showCountdown) {
            viewModel.pauseTimer()
        } else {
            delay(300)
            viewModel.resumeTimer()
        }
    }

    // Block back button for 1 second after countdown ends
    LaunchedEffect(countdownFinished) {
        if (countdownFinished) {
            delay(1000)
            showCountdown = false
            countdownFinished = false
        }
    }

    val isPaused = state.screen == Screen.PAUSE
    val hideGameContent = state.screen != Screen.GAME

    LaunchedEffect(state.screen) {
        if (state.screen == Screen.PAUSE) {
            keepPauseOverlayVisible = true
        } else if (keepPauseOverlayVisible) {
            delay(220)
            keepPauseOverlayVisible = false
        }
    }

    // Revert sit sprite when exiting solve mode
    LaunchedEffect(isSolveMode) {
        if (!isSolveMode && viewModel.korgeManager.gameWorld != null) {
            viewModel.korgeManager.gameWorld?.revertNinjaToRunSprite()
        }
    }

    val showPauseOverlay = state.screen == Screen.PAUSE || keepPauseOverlayVisible
    val showScreenShield = state.screen != Screen.GAME || keepPauseOverlayVisible


    val canGoBack = !won || state.isSolved || won || state.defeated
    BackHandler(enabled = canGoBack) {
        if (showCountdown) return@BackHandler
        if (isPaused) {
            viewModel.setForceQuitInPause(false)
            viewModel.resume()
        } else if (state.isSolved || won || state.defeated) {
            viewModel.setForceQuitInPause(true)
            viewModel.pause()
        } else {
            viewModel.pause()
        }
    }

    val showCountdownRef = rememberUpdatedState(showCountdown)
    val showCongratsRef = rememberUpdatedState(state.showCongrats)
    val showDefeatRef = rememberUpdatedState(state.showDefeat)
    val isSolvedRef = rememberUpdatedState(state.isSolved)
    val lifecycleOwner = LocalLifecycleOwner.current
    val wonRef = rememberUpdatedState(won)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val isInSpecialScreen = showCountdownRef.value ||
                showCongratsRef.value ||
                showDefeatRef.value ||
                (isSolvedRef.value && !showCongratsRef.value)
            if (event == Lifecycle.Event.ON_PAUSE && !wonRef.value && !isInSpecialScreen) {
                viewModel.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // When won, ensure we're not stuck in pause - but not on end-game screens
    LaunchedEffect(won) {
        val isEndGame = state.showCongrats || state.showDefeat || (state.isSolved && !state.showCongrats)
        if (won && state.screen == Screen.PAUSE && !isEndGame) {
            viewModel.resume()
        }
    }

    // Screen entrance handled by MainActivity's circular reveal
    val bgColor = FutoshikiColors.background()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val vw = maxWidth
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val vh = maxHeight - navBarBottom

        val hPad = 20.dp
        val usableW = (vw - hPad * 2).coerceAtMost(450.dp)

        val isSmallScreen = vh < 800.dp
        val headerH = if (isSmallScreen) vh * 0.07f else vh * 0.09f
        val ninjaH = if (isSmallScreen) 100.dp else 135.dp
        val footerBtnH = if (isSmallScreen) 58.dp else 64.dp
        val numpadBtnMaxH = if (isSmallScreen) vh * 0.065f else vh * 0.075f

        val gridTopSpaceTarget = if (isSmallScreen) 18.dp else 24.dp
        val gridTopSpace by animateDpAsState(
            targetValue = gridTopSpaceTarget,
            animationSpec = tween(400),
            label = "gridTopSpace"
        )

        val commonSpacing = if (isSmallScreen) 32.dp else 50.dp
        val footerTotalH = footerBtnH + 20.dp // 12dp row bottom + 8dp button bottom
        
        // Maximize board budget:
        // Subtract only the components that are actually above/below it
        val boardBudgetH = vh - headerH - ninjaH - gridTopSpaceTarget - numpadBtnMaxH - footerTotalH - 24.dp
        val totalTopSpace = vh * 0.26f

        val korgeGap = if (isSmallScreen) 14.dp else 16.dp
        val korgeHeight = headerH + korgeGap + ninjaH

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

                // Pause overlay is now handled in MainActivity for proper z-index
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
            targetValue = when {
                hideGameContent || showCountdown || state.showDefeat -> 0f
                newGameExpanded -> 0.2f
                else -> 1f
            },
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
                Spacer(Modifier.height(headerH + korgeGap))
                Spacer(Modifier.height(ninjaH))

                Spacer(Modifier.height(gridTopSpace)) // Lowered the grid

                if (state.showCongrats) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CongratsView(
                            timerSeconds = state.timerSeconds,
                            onPlayAgain = { viewModel.newGame(size) },
                            gridSize = state.size,
                            difficulty = state.difficulty,
                            isExpanded = newGameExpanded,
                            korgeView = viewModel.korgeManager.korgeAndroidView,
                            pauseCount = state.pauseCount,
                            pauseTimeMs = state.pauseTimeMs,
                            modifier = Modifier.padding(horizontal = hPad)
                        )
                    }
                } else if (state.showDefeat) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        DefeatView(
                            onPlayAgain = { viewModel.newGame(size) },
                            gridSize = state.size,
                            difficulty = state.difficulty,
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
                        if (state.isGenerating && !showCountdown) {
                            Text(
                                text = "PREPARING BOARD…",
                                color = FutoshikiColors.onSurface().copy(alpha = 0.85f),
                                fontSize = 16.sp,
                                fontFamily = com.hexcorp.futoshiki.ui.theme.PixelF,
                                letterSpacing = 2.sp
                            )
                        } else if (!state.isGenerating && puzzle != null) {
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
                        } else if (!showCountdown && !state.isGenerating) {
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
                visible = !showCountdown && !state.isGenerating,
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
                    isSolved      = state.isSolved || state.won || state.showCongrats || state.showDefeat,
                    showCountdown = showCountdown,
                    enabled       = !showCountdown && !state.isGenerating,
                    onClearAll    = { viewModel.clearAll() },
                    onSolve       = { viewModel.solve() },
                    isSolveMode   = isSolveMode,
                    onSolveModeChange = { isSolveMode = it },
                    hPad          = hPad,
                    isPaused      = isPaused,
                    onPauseClick  = {
                        if (!isPaused) viewModel.pause() else viewModel.resume()
                    },
                    newGameExpanded = newGameExpanded,
                    onNewGameExpandedChange = { newGameExpanded = it },
                    selectedSize = selectedSize,
                    onSizeSelected = { selectedSize = it },
                    selectedDifficulty = selectedDifficulty,
                    onDifficultyChange = { selectedDifficulty = it },
                    onNewGame = { s, d -> viewModel.newGame(s, d) },
                    buttonHeight = footerBtnH,
                    isDark = isDark,
                    hasActiveGame = state.screen == Screen.GAME || state.screen == Screen.PAUSE,
                    isSmallScreen = isSmallScreen
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
                onDone       = {
                    countdownFinished = true
                    viewModel.finishGenerating()
                },
                forceFullCountdown = isFreshGame,
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
                if (!state.isSolved && !state.showDefeat) {
                    CompositionLocalProvider(LocalIsDark provides LocalIsDark.current) {
                        GameHeader(
                            size = size,
                            won = won,
                            showCountdown = showCountdown,
                            onTitleClick = { },
                            onTitleLongClick = { },
                            headerH = headerH,
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
                    .pointerInput(Unit) { }
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
                    viewModel.setForceQuitInPause(false)
                    viewModel.setShowConfirmQuit(false)
                    viewModel.setShowConfirmNewGame(false)
                    viewModel.setShowHelp(false)
                    viewModel.resume()
                },
                onMainMenu = { viewModel.goToMainMenu() },
                onNewGame = { s, d -> viewModel.newGame(s, d) },
                onTheming = { viewModel.goToThemingFromGame() },
                onDifficultySave = { viewModel.saveDifficultyPreference(it) },
                korgeManager = viewModel.korgeManager,
                isDark = isDark,
                themeMode = state.themeMode,
                customMonoAccent = state.customMonoAccent,
                customDayNight = state.customDayNight,
                modifier = Modifier.zIndex(10f),
                startWithQuitConfirm = state.forceQuitInPause,
                onConfirmQuitChange = { show -> viewModel.setShowConfirmQuit(show) },
                onConfirmNewGameChange = { show -> viewModel.setShowConfirmNewGame(show) },
                onShowHelpChange = { show -> viewModel.setShowHelp(show) }
            )
        }


    }
}