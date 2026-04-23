package com.hexcorp.futoshiki.game.entities

import korlibs.image.bitmap.*
import korlibs.korge.view.Image as KorgeImage
import korlibs.korge.view.*
import korlibs.math.geom.*

class ParallaxLayer(
    private val image: Bitmap,
    private val scrollSpeed: Double,
    initialY: Double = 0.0,
    private val spacing: Double = 0.0,
    private val offsetX: Double = 0.0
) : Container() {

    private val backgrounds = mutableListOf<View>()
    private val repeatSize = image.width.toDouble() + spacing

    init {
        y = initialY
        // Create multiple copies of the background to ensure seamless looping
        // Using -2..2 to ensure we cover the screen even with offsets or small assets
        for (i in -2..2) {
            val bg = KorgeImage(image)
            bg.x = i * repeatSize + offsetX
            addChild(bg)
            backgrounds.add(bg)
        }
    }

    fun update(cameraX: Double) {
        // The effective width of one background tile in world coordinates
        val r = repeatSize * scaleX
        if (r <= 0.0) return

        // To create parallax, the layer should ideally be at:
        // xIdeal = cameraX * (1.0 - scrollSpeed)
        // This ensures that: ScreenPos = GameWorld.x + xIdeal = (100 - cameraX) + xIdeal = 100 - cameraX * scrollSpeed
        val xIdeal = cameraX * (1.0 - scrollSpeed)

        // However, we must keep the layer's container near the cameraX so that its
        // tiled children (spanning -2r to 2r) are actually visible on screen.
        // We shift xIdeal by multiples of 'r' to 'snap' it to the camera's neighborhood.
        // Since the content repeats every 'r', this shift is visually invisible.
        val k = kotlin.math.floor((cameraX - xIdeal) / r)
        x = xIdeal + k * r
    }
}
