package com.hexcorp.futoshiki.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.components.BigButton
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    var direction by remember { mutableIntStateOf(1) } // 1 for right, -1 for left

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

            // Animated Logo with Fade and Slide
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

            // Back Button
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

@Composable
fun ThemeModeSlider(
    currentTheme: AppTheme,
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val modes = ThemeMode.entries.toTypedArray()

    var width by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Dragging state
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
    // Snap animation
    val animatableOffset = remember { Animatable(currentMode.ordinal.toFloat()) }
    
    // Rotation animation when dragging
    val rotation by animateFloatAsState(
        targetValue = if (isDragging) 360f else 0f,
        animationSpec = if (isDragging) {
            infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart)
        } else {
            tween(300)
        },
        label = "shurikenRotation"
    )

    // Keep animatable in sync with currentMode when not dragging
    LaunchedEffect(currentMode) {
        if (!isDragging) {
            animatableOffset.animateTo(currentMode.ordinal.toFloat(), tween(300))
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            modes.forEach { mode ->
                val isSelected = if (isDragging) {
                    val sectionWidth = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 1f
                    (dragOffset / sectionWidth).roundToInt().coerceIn(0, modes.size - 1) == mode.ordinal
                } else {
                    mode == currentMode
                }

                Text(
                    text = mode.name,
                    fontSize = 9.sp,
                    fontFamily = ReemKufi,
                    fontWeight = FontWeight.Bold,
                    color = FutoshikiColors.onSurface().copy(alpha = if (isSelected) 1f else 0.4f),
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .width(60.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onModeChange(mode) },
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Slider Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .onGloballyPositioned { width = it.size.width }
                .pointerInput(modes.size) {
                    detectTapGestures { offset ->
                        val sectionWidth = width.toFloat() / (modes.size - 1)
                        val index = (offset.x / sectionWidth).roundToInt().coerceIn(0, modes.size - 1)
                        onModeChange(modes[index])
                    }
                }
                .pointerInput(modes.size) {
                    detectHorizontalDragGestures(
                        onDragStart = { 
                            isDragging = true 
                            val sectionWidth = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 1f
                            dragOffset = animatableOffset.value * sectionWidth
                        },
                        onDragEnd = {
                            isDragging = false
                            val sectionWidth = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 1f
                            val targetIndex = (dragOffset / sectionWidth).roundToInt().coerceIn(0, modes.size - 1)
                            onModeChange(modes[targetIndex])
                            scope.launch {
                                animatableOffset.animateTo(targetIndex.toFloat(), tween(200))
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch {
                                animatableOffset.animateTo(currentMode.ordinal.toFloat(), tween(200))
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        dragOffset = (dragOffset + dragAmount).coerceIn(0f, width.toFloat())
                        val sectionWidth = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 1f
                        scope.launch {
                            animatableOffset.snapTo(dragOffset / sectionWidth)
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Horizontal Line
            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                drawLine(
                    color = if (isDark) Color.White else Color.Gray,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Dots for each section
                val sectionWidth = size.width / (modes.size - 1)
                val accent = when (currentTheme) {
                    AppTheme.FIRE  -> FutoshikiColors.FireAccent
                    AppTheme.WATER -> FutoshikiColors.WaterAccent
                    AppTheme.EARTH -> FutoshikiColors.EarthAccent
                    AppTheme.WOOD  -> FutoshikiColors.WoodAccent
                }
                for (i in 0 until modes.size) {
                    drawCircle(
                        color = accent,
                        radius = 4.dp.toPx(),
                        center = Offset(i * sectionWidth, size.height / 2)
                    )
                }
            }

            // Shuriken Thumb
            val sectionWidthPx = if (modes.size > 1) width.toFloat() / (modes.size - 1) else 0f
            val thumbOffset = (animatableOffset.value * sectionWidthPx).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffset - with(density) { 20.dp.roundToPx() }, 0) }
                    .size(40.dp)
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = if (isDark) R.drawable.shuriken_dark else R.drawable.shuriken),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
