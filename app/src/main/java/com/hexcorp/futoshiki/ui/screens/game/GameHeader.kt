package com.hexcorp.futoshiki.ui.screens.game

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import com.hexcorp.futoshiki.ui.components.shared.FutoshikiTitle
import com.hexcorp.futoshiki.ui.components.shared.TimerPill

@Composable
fun GameHeader(
    size: Int,
    timerSeconds: Int,
    won: Boolean,
    isPaused: Boolean,
    showCountdown: Boolean,
    showTabs: Boolean = false,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit = {},
    onTimerClick: () -> Unit,
    onTimerLongClick: () -> Unit,
    onSizeChange: (Int) -> Unit = {},
    animatedBg: Color,
    animatedBorder: Color,
    headerH: Dp,
    tabH: Dp = 0.dp,
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
                onClick = if (showCountdown) null else onTitleClick,
                onLongClick = if (showCountdown) null else onTitleLongClick,
                showUnderline = false
            )

            TimerPill(
                seconds = timerSeconds,
                won = won,
                isPaused = isPaused,
                enabled = !showCountdown,
                onClick = onTimerClick,
                onLongClick = onTimerLongClick,
                label = if (won) "HOME" else null,
                icon = null,
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
    }
}
