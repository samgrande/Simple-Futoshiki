package com.hexcorp.futoshiki.ui.theme

/**
 * Canonical definition of the KorGE skybox colour.
 *
 * This is the surface that sits *behind the status bar*, because the game world is drawn
 * edge-to-edge. Two very different consumers need to agree on it, exactly:
 *
 *  - [com.hexcorp.futoshiki.game.entities.GameWorld] paints the sky rect with it.
 *  - [FutoshikiTheme] uses its luminance to decide whether the status bar icons must be
 *    drawn dark or light.
 *
 * Keeping the maths here means the two can never drift apart. They previously did, which is
 * how dark mode ended up with white status bar icons on a near-white `#f5f2f2` sky.
 *
 * Everything is plain ARGB [Int] so this file stays free of both Compose and KorGE types.
 */
object SkyColor {

    /** The default sky: a warm off-white. Used whenever the skybox is not accent-tinted. */
    const val NEUTRAL: Int = 0xFFF5F2F2.toInt()

    /** How far the accent-tinted day sky is mixed toward white. */
    private const val DAY_WHITE_MIX = 0.85

    /** How much the accent-tinted night sky is darkened. */
    private const val NIGHT_DIM = 0.7

    /**
     * Resolves the sky colour.
     *
     * @param isSkyboxTinted when false the sky is always [NEUTRAL], regardless of dark mode.
     *   This is bound to the user's "mono accent" preference and defaults to false.
     * @param isAppDark whether the app is currently in dark mode.
     * @param accentArgb the active theme accent, used as the tint base.
     */
    fun argb(isSkyboxTinted: Boolean, isAppDark: Boolean, accentArgb: Int): Int {
        if (!isSkyboxTinted) return NEUTRAL

        val a = (accentArgb ushr 24) and 0xFF
        val r = (accentArgb ushr 16) and 0xFF
        val g = (accentArgb ushr 8) and 0xFF
        val b = accentArgb and 0xFF

        return if (isAppDark) {
            pack(a, dim(r), dim(g), dim(b))
        } else {
            pack(a, toDay(r), toDay(g), toDay(b))
        }
    }

    private fun dim(channel: Int): Int = (channel * NIGHT_DIM).toInt()

    private fun toDay(channel: Int): Int =
        (channel * (1 - DAY_WHITE_MIX) + 255 * DAY_WHITE_MIX).toInt()

    // Alpha is passed through untouched (not dimmed/mixed toward white like the RGB
    // channels) since every accent in FutoshikiColors is opaque today; this just keeps
    // a translucent accent from silently becoming opaque if one is ever added.
    private fun pack(a: Int, r: Int, g: Int, b: Int): Int =
        (a.coerceIn(0, 255) shl 24) or (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)
}
