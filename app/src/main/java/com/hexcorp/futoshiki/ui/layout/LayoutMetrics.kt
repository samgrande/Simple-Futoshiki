package com.hexcorp.futoshiki.ui.layout

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared vertical layout contract for every full-screen surface in the app.
 *
 * ## The rule
 *
 * The app is edge-to-edge (mandatory from Android 15 / API 35). The KorGE world is rendered
 * once by `MainActivity` in a box anchored at `y = 0`, so its sky deliberately paints behind
 * the status bar. `MainActivity` also opts out of system bar contrast enforcement, so the
 * app background — not a platform scrim — is what shows through behind both bars.
 *
 * Every full-screen content container should therefore:
 *
 *  - apply `statusBarsPadding()` on top, so text never lands under the clock
 *  - apply [contentSafePadding] on the bottom, covering navigation bar **and** display cutout
 *  - budget vertical space as [LayoutMetrics.vh]
 *
 * ## Why `vh` only subtracts the bottom inset
 *
 * `vh` is a *scale reference*, not a height budget. Screens use it for proportional sizing
 * and for nudge offsets such as `-(vh * 0.05f)` on the landing title. Those constants were
 * tuned against `maxHeight - navigationBars`, so redefining `vh` silently reflows every
 * screen. Container height comes from `fillMaxHeight()` plus the padding above; the two are
 * intentionally separate concerns.
 *
 * Widgets drawn outside these containers — the timer pill and the toast, which sit in
 * `MainActivity`'s KorGE box — apply `statusBarsPadding()` individually.
 */
@Immutable
data class LayoutMetrics(
    /** Usable height: the full window minus the bottom system inset. */
    val vh: Dp,
    /** Compact-phone breakpoint; drives the tighter spacing variants. */
    val isSmallScreen: Boolean,
    /** Height reserved for the title/header row. */
    val headerH: Dp,
    /** Height reserved for the ninja sprite. */
    val ninjaH: Dp,
    /** Gap between the header and the ninja. */
    val korgeGap: Dp,
    /** Total height of the KorGE region — the offset content must skip to clear it. */
    val korgeHeight: Dp
)

/**
 * The insets a content container has to avoid: the navigation bar (or gesture pill) plus any
 * display cutout intruding from the bottom edge.
 *
 * Deliberately excludes the IME. `adjustResize` is set in the manifest, but there is no text
 * input anywhere in the app, and folding the IME in here would silently reshuffle the board.
 */
private val contentSafeInsets: WindowInsets
    @Composable get() = WindowInsets.systemBars
        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Bottom)

/**
 * Insets a container away from the bottom system UI while leaving the top edge alone, so the
 * KorGE sky keeps running edge-to-edge behind the status bar.
 *
 * This is the bottom-edge counterpart to [rememberLayoutMetrics]; the two must be used
 * together or the space budget stops matching the space that actually exists.
 */
@Composable
fun Modifier.contentSafePadding(): Modifier = windowInsetsPadding(contentSafeInsets)

/**
 * Derives the shared [LayoutMetrics] from a `BoxWithConstraints` max height.
 *
 * @param maxHeight the container's full height, i.e. `BoxWithConstraintsScope.maxHeight`.
 */
@Composable
fun rememberLayoutMetrics(maxHeight: Dp): LayoutMetrics {
    val bottomInset = contentSafeInsets.asPaddingValues().calculateBottomPadding()
    return remember(maxHeight, bottomInset) {
        val vh = maxHeight - bottomInset
        val isSmallScreen = vh < 800.dp
        val headerH = if (isSmallScreen) vh * 0.07f else vh * 0.09f
        val ninjaH = if (isSmallScreen) 100.dp else 135.dp
        val korgeGap = if (isSmallScreen) 14.dp else 16.dp
        LayoutMetrics(
            vh = vh,
            isSmallScreen = isSmallScreen,
            headerH = headerH,
            ninjaH = ninjaH,
            korgeGap = korgeGap,
            korgeHeight = headerH + korgeGap + ninjaH
        )
    }
}
