package com.hexcorp.futoshiki.ui.screens.theming

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.components.shared.BigButton
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.ThemeMode

@OptIn(ExperimentalAnimationApi::class)
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

            ThemeCarousel(
                currentIndex = currentIndex,
                direction = direction,
                onNavigate = { next -> navigate(next) },
                scope = scope
            )

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
