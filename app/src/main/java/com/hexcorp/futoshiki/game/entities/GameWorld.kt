package com.hexcorp.futoshiki.game.entities

import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import kotlinx.coroutines.*

class GameWorld(
    private val assets: AssetManager,
    private val isDark: Boolean = false,
    private val skyColorHex: String = "#0b0b0b"
) : Container() {

    private lateinit var ninja: NinjaEntity
    private lateinit var dragon: DragonEntity
    private val layers = mutableListOf<ParallaxLayer>()
    private var floorY = 410.0

    suspend fun setupWorld() {
        // 0. Setup Sky Background Color based on theme
        val skyColor = if (isDark) {
            val base = Colors[skyColorHex]
            RGBA(
                (base.r * 0.7).toInt(),
                (base.g * 0.7).toInt(),
                (base.b * 0.7).toInt(),
                base.a
            )
        } else Colors["#f5f2f2"]
        val bg = SolidRect(20000, 1000, skyColor).apply {
            anchor(0.5, 0.5)
            x = 0.0
            y = 0.0
            zIndex = -10.0
        }
        addChild(bg)

        // 1. Setup Parallax Layers
        // Layer 3: Mountains (Slowest)
        layers.add(ParallaxLayer(
            assets.loadImage("sprites/mountains back.png"),
            0.1, - 0.0
        ).apply {
            scale = 1.0
            zIndex = 0.0
        })
        // Layer 2: Midground (Clouds/Trees) - Simplified to one image for now
        layers.add(ParallaxLayer(
            assets.loadImage("sprites/cloud1.png"),
            0.4, 250.0, 3000.0
        ).apply {
            scale = 0.2
            zIndex = 1.0
        })

        layers.add(ParallaxLayer(
            assets.loadImage("sprites/cloud2.png"),
            0.4, 300.0, 3000.0, 1500.0
        ).apply {
            scale = 0.2
            zIndex = 1.0
        })

        layers.add(ParallaxLayer(
            assets.loadImage("sprites/gate.png"),
            0.8, floorY, 8000.0, 1000.0
        ).apply {
            scale = 0.35
            zIndex = 1.0
            y = floorY + -65
        })

        layers.add(ParallaxLayer(
            assets.loadImage("sprites/statue.png"),
            0.8, floorY, 10000.0, 2200.0
        ).apply {
            scale = 0.1
            zIndex = 1.0
            y = floorY + 60
        })

        layers.add(ParallaxLayer(
            assets.loadImage("sprites/Tree.png"),
            0.8, floorY, 12000.0, 1500.0
        ).apply {
            scale = 0.5
            zIndex = 1.0
            y = floorY + -80
        })

        // Layer 1: Ground (Fastest)
        layers.add(ParallaxLayer(
            assets.loadImage("sprites/ground.png"),
            0.8, floorY
        ).apply {
            zIndex = 2.0
            scale = 0.5
            y = floorY + 52
        })

        layers.forEach { addChild(it) }

        // 2. Setup Ninja
        ninja = NinjaEntity(
            assets.loadImage("sprites/stand.png"),
            assets.loadImage("sprites/ninja.png"),
            assets.loadImage("sprites/jump.png"),
            400, 400
        ).apply {
            x = 100.0
            y = floorY + 95.0
            zIndex = 20.0
            scale = 0.2
        }
        addChild(ninja)

        // 3. Setup Dragon
        val dragonSheet = assets.loadImage("sprites/dragon.png")
        dragon = DragonEntity(dragonSheet, 400, 400).apply {
            setTarget(ninja)
            zIndex = 10.0
            scale = 1.2
        }
        addChild(dragon)
        

    }

    fun startGame() {
        // Run intros in parallel
        GlobalScope.launch {
            if (::ninja.isInitialized) launch { ninja.runIntroSequence() }
            if (::dragon.isInitialized) launch { dragon.runCinematicIntro() }
        }
    }

    fun update(dt: Double, aggression: Float) {
        if (!::ninja.isInitialized || !::dragon.isInitialized) return
        
        // Use 60.0 offset to keep his feet at the grass line with the new larger 400x400 sprite
        ninja.update(dt, floorY + 95.0)
        dragon.update(dt)
        dragon.updateAggression(aggression)

        // CAMERA FOLLOW: Keep the ninja at center of screen (virtualWidth = 1000, so center = 500)
        this.x = 500.0 - ninja.x

        // Update Parallax based on Ninja's world position
        layers.forEach { it.update(ninja.x) }
        
        // Keep sky background roughly centered on ninja
        children.firstOrNull { it is SolidRect }?.let { 
            it.x = ninja.x 
        }
    }
}

class AssetManager {
    suspend fun loadImage(path: String): Bitmap {
        return try {
            resourcesVfs[path].readBitmap().toBMP32()
        } catch (e: Exception) {
            Bitmap32(128, 128, Colors.MAGENTA)
        }
    }
}
