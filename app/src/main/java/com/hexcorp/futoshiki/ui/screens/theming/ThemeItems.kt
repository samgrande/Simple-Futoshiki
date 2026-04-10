package com.hexcorp.futoshiki.ui.screens.theming

import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.theme.AppTheme

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
