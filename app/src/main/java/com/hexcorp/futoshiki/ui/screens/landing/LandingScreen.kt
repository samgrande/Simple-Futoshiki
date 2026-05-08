package com.hexcorp.futoshiki.ui.screens.landing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import com.hexcorp.futoshiki.game.Difficulty
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import com.hexcorp.futoshiki.ui.korge.KorGEView

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LandingScreen(
    currentSize: Int,
    onStart: (Int, Difficulty) -> Unit,
    onTheming: () -> Unit,
    onQuit: () -> Unit,
    showKorge: Boolean = true,
    korgeManager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager,
    isSkyboxDark: Boolean = false,
    modifier: Modifier = Modifier,
    scope: AnimatedVisibilityScope? = null,
    onSizeSave: (Int) -> Unit = {}
) {
    var showHelp by remember { mutableStateOf(false) }
    var showConfirmQuit by remember { mutableStateOf(false) }
    var startExpanded by remember { mutableStateOf(false) }
    
    var selectedSize by remember { mutableIntStateOf(currentSize) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    val isDark = LocalIsDark.current

    BackHandler(enabled = true) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> showConfirmQuit = false
            startExpanded -> {
                selectedSize = currentSize
                selectedDifficulty = Difficulty.EASY
                startExpanded = false
            }
            else -> showConfirmQuit = true
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val vh = maxHeight - navBarBottom

        val isSmallScreen = vh < 720.dp
        val headerH = if (isSmallScreen) vh * 0.07f else vh * 0.09f
        val ninjaH = if (isSmallScreen) 80.dp else 120.dp
        val korgeHeight = headerH + 16.dp + ninjaH

        // KorGEView removed - managed by MainActivity

        if (startExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { 
                            selectedSize = currentSize
                            selectedDifficulty = Difficulty.EASY
                            startExpanded = false 
                        }
                    )
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxHeight()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
                .zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Match game screen layout - same height as korge + 16dp
            Spacer(Modifier.height(korgeHeight + 8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FutoshikiTitle(fontSize = 46.sp)
            }

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = fadeOut(tween(200))
                            )
                        }
                    } else Modifier)
            ) {
                if (showConfirmQuit || showHelp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (showConfirmQuit) {
                            Text(
                                text = "Q U I T   T H E   G A M E  ?",
                                fontSize = 13.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                fontFamily = PixelF,
                                color = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                letterSpacing = 2.sp
                            )
                            Spacer(Modifier.height(32.dp))
                            com.hexcorp.futoshiki.ui.components.shared.BigButton(
                                label = "Y E S",
                                onClick = onQuit,
                                inverted = true,
                                isDark = isDark
                            )
                            Spacer(Modifier.height(35.dp))
                            com.hexcorp.futoshiki.ui.components.shared.BigButton(
                                label = "N O",
                                onClick = { showConfirmQuit = false },
                                isDark = isDark
                            )
                        } else if (showHelp) {
                            Spacer(Modifier.height(24.dp))
                            com.hexcorp.futoshiki.ui.components.shared.HelpPanel(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .fillMaxHeight(0.8f),
                                scrollable = true
                            )
                            Spacer(Modifier.height(20.dp))
                            com.hexcorp.futoshiki.ui.components.shared.BigButton(
                                label = "BACK",
                                onClick = { showHelp = false },
                                inverted = true,
                                isDark = isDark
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        com.hexcorp.futoshiki.ui.components.shared.ExpandableStartButton(
                            label = "START",
                            isExpanded = startExpanded,
                            onExpandToggle = { startExpanded = !startExpanded },
                            selectedSize = selectedSize,
                            onSizeSelected = { 
                                selectedSize = it
                                onSizeSave(it)
                            },
                            selectedDifficulty = selectedDifficulty,
                            onDifficultyChange = { selectedDifficulty = it },
                            onStart = { 
                                korgeManager.gameWorld?.startGame(skipIntro = false)
                                onStart(selectedSize, selectedDifficulty) 
                            },
                            isDark = isDark
                        )
                        
                        AnimatedVisibility(
                            visible = !startExpanded,
                            enter = fadeIn(tween(200)),
                            exit = fadeOut(tween(150))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(Modifier.height(35.dp))
                                com.hexcorp.futoshiki.ui.components.shared.BigButton(
                                    label = "HELP",
                                    onClick = { showHelp = true },
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(35.dp))
                                com.hexcorp.futoshiki.ui.components.shared.BigButton(
                                    label = "THEMES",
                                    onClick = onTheming,
                                    isDark = isDark
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = fadeOut(tween(200))
                            )
                        }
                    } else Modifier)
            ) {
                Text(
                    text = "Made with love by @HeX",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    fontFamily = PixelF
                )
            }
        }
    }
}