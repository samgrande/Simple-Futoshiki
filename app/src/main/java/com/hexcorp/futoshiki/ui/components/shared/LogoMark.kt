package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.theme.LocalIsDark

@Composable
fun LogoMark(size: Dp = 96.dp) {
    val isDark = LocalIsDark.current
    Image(
        painter = painterResource(id = if (isDark) R.drawable.futo_logo_dark else R.drawable.futo_logo),
        contentDescription = "Futoshiki Logo",
        modifier = Modifier.size(size)
    )
}
