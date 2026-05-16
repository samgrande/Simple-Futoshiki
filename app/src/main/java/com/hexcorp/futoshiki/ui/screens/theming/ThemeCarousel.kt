package com.hexcorp.futoshiki.ui.screens.theming

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.Yuji
import com.hexcorp.futoshiki.ui.theme.accentColor

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThemeCarousel(
    currentIndex: Int,
    direction: Int,
    onNavigate: (Boolean) -> Unit,
    useAccentColor: Boolean = false,
    modifier: Modifier = Modifier,
    scope: AnimatedVisibilityScope? = null
) {
    val swipeModifier = Modifier.pointerInput(Unit) {
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
                    onNavigate(false)
                    hasTriggered = true
                } else if (accumulatedDrag < -threshold) {
                    onNavigate(true)
                    hasTriggered = true
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .then(swipeModifier)
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
                    // Japanese kanji logo using Yuji font
                    val kanji = when (themes[index].theme) {
                        com.hexcorp.futoshiki.ui.theme.AppTheme.FIRE -> "火"
                        com.hexcorp.futoshiki.ui.theme.AppTheme.WATER -> "水"
                        com.hexcorp.futoshiki.ui.theme.AppTheme.EARTH -> "土"
                        else -> "砂" // sand (wood theme)
                    }
                    Text(
                        text = kanji,
                        fontSize = 160.sp,
                        fontFamily = Yuji,
                        color = if (useAccentColor) accentColor() else FutoshikiColors.onSurface(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.size(200.dp),
                        style = androidx.compose.ui.text.TextStyle(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
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
                .then(swipeModifier)
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
                    ) { onNavigate(false) },
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
                        fontFamily = PixelF,
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
                    ) { onNavigate(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    fontSize = 12.sp,
                    color = FutoshikiColors.onSurface()
                )
            }
        }
    }
}
