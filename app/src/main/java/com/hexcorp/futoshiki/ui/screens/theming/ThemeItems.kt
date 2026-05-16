package com.hexcorp.futoshiki.ui.screens.theming

import com.hexcorp.futoshiki.ui.theme.AppTheme

data class ThemeItem(
    val name: String,
    val theme: AppTheme
)

val themes = listOf(
    ThemeItem("F I R E", AppTheme.FIRE),
    ThemeItem("W A T E R", AppTheme.WATER),
    ThemeItem("E A R T H", AppTheme.EARTH),
    ThemeItem("S A N D", AppTheme.WOOD)
)
