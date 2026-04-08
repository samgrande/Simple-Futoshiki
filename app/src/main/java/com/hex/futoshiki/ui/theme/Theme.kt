package com.hex.futoshiki.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.hex.futoshiki.R

// ── Brand colours ─────────────────────────────────────────────────────────────

object FutoshikiColors {
    val Background      = Color(0xFFF5F2F2)
    val Surface         = Color(0xFFF4F4F4)
    val OnSurface       = Color(0xFF111111)
    val Coral           = Color(0xFFFF404E)   // arrow + accent
    val CoralLight      = Color(0xFFF08080)   // wavy underline
    val CellDefault     = Color(0xFFF7F7F7)
    val CellSelected    = Color(0xFFFFFFFF)
    val CellRelated     = Color(0xFFEAEAEA)
    val ErrorBg         = Color(0xFFFFE5E5)
    val ErrorStroke     = Color(0xFFE24B4A)
    val TimerBg         = Color(0xFF111111)
    val TimerText       = Color(0xFFFFFFFF)
    val Overlay         = Color(0x8C000000)   // rgba(0,0,0,0.55)
    val TabThumb        = Color(0xFF111111)
    val TabText         = Color(0xFFFF404E)
    val ButtonPrimary   = Color(0xFFFF404E)   // coral pill buttons
    val ButtonSecondary = Color(0xFFFF404E)
    val LogoKanjiTile   = Color(0xFF2F353E)
    val LogoCellBg      = Color(0xFFF1F0F0)
}

// ── Typography (Reem Kufi loaded at runtime via downloadable fonts) ───────────
// Declare the FontFamily here; fonts/reem_kufi_*.ttf must be placed in res/font/
// OR use the Google Fonts XML provider (see res/font/reem_kufi.xml in project).

val ReemKufi = FontFamily(
    Font(R.font.reem_kufi_regular,   FontWeight.Normal),
    Font(R.font.reem_kufi_medium,    FontWeight.Medium),
    Font(R.font.reem_kufi_semibold,  FontWeight.SemiBold),
    Font(R.font.reem_kufi_bold,      FontWeight.Bold)
)

// ── Material theme wrapper ────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    background = FutoshikiColors.Background,
    surface    = FutoshikiColors.Surface,
    primary    = FutoshikiColors.Coral,
    onPrimary  = Color.White,
    onSurface  = FutoshikiColors.OnSurface,
)

@Composable
fun FutoshikiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content     = content
    )
}
