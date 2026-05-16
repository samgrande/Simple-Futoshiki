package com.hexcorp.futoshiki.ui.screens.theming

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.accentColor
import com.hexcorp.futoshiki.ui.theme.LocalIsDark

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThemingScreen(
    currentTheme: AppTheme,
    themeMode: ThemeMode,
    isDark: Boolean,
    customMonoAccent: Boolean,
    customDayNight: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onCustomThemeChange: (monoAccent: Boolean, dayNight: Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scope: AnimatedVisibilityScope? = null
) {
    BackHandler(onBack = onBack)

    var currentIndex by remember {
        mutableIntStateOf(themes.indexOfFirst { it.theme == currentTheme }.coerceAtLeast(0))
    }
    var direction by remember { mutableIntStateOf(1) }

    // Global states from ViewModel are used directly

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
            .background(FutoshikiColors.background()),
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
                fontFamily = PixelF,
                fontWeight = FontWeight.SemiBold,
                color = FutoshikiColors.onSurface().copy(alpha = 0.6f),
                letterSpacing = 4.sp,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                enter = slideInVertically(tween(600)) { -it * 2 } + fadeIn(tween(400)),
                                exit = slideOutVertically(tween(600)) { -it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier)
            )

            Spacer(Modifier.weight(0.4f))

            Box(modifier = Modifier.statusBarsPadding()) {
                ThemeCarousel(
                    currentIndex = currentIndex,
                    direction = direction,
                    onNavigate = { next -> navigate(next) },
                    useAccentColor = themeMode == ThemeMode.CUSTOM && customMonoAccent,
                    scope = scope
                )
            }

            Spacer(Modifier.weight(0.7f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .then(if (scope != null) {
                        with(scope) {
                            Modifier.animateEnterExit(
                                enter = slideInVertically(tween(600)) { it * 2 } + fadeIn(tween(400)),
                                exit = slideOutVertically(tween(600)) { it * 2 } + fadeOut(tween(400))
                            )
                        }
                    } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = themeMode == ThemeMode.CUSTOM,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                    exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 }
                ) {
                    CustomThemeToggle(
                        customMonoAccent = customMonoAccent,
                        customDayNight = customDayNight,
                        isDark = isDark,
                        onCustomThemeChange = onCustomThemeChange
                    )
                }
            }

            ThemeModeSlider(
                currentTheme = currentTheme,
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

            Spacer(Modifier.weight(0.8f))

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
                val currentAccent = accentColor()
            BigButton(
                label = "BACK",
                onClick = onBack,
                primary = true,
                isDark = isDark,
                rippleColor = currentAccent
            )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

