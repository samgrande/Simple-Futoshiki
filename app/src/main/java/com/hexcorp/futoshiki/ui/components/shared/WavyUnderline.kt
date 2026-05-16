package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.hexcorp.futoshiki.ui.components.shared.wavy.EarthUnderline
import com.hexcorp.futoshiki.ui.components.shared.wavy.FireUnderline
import com.hexcorp.futoshiki.ui.components.shared.wavy.WaterUnderline
import com.hexcorp.futoshiki.ui.components.shared.wavy.WoodUnderline
import com.hexcorp.futoshiki.ui.theme.AppTheme
import com.hexcorp.futoshiki.ui.theme.LocalAppTheme
import com.hexcorp.futoshiki.ui.theme.accentColor

@Composable
fun WavyUnderline(width: Dp, height: Dp, modifier: Modifier = Modifier) {
    val theme = LocalAppTheme.current
    val accent = accentColor()
    when (theme) {
        AppTheme.FIRE  -> FireUnderline(width, height, modifier, accent)
        AppTheme.WATER -> WaterUnderline(width, height, modifier, accent)
        AppTheme.WOOD  -> WoodUnderline(width, height, modifier, accent)
        AppTheme.EARTH -> EarthUnderline(width, height, modifier, accent)
    }
}
