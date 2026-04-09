package com.hexcorp.futoshiki.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.components.BigButton
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi

data class ThemeItem(
    val name: String,
    val iconResId: Int,
    val theme: AppTheme
)

val themes = listOf(
    ThemeItem("F I R E", R.drawable.fire, AppTheme.FIRE),
    ThemeItem("W A T E R", R.drawable.water, AppTheme.WATER),
    ThemeItem("E A R T H", R.drawable.earth, AppTheme.EARTH),
    ThemeItem("W O O D", R.drawable.wood, AppTheme.WOOD)
)

@Composable
fun ThemingScreen(
    currentTheme: AppTheme,
    isDark: Boolean,
    onToggleDark: () -> Unit,
    onApply: (AppTheme) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var currentIndex by remember { 
        mutableIntStateOf(themes.indexOfFirst { it.theme == currentTheme }.coerceAtLeast(0)) 
    }
    var direction by remember { mutableIntStateOf(1) } // 1 for right, -1 for left

    fun navigate(next: Boolean) {
        direction = if (next) 1 else -1
        if (next) {
            currentIndex = (currentIndex + 1) % themes.size
        } else {
            currentIndex = (currentIndex - 1 + themes.size) % themes.size
        }
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
                        if (accumulatedDrag > threshold) { // Swipe Right (Previous)
                            navigate(false)
                            hasTriggered = true
                        } else if (accumulatedDrag < -threshold) { // Swipe Left (Next)
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
                modifier = Modifier.padding(top = 32.dp)
            )

            Spacer(Modifier.weight(1f))

            // Animated Logo with Fade and Slide
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
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
                    Box(modifier = Modifier.size(200.dp)) {
                        // Hard drop shadow: 4x4 offset, 40% opacity black, no blur
                        Image(
                            painter = painterResource(id = themes[index].iconResId),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                if (isDark) Color.White.copy(alpha = 0.15f)
                                else Color.Black.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .size(200.dp)
                                .offset(x = 4.dp, y = 4.dp)
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

            // Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Arrow
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

                // Theme Name with AnimatedContent for smooth text transition
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

                // Right Arrow
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

            Spacer(Modifier.weight(1.3f))

            // Dark/Light Mode Pill Button
            val pillBgColor by animateColorAsState(
                targetValue = if (isDark) Color(0xFFDCDCDC) else Color(0xFF111111),
                animationSpec = tween(400),
                label = "pillBgColor"
            )
            val pillTextColor by animateColorAsState(
                targetValue = if (isDark) Color(0xFF111111) else Color(0xFFF5F2F2),
                animationSpec = tween(400),
                label = "pillTextColor"
            )

            Row(
                modifier = Modifier
                    .width(90.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(pillBgColor)
                    .clickable { onToggleDark() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = pillTextColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isDark) "D A R K" else "L I G H T",
                    color = pillTextColor,
                    fontSize = 9.sp,
                    fontFamily = ReemKufi,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(56.dp))

            // Apply Button
            BigButton(
                label = "APPLY",
                onClick = { onApply(themes[currentIndex].theme) },
                primary = true,
                isDark = isDark
            )
            
            Spacer(Modifier.height(48.dp))
        }
    }
}
