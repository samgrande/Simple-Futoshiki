package com.hexcorp.futoshiki.ui.theme

import android.view.ContextThemeWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.hexcorp.futoshiki.R

// ── Theme Enum ──────────────────────────────────────────────────────────────

enum class AppTheme {
    FIRE, WATER, EARTH, WOOD
}

// ── Brand colours ─────────────────────────────────────────────────────────────

object FutoshikiColors {
    val Background      = Color(0xFFF5F2F2)
    val Surface         = Color(0xFFF4F4F4)
    val OnSurface       = Color(0xFF111111)
    
    // Accent colors based on theme
    val FireAccent      = Color(0xFFFF404E)
    val WaterAccent     = Color(0xFF0088FF)
    val EarthAccent     = Color(0xFF34C759)
    val WoodAccent      = Color(0xFFFF8D28)

    val CellDefault     = Color(0xFFF7F7F7)
    val CellSelected    = Color(0xFFFFFFFF)
    val CellRelated     = Color(0xFFEAEAEA)
    val ErrorBg         = Color(0xFFFFE5E5)
    val ErrorStroke     = Color(0xFFE24B4A)
    val TimerBg         = Color(0xFF111111)
    val TimerText       = Color(0xFFFFFFFF)
    val Overlay         = Color(0x8C000000)
    val TabThumb        = Color(0xFF111111)
    val TabText         = Color(0xFF111111)
    val LogoKanjiTile   = Color(0xFF2F353E)
    val LogoCellBg      = Color(0xFFF1F0F0)
}

// Dynamic accent provider
@Composable
fun accentColor(): Color = when (LocalAppTheme.current) {
    AppTheme.FIRE  -> FutoshikiColors.FireAccent
    AppTheme.WATER -> FutoshikiColors.WaterAccent
    AppTheme.EARTH -> FutoshikiColors.EarthAccent
    AppTheme.WOOD  -> FutoshikiColors.WoodAccent
}

val LocalAppTheme = staticCompositionLocalOf { AppTheme.FIRE }

// ── Typography ───────────────────────────────────────────────────────────────

val ReemKufi = FontFamily(
    Font(R.font.reem_kufi_regular,   FontWeight.Normal),
    Font(R.font.reem_kufi_medium,    FontWeight.Medium),
    Font(R.font.reem_kufi_semibold,  FontWeight.SemiBold),
    Font(R.font.reem_kufi_bold,      FontWeight.Bold)
)

// ── Material theme wrapper ────────────────────────────────────────────────────

@Composable
fun FutoshikiTheme(
    theme: AppTheme = AppTheme.FIRE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeResId = when (theme) {
        AppTheme.FIRE  -> R.style.Theme_Futoshiki_Fire
        AppTheme.WATER -> R.style.Theme_Futoshiki_Water
        AppTheme.EARTH -> R.style.Theme_Futoshiki_Earth
        AppTheme.WOOD  -> R.style.Theme_Futoshiki_Wood
    }

    val themedContext = remember(theme) {
        ContextThemeWrapper(context, themeResId)
    }

    val accent = when (theme) {
        AppTheme.FIRE  -> FutoshikiColors.FireAccent
        AppTheme.WATER -> FutoshikiColors.WaterAccent
        AppTheme.EARTH -> FutoshikiColors.EarthAccent
        AppTheme.WOOD  -> FutoshikiColors.WoodAccent
    }

    val colorScheme = lightColorScheme(
        background = FutoshikiColors.Background,
        surface    = FutoshikiColors.Surface,
        primary    = accent,
        onPrimary  = Color.White,
        onSurface  = FutoshikiColors.OnSurface,
    )

    CompositionLocalProvider(
        LocalAppTheme provides theme,
        LocalContext provides themedContext
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content     = content
        )
    }
}
