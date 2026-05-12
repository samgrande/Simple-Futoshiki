package com.hexcorp.futoshiki.ui.screens.pause

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.game.Difficulty
import com.hexcorp.futoshiki.ui.components.shared.*
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import com.hexcorp.futoshiki.ui.korge.KorGEView
import kotlin.math.roundToInt

@Composable
fun PauseOverlay(
    revealCenter: Offset,
    pillOffset: Offset,
    seconds: Int,
    won: Boolean = false,
    currentSize: Int = 4,
    currentDifficulty: Difficulty = Difficulty.EASY,
    onResume: () -> Unit,
    onMainMenu: () -> Unit,
    onNewGame: (Int, Difficulty) -> Unit,
    onTheming: () -> Unit,
    onDifficultySave: (Difficulty) -> Unit = {},
    korgeManager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager,
    isDark: Boolean,
    themeMode: ThemeMode,
    customMonoAccent: Boolean,
    customDayNight: Boolean,
    modifier: Modifier = Modifier,
    startWithQuitConfirm: Boolean = false,
    onConfirmQuitChange: (Boolean) -> Unit = {},
    onConfirmNewGameChange: (Boolean) -> Unit = {},
    onShowHelpChange: (Boolean) -> Unit = {}
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showConfirmQuit by rememberSaveable { mutableStateOf(startWithQuitConfirm) }
    var newGameExpanded by rememberSaveable { mutableStateOf(false) }
    
    var selectedSize by remember { mutableIntStateOf(currentSize) }
    var selectedDifficulty by remember { mutableStateOf(currentDifficulty) }

    // Notify parent when entering/exiting confirm quit screen
    LaunchedEffect(showConfirmQuit) {
        onConfirmQuitChange(showConfirmQuit)
    }

    // Notify parent when entering/exiting help screen
    LaunchedEffect(showHelp) {
        onShowHelpChange(showHelp)
    }

    BackHandler(enabled = true) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> if (won) onResume() else showConfirmQuit = false
            newGameExpanded -> {
                selectedSize = currentSize
                selectedDifficulty = currentDifficulty
                newGameExpanded = false
            }
            else -> onResume()
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val vh = maxHeight - navBarBottom

        val isSmallScreen = vh < 800.dp
        val headerH = if (isSmallScreen) vh * 0.07f else vh * 0.09f
        val ninjaH = if (isSmallScreen) 100.dp else 135.dp
        val korgeGap = if (isSmallScreen) 14.dp else 16.dp
        val korgeHeight = headerH + korgeGap + ninjaH

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FutoshikiColors.background()),
            contentAlignment = Alignment.Center
        ) {
            if (newGameExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { 
                                selectedSize = currentSize
                                selectedDifficulty = currentDifficulty
                                newGameExpanded = false 
                            }
                        )
                )
            }
            
            val isSkyboxDark = if (themeMode == ThemeMode.CUSTOM) customMonoAccent else isDark
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(korgeHeight)
                    .align(Alignment.TopCenter)
            ) {
                // KorGEView removed - managed by MainActivity
            }
            
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxHeight()
                    .systemBarsPadding()
                    .padding(horizontal = 20.dp, vertical = if (isSmallScreen) 30.dp else 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(korgeHeight + if (isSmallScreen) 0.dp else 8.dp))

                AnimatedContent(
                    targetState = when {
                        showConfirmQuit -> "confirm_quit"
                        showHelp -> "help"
                        else -> "menu"
                    },
                    transitionSpec = {
                        val duration = 280
                        if (targetState != "menu") {
                            (slideInVertically(tween(duration)) { it / 4 } + fadeIn(tween(duration)))
                                .togetherWith(
                                    slideOutVertically(tween(duration)) { -it / 4 } + fadeOut(tween(400))
                                )
                        } else {
                            fadeIn(tween(duration))
                                .togetherWith(fadeOut(tween(200)))
                        }.using(SizeTransform(clip = false))
                    },
                    label = "pauseContentTransition"
                ) { currentState ->
                    val isDark = LocalIsDark.current
                    when (currentState) {
                        "help" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                            ) {
                                Spacer(Modifier.height(24.dp))
                                HelpPanel(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .fillMaxHeight(0.8f),
                                    scrollable = true
                                )
                                Spacer(Modifier.height(20.dp))
                                BigButton(
                                    label = "BACK",
                                    onClick = { showHelp = false },
                                    inverted = true,
                                    isDark = isDark
                                )
                            }
                        }

                        "confirm_quit" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(Modifier.height(32.dp))
                                Text(
                                    text = "QUIT TO MAIN MENU?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PixelF,
                                    color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(32.dp))
                                BigButton(
                                    label = "YES",
                                    onClick = onMainMenu,
                                    inverted = true,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(20.dp))
                                BigButton(
                                    label = "NO",
                                    onClick = { if (won) onResume() else showConfirmQuit = false },
                                    isDark = isDark
                                )
                            }
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FutoshikiTitle(fontSize = if (isSmallScreen) 30.sp else 32.sp)
                                Spacer(Modifier.height(if (isSmallScreen) 24.dp else 36.dp))
                                
                                ExpandableStartButton(
                                    label = "NEW GAME",
                                    isExpanded = newGameExpanded,
                                    onExpandToggle = { newGameExpanded = !newGameExpanded },
                                    selectedSize = selectedSize,
                                    onSizeSelected = { selectedSize = it },
                                    selectedDifficulty = selectedDifficulty,
                                    onDifficultyChange = { selectedDifficulty = it },
                                    onStart = { onNewGame(selectedSize, selectedDifficulty) },
                                    isDark = isDark,
                                    onDifficultySave = onDifficultySave,
                                    currentSize = currentSize,
                                    currentDifficulty = currentDifficulty,
                                    hasActiveGame = !won,
                                    isSmallScreen = isSmallScreen
                                )

                                AnimatedVisibility(
                                    visible = !newGameExpanded,
                                    enter = fadeIn(tween(300)),
                                    exit = fadeOut(tween(200))
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Spacer(Modifier.height(if (isSmallScreen) 18.dp else 20.dp))

                                        BigButton(
                                            label = "HELP",
                                            onClick = { showHelp = true },
                                            isDark = isDark,
                                            height = if (isSmallScreen) 58.dp else 64.dp
                                        )

                                        Spacer(Modifier.height(if (isSmallScreen) 18.dp else 20.dp))

                                        BigButton(
                                            label = "THEMES",
                                            onClick = onTheming,
                                            isDark = isDark,
                                            height = if (isSmallScreen) 58.dp else 64.dp
                                        )

                                        Spacer(Modifier.height(if (isSmallScreen) 18.dp else 20.dp))

                                        BigButton(
                                            label = "MAIN MENU",
                                            onClick = { showConfirmQuit = true },
                                            inverted = false,
                                            isDark = isDark,
                                            height = if (isSmallScreen) 58.dp else 64.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }
        }
    }
}