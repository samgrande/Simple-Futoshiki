package com.hexcorp.futoshiki.ui.screens.theming

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.PixelF
import com.hexcorp.futoshiki.ui.theme.accentColor

@Composable
fun CustomThemeToggle(
    customMonoAccent: Boolean,
    customDayNight: Boolean,
    isDark: Boolean,
    onCustomThemeChange: (monoAccent: Boolean, dayNight: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = accentColor()
    val backgroundColor = if (isDark) {
        Color(0xFFD9D9D9).copy(alpha = 0.16f) // #29D9D9D9
    } else {
        Color(0xFF000000).copy(alpha = 0.08f)
    }
    
    val baseTextColor = if (isDark) Color.White else Color(0xFF111111)
    val unselectedDotColor = if (isDark) Color(0xFFD9D9D9).copy(alpha = 0.4f) else Color(0xFFBDBDBD)

    Box(
        modifier = modifier
            .width(280.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // Row 1: COLOR (MONO / ACCENT)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COLOR",
                    fontSize = 11.sp,
                    fontFamily = PixelF,
                    fontWeight = FontWeight.Normal,
                    color = baseTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.width(60.dp)
                )

                Spacer(Modifier.weight(1f))

                // MONO Option
                ToggleOption(
                    label = "MONO",
                    isSelected = !customMonoAccent,
                    selectedColor = accent,
                    unselectedColor = unselectedDotColor,
                    textColor = baseTextColor,
                    onClick = { onCustomThemeChange(false, customDayNight) },
                    modifier = Modifier.width(85.dp)
                )

                Spacer(Modifier.width(8.dp))

                // ACCENT Option
                ToggleOption(
                    label = "ACCENT",
                    isSelected = customMonoAccent,
                    selectedColor = accent,
                    unselectedColor = unselectedDotColor,
                    textColor = baseTextColor,
                    onClick = { onCustomThemeChange(true, customDayNight) },
                    modifier = Modifier.width(85.dp)
                )
            }

            // Separator Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(baseTextColor.copy(alpha = 0.1f))
            )

            // Row 2: MODE (DAY / NIGHT)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MODE",
                    fontSize = 11.sp,
                    fontFamily = PixelF,
                    fontWeight = FontWeight.Normal,
                    color = baseTextColor.copy(alpha = 0.7f),
                    modifier = Modifier.width(60.dp)
                )

                Spacer(Modifier.weight(1f))

                // DAY Option
                ToggleOption(
                    label = "DAY",
                    isSelected = !customDayNight,
                    selectedColor = accent,
                    unselectedColor = unselectedDotColor,
                    textColor = baseTextColor,
                    onClick = { onCustomThemeChange(customMonoAccent, false) },
                    modifier = Modifier.width(85.dp)
                )

                Spacer(Modifier.width(8.dp))

                // NIGHT Option
                ToggleOption(
                    label = "NIGHT",
                    isSelected = customDayNight,
                    selectedColor = accent,
                    unselectedColor = unselectedDotColor,
                    textColor = baseTextColor,
                    onClick = { onCustomThemeChange(customMonoAccent, true) },
                    modifier = Modifier.width(85.dp)
                )
            }
        }
    }
}

@Composable
private fun ToggleOption(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(400),
        label = "dotColor"
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.4f,
        animationSpec = tween(400),
        label = "textAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(dotColor, CircleShape)
        )
        
        Spacer(Modifier.width(10.dp))
        
        Text(
            text = label,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor.copy(alpha = textAlpha),
            modifier = Modifier.wrapContentWidth()
        )
    }
}
