package com.hexcorp.futoshiki.ui.screens.landing

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.components.shared.HelpPanel
import com.hexcorp.futoshiki.ui.components.shared.LogoMark
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.ReemKufi

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LandingScreen(
    onStart: () -> Unit,
    onTheming: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier,
    scope: AnimatedVisibilityScope? = null
) {
    var showHelp by remember { mutableStateOf(false) }
    var showConfirmQuit by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        when {
            showHelp -> showHelp = false
            showConfirmQuit -> showConfirmQuit = false
            else -> showConfirmQuit = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FutoshikiColors.background()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxHeight()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.2f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = slideOutVertically(tween(600)) { -it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = !showHelp,
                    contentAlignment = Alignment.Center,
                    transitionSpec = {
                        (fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.5f))
                            .togetherWith(fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.5f))
                            .using(SizeTransform(clip = false))
                    },
                    label = "logoTransition"
                ) { isVisible ->
                    if (isVisible) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.wrapContentSize()
                        ) {
                            LogoMark(size = 96.dp)
                            Spacer(Modifier.height(18.dp))
                        }
                    } else {
                        Spacer(Modifier.size(0.dp))
                    }
                }
            }

            Box(
                modifier = if (scope != null) {
                    with(scope) {
                        Modifier.animateEnterExit(
                            exit = slideOutVertically(tween(600)) { -it * 2 } + fadeOut(tween(400))
                        )
                    }
                } else Modifier
            ) {
                FutoshikiTitle(fontSize = 38.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                exit = slideOutVertically(tween(600)) { it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier)
            ) {
                AnimatedContent(
                    targetState = when {
                        showConfirmQuit -> "confirm"
                        showHelp -> "help"
                        else -> "menu"
                    },
                    transitionSpec = {
                        val duration = 300
                        if (targetState != "menu") {
                            (fadeIn(tween(duration)) + slideInVertically { it / 4 })
                                .togetherWith(fadeOut(tween(250)) + slideOutVertically { -it / 4 })
                        } else {
                            (fadeIn(tween(duration)) + slideInVertically { -it / 4 })
                                .togetherWith(fadeOut(tween(250)) + slideOutVertically { it / 4 })
                        }.using(SizeTransform(clip = false))
                    },
                    label = "landingContentTransition",
                    modifier = Modifier.fillMaxWidth()
                ) { state ->
                    val isDark = LocalIsDark.current
                    when (state) {
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
                                        .fillMaxHeight(0.7f),
                                    scrollable = true
                                )
                                Spacer(Modifier.height(20.dp))
                                BigButton(
                                    label = "BACK",
                                    onClick = { showHelp = false },
                                    isDark = isDark
                                )
                            }
                        }
                        "confirm" -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(Modifier.height(32.dp))
                                Text(
                                    text          = "Q U I T   T H E   G A M E  ?",
                                    fontSize      = 13.sp,
                                    fontWeight    = FontWeight.SemiBold,
                                    fontFamily    = ReemKufi,
                                    color         = if (isDark) Color(0xFF888888) else Color(0xFF999999),
                                    letterSpacing = 2.sp
                                )
                                Spacer(Modifier.height(32.dp))
                                BigButton(
                                    label = "Y E S",
                                    onClick = onQuit,
                                    primary = true,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(14.dp))
                                BigButton(
                                    label = "N O",
                                    onClick = { showConfirmQuit = false },
                                    isDark = isDark
                                )
                            }
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(Modifier.height(48.dp))
                                BigButton(
                                    label = "START",
                                    onClick = onStart,
                                    primary = true,
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(12.dp))
                                BigButton(
                                    label = "HELP",
                                    onClick = { showHelp = true },
                                    isDark = isDark
                                )
                                Spacer(Modifier.height(12.dp))
                                BigButton(
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
                                exit = slideOutVertically(tween(600)) { it * 4 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier)
            ) {
                Text(
                    text = "Made with ♡ by @HeX",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    fontFamily = ReemKufi
                )
            }
        }
    }
}
