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

import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState

@Composable
fun GameHeader(
    size: Int,
    timerSeconds: Int,
    won: Boolean,
    isPaused: Boolean,
    showCountdown: Boolean,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit = {},
    onTimerClick: () -> Unit,
    onSizeChange: (Int) -> Unit = {},
    headerH: Dp,
    hideGameContent: Boolean,
    isSmallScreen: Boolean = false,
    isExpanded: Boolean = false
) {
    val headerAlpha by animateFloatAsState(
        targetValue = if (isExpanded && won) 0f else 1f,
        animationSpec = tween(400),
        label = "headerAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = headerAlpha }
            .padding(horizontal = 20.dp, vertical = if (isSmallScreen) 16.dp else 20.dp)
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
                showUnderline = false,
                isSmallScreen = isSmallScreen
            )

        }
    }
}
