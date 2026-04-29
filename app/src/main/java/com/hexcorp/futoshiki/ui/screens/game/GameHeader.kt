package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.components.shared.DraggableSizeTabs
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.components.shared.TimerPill

@Composable
fun GameHeader(
    size: Int,
    timerSeconds: Int,
    won: Boolean,
    isPaused: Boolean,
    showTabs: Boolean,
    onTitleClick: () -> Unit,
    onTimerClick: () -> Unit,
    onSizeChange: (Int) -> Unit,
    animatedBg: Color,
    animatedBorder: Color,
    headerH: Dp,
    tabH: Dp,
    containerCoordinates: LayoutCoordinates?,
    onPillPositioned: (Offset, Offset) -> Unit,
    hideGameContent: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(animatedBg)
            .border(
                width = 1.dp,
                color = animatedBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .animateContentSize(animationSpec = tween(400))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerH),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FutoshikiTitle(
                    size = size,
                    fontSize = 28.sp,
                    isSolved = false,
                    showTabs = showTabs,
                    onClick = onTitleClick,
                    showUnderline = false
                )

                TimerPill(
                    seconds = timerSeconds,
                    won = won,
                    isPaused = isPaused,
                    onClick = onTimerClick,
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            containerCoordinates?.let { container ->
                                if (container.isAttached && coords.isAttached) {
                                    val localPos = container.localPositionOf(coords, Offset.Zero)
                                    val center = Offset(
                                        localPos.x + coords.size.width / 2f,
                                        localPos.y + coords.size.height / 2f
                                    )
                                    onPillPositioned(localPos, center)
                                }
                            }
                        }
                        .graphicsLayer { alpha = if (hideGameContent) 0f else 1f }
                )
            }

            AnimatedVisibility(
                visible = showTabs,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                Column {
                    Spacer(Modifier.height(tabH * 0.3f))
                    DraggableSizeTabs(
                        currentSize = size,
                        onSizeChange = onSizeChange,
                        height = tabH,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(tabH * 0.2f))
                }
            }
        }
    }
}
