package com.hexcorp.futoshiki.ui.theme

import android.app.Activity
import android.view.ContextThemeWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.hexcorp.futoshiki.R
// ── Theme Enum ──────────────────────────────────────────────────────────────

enum class AppTheme {
    FIRE, WATER, EARTH, WOOD
}

enum class ThemeMode {
    AUTO, DAY, NIGHT, CUSTOM
}

// ── Brand colours ─────────────────────────────────────────────────────────────

object FutoshikiColors {
    val Background      = Color(0xFFF5F2F2)
    val Surface         = Color(0xFFF4F4F4)
    val OnSurface       = Color(0xFF111111)
    
    val BackgroundDark  = Color(0xFF0B0B0B)
    val SurfaceDark     = Color(0xFF161616)
    val OnSurfaceDark   = Color(0xFFF5F2F2)
    
    // Accent colors based on theme
    val FireAccent      = Color(0xFFFF404E)
    val WaterAccent     = Color(0xFF0088FF)
    val EarthAccent     = Color(0xFF34C759)
    val WoodAccent      = Color(0xFFFF8D28)

    val CellDefault     = Color(0xFFDCDCDC)
    val CellDefaultDark = Color(0xFF222222)
    val CellSelected    = Color(0xFFFFFFFF)
    val CellSelectedDark = Color(0xFF333333)
    val CellRelated     = Color(0xFFC0C0C0)
    val CellRelatedDark = Color(0xFF1A1A1A)
    val ErrorBg         = Color(0xFFFFE5E5)
    val ErrorBgDark     = Color(0xFF3D1D1D)
    val ErrorStroke     = Color(0xFFE24B4A)
    val TimerBg         = Color(0xFF111111)
    val TimerBgDark     = Color(0xFFF5F2F2)
    val TimerText       = Color(0xFFFFFFFF)
    val TimerTextDark   = Color(0xFF111111)
    val Overlay         = Color(0x8C000000)
    val TabThumb        = Color(0xFF111111)
    val TabThumbDark    = Color(0xFFF5F2F2)
    val TabText         = Color(0xFF111111)
    val TabTextDark     = Color(0xFFF5F2F2)
    val BigButtonPrimary = Color(0xFF111111)
    val BigButtonPrimaryDark = Color(0xFFFFFFFF)
    val BigButtonSecondary = Color(0xFFE0E0E0)
    val BigButtonSecondaryDark = Color(0xFF0A0A0A)
    val BigButtonTextPrimary = Color(0xFFFFFFFF)
    val BigButtonTextPrimaryDark = Color(0xFF111111)
    val BigButtonTextSecondary = Color(0xFF111111)
    val BigButtonTextSecondaryDark = Color(0xFFFFFFFF)
    val LogoKanjiTile   = Color(0xFF2F353E)
    val LogoCellBg      = Color(0xFFF1F0F0)
    val LogoCellBgDark  = Color(0xFF1A1A1A)

    val NumberPadBg      = Color(0xFFC8C8C8) // Darker than CellDefault (DCDCDC)
    val NumberPadBgDark  = Color(0xFF2A2A2A) // Lighter than CellDefaultDark (222222)
    
    @Composable
    fun background(): Color = if (LocalIsDark.current) BackgroundDark else Background

    @Composable
    fun surface(): Color = if (LocalIsDark.current) SurfaceDark else Surface

    @Composable
    fun onSurface(): Color = if (LocalIsDark.current) OnSurfaceDark else OnSurface

    @Composable
    fun cellDefault(): Color = if (LocalIsDark.current) CellDefaultDark else CellDefault
    
    @Composable
    fun numberPadBg(): Color = if (LocalIsDark.current) NumberPadBgDark else NumberPadBg

    @Composable
    fun cellRelated(): Color = if (LocalIsDark.current) CellRelatedDark else CellRelated

    @Composable
    fun errorBg(): Color = if (LocalIsDark.current) ErrorBgDark else ErrorBg

    @Composable
    fun timerBg(): Color = if (LocalIsDark.current) TimerBgDark else TimerBg

    @Composable
    fun timerText(): Color = if (LocalIsDark.current) TimerTextDark else TimerText

    @Composable
    fun tabThumb(): Color = if (LocalIsDark.current) TabThumbDark else TabThumb

    @Composable
    fun tabText(): Color = if (LocalIsDark.current) TabTextDark else TabText

    @Composable
    fun logoCellBg(): Color = if (LocalIsDark.current) LogoCellBgDark else LogoCellBg

    @Composable
    fun shadowColor(): Color = if (LocalIsDark.current) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.3f)

}

/** Accent for a given theme. Non-composable so callers outside the theme scope can use it too. */
fun accentFor(theme: AppTheme): Color = when (theme) {
    AppTheme.FIRE  -> FutoshikiColors.FireAccent
    AppTheme.WATER -> FutoshikiColors.WaterAccent
    AppTheme.EARTH -> FutoshikiColors.EarthAccent
    AppTheme.WOOD  -> FutoshikiColors.WoodAccent
}

// Dynamic accent provider
@Composable
fun accentColor(): Color = accentFor(LocalAppTheme.current)

val LocalAppTheme = staticCompositionLocalOf { AppTheme.FIRE }
val LocalIsDark = staticCompositionLocalOf { false }

// ── Typography ───────────────────────────────────────────────────────────────

val Midorima = FontFamily(
    Font(R.font.midorima, FontWeight.Normal)
)

val PixelF = FontFamily(
    Font(R.font.pixel_f, FontWeight.Normal)
)

val Yuji = FontFamily(
    Font(R.font.yuji, FontWeight.Normal)
)

// ── Material theme wrapper ────────────────────────────────────────────────────

@Composable
fun FutoshikiTheme(
    theme: AppTheme = AppTheme.FIRE,
    isDark: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeResId = when (theme) {
        AppTheme.FIRE  -> R.style.Theme_Futoshiki_Fire
        AppTheme.WATER -> R.style.Theme_Futoshiki_Water
        AppTheme.EARTH -> R.style.Theme_Futoshiki_Earth
        AppTheme.WOOD  -> R.style.Theme_Futoshiki_Wood
    }

    val themedContext = remember(theme, isDark) {
        val config = android.content.res.Configuration(context.resources.configuration)
        config.uiMode = if (isDark) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        } else {
            android.content.res.Configuration.UI_MODE_NIGHT_NO
        } or (config.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK.inv())
        
        ContextThemeWrapper(context, themeResId).apply {
            applyOverrideConfiguration(config)
        }
    }

    val accent = accentFor(theme)

    val colorScheme = if (isDark) {
        darkColorScheme(
            background = Color.Transparent,
            surface = FutoshikiColors.SurfaceDark,
            primary = accent,
            onPrimary = Color.White,
            onSurface = FutoshikiColors.OnSurfaceDark,
        )
    } else {
        lightColorScheme(
            background = Color.Transparent,
            surface = FutoshikiColors.Surface,
            primary = accent,
            onPrimary = Color.White,
            onSurface = FutoshikiColors.OnSurface,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalAppTheme provides theme,
        LocalIsDark provides isDark,
        LocalContext provides themedContext
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content     = content
        )
    }
}

