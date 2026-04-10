package com.hexcorp.futoshiki.ui.screens.theming

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.ThemeMode

@Composable
fun ThemingScreen(
    currentTheme: AppTheme,
    themeMode: ThemeMode,
    isDark: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scope: AnimatedVisibilityScope? = null
) {
    BackHandler(onBack = onBack)

    var currentIndex by remember {
        mutableIntStateOf(themes.indexOfFirst { it.theme == currentTheme }.coerceAtLeast(0))
    }
    var direction by remember { mutableIntStateOf(1) }

    fun navigate(next: Boolean) {
        direction = if (next) 1 else -1
        if (next) {
            currentIndex = (currentIndex + 1) % themes.size
        } else {
            currentIndex = (currentIndex - 1 + themes.size) % themes.size
        }
        onThemeChange(themes[currentIndex].theme)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FutoshikiColors.background())
            .pointerInput(Unit) {
                var accumulatedDrag = 0f
                var hasTriggered = false
                detectHorizontalDragGestures(
                    onDragStart = {
                        accumulatedDrag = 0f
                        hasTriggered = false
                    },
                    onDragEnd = { hasTriggered = false },
                    onDragCancel = { hasTriggered = false }
                ) { change, dragAmount ->
                    change.consume()
                    accumulatedDrag += dragAmount
                    if (!hasTriggered) {
                        val threshold = 60f
                        if (accumulatedDrag > threshold) {
                            navigate(false)
                            hasTriggered = true
                        } else if (accumulatedDrag < -threshold) {
                            navigate(true)
                            hasTriggered = true
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "T H E M E S",
                fontSize = 13.sp,
                fontFamily = ReemKufi,
                fontWeight = FontWeight.SemiBold,
                color = FutoshikiColors.onSurface().copy(alpha = 0.6f),
                letterSpacing = 4.sp,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                enter = slideInVertically(tween(600)) { -it * 2 } + fadeIn(tween(400)),
                                exit = slideOutVertically(tween(600)) { -it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier)
            )

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                enter = slideInVertically(tween(600)) { -it * 2 } + fadeIn(tween(400)),
                                exit = slideOutVertically(tween(600)) { -it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        if (direction > 0) {
                            (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 2 })
                                .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 2 })
                                .using(SizeTransform(clip = false))
                        } else {
                            (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 2 })
                                .togetherWith(fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 2 })
                                .using(SizeTransform(clip = false))
                        }
                    },
                    label = "themeLogoTransition"
                ) { index ->
                    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                        // Blurred shadow: 0 offset, dark mode gets darker shadow
                        Image(
                            painter = painterResource(id = themes[index].iconResId),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(FutoshikiColors.shadowColor()),
                            modifier = Modifier
                                .size(200.dp)
                                .blur(12.dp)
                        )
                        Image(
                            painter = painterResource(id = themes[index].iconResId),
                            contentDescription = themes[index].name,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                enter = slideInVertically(tween(600)) { it * 2 } + fadeIn(tween(400)),
                                exit = slideOutVertically(tween(600)) { it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navigate(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀",
                        fontSize = 12.sp,
                        color = FutoshikiColors.onSurface()
                    )
                }

                Spacer(Modifier.width(24.dp))

                Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = themes[currentIndex].name,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "themeNameTransition"
                    ) { name ->
                        Text(
                            text = name,
                            fontSize = 13.sp,
                            fontFamily = ReemKufi,
                            fontWeight = FontWeight.Medium,
                            color = FutoshikiColors.onSurface(),
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navigate(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        fontSize = 12.sp,
                        color = FutoshikiColors.onSurface()
                    )
                }
            }

            Spacer(Modifier.weight(0.5f))

            ThemeModeSlider(
                currentTheme = themes[currentIndex].theme,
                currentMode = themeMode,
                onModeChange = onThemeModeChange,
                isDark = isDark,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 24.dp)
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                enter = slideInVertically(tween(600)) { it * 2 } + fadeIn(tween(400)),
                                exit = slideOutVertically(tween(600)) { it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier)
            )

            Spacer(Modifier.height(80.dp))

            Box(
                modifier = if (scope != null) {
                    with(scope) {
                        Modifier.animateEnterExit(
                            enter = slideInVertically(tween(600)) { it * 2 } + fadeIn(tween(400)),
                            exit = slideOutVertically(tween(600)) { it * 2 } + fadeOut(tween(400))
                        )
                    }
                } else Modifier
            ) {
                BigButton(
                    label = "BACK",
                    onClick = onBack,
                    primary = true,
                    isDark = isDark
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
