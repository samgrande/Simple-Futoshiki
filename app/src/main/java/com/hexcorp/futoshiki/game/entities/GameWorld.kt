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
    private val manager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager,
    private val isSkyboxDark: Boolean = false,
    private val isAppDark: Boolean = false,
    private val skyColorHex: String = "#0b0b0b"
) : Container() {

    private lateinit var ninja: NinjaEntity
    private lateinit var dragon: DragonEntity
    private val layers = mutableListOf<ParallaxLayer>()

    lateinit var sitSprite: Bitmap
        private set
    lateinit var s2sitSprite: Bitmap
        private set
    lateinit var s2readSprite: Bitmap
        private set
    lateinit var readSprite: Bitmap
        private set
    private var floorY = 410.0
    private var targetNinjaScreenX = 500.0
    private var currentNinjaScreenX = 500.0
    private var cameraStiffness = 0.2
    private var freezeCamera = false

    private var currentAnimationJob: Job? = null
    private val animationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun setupWorld() {
        // 0. Setup Sky Background Color based on theme
        val skyColor = if (isSkyboxDark) {
            val base = Colors[skyColorHex]
            if (isAppDark) {
                RGBA(
                    (base.r * 0.7).toInt(),
                    (base.g * 0.7).toInt(),
                    (base.b * 0.7).toInt(),
                    base.a
                )
            } else {
                // Mix with white to make it a light pastel version for Day mode
                val ratio = 0.85
                RGBA(
                    (base.r * (1 - ratio) + 255 * ratio).toInt(),
                    (base.g * (1 - ratio) + 255 * ratio).toInt(),
                    (base.b * (1 - ratio) + 255 * ratio).toInt(),
                    base.a
                )
            }
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
            assets.loadImage("sprites/mountains back.webp"),
            0.1, - 0.0
        ).apply {
            scale = 1.0
            zIndex = 0.0
        })
        // Layer 2: Midground (Clouds/Trees) - Simplified to one image for now
        // Time drift speed of 15.0 gives a medium-slow noticeable movement
        layers.add(ParallaxLayer(
            assets.loadImage("sprites/cloud1.webp"),
            0.4, 250.0, 3000.0, timeDriftSpeed = 15.0
        ).apply {
            scale = 0.2
            zIndex = 1.0
        })

        layers.add(ParallaxLayer(
            assets.loadImage("sprites/cloud2.webp"),
            0.4, 300.0, 3000.0, 1500.0, timeDriftSpeed = 12.0
        ).apply {
            scale = 0.2
            zIndex = 1.0
        })

        layers.add(ParallaxLayer(
            assets.loadImage("sprites/gate.webp"),
            0.8, floorY, 8000.0, 1000.0
        ).apply {
            scale = 0.35
            zIndex = 1.0
            y = floorY + -65
        })

        layers.add(ParallaxLayer(
            assets.loadImage("sprites/statue.webp"),
            0.8, floorY, 10000.0, 2200.0
        ).apply {
            scale = 0.1
            zIndex = 1.0
            y = floorY + 60
        })

        layers.add(ParallaxLayer(
            assets.loadImage("sprites/Tree.webp"),
            0.8, floorY, 12000.0, 1500.0
        ).apply {
            scale = 0.5
            zIndex = 1.0
            y = floorY + -80
        })

        // Layer 1: Ground (Fastest)
        layers.add(ParallaxLayer(
            assets.loadImage("sprites/ground.webp"),
            0.8, floorY
        ).apply {
            zIndex = 2.0
            scale = 0.5
            y = floorY + 52
        })

        layers.forEach { addChild(it) }

        // 2. Setup Ninja
        ninja = NinjaEntity(
            assets.loadImage("sprites/stand.webp"),
            assets.loadImage("sprites/ninja.webp"),
            assets.loadImage("sprites/jump.webp"),
            400, 400,
            manager
        ).apply {
            x = 500.0
            y = floorY + 95.0
            zIndex = 20.0
            scale = 0.2
        }
        addChild(ninja)

        // 3. Setup Dragon
        val dragonSheet = assets.loadImage("sprites/dragon.webp")
        dragon = DragonEntity(dragonSheet, 400, 400, manager).apply {
            setTarget(ninja)
            zIndex = 10.0
            scale = 1.6
        }
        addChild(dragon)

        // 4. Load additional character sprites
        sitSprite = assets.loadImage("sprites/sit.webp")
        s2sitSprite = assets.loadImage("sprites/s2sit.webp")
        s2readSprite = assets.loadImage("sprites/s2read.webp")
        readSprite = assets.loadImage("sprites/read.webp")
    }

    fun updateTheme(isSkyboxDark: Boolean, isAppDark: Boolean, skyColorHex: String) {
        val skyColor = if (isSkyboxDark) {
            val base = Colors[skyColorHex]
            if (isAppDark) {
                RGBA(
                    (base.r * 0.7).toInt(),
                    (base.g * 0.7).toInt(),
                    (base.b * 0.7).toInt(),
                    base.a
                )
            } else {
                // Mix with white to make it a light pastel version for Day mode
                val ratio = 0.85
                RGBA(
                    (base.r * (1 - ratio) + 255 * ratio).toInt(),
                    (base.g * (1 - ratio) + 255 * ratio).toInt(),
                    (base.b * (1 - ratio) + 255 * ratio).toInt(),
                    base.a
                )
            }
        } else Colors["#f5f2f2"]
        
        children.firstOrNull { it is SolidRect }?.let { 
            (it as SolidRect).color = skyColor
        }
        
        // Note: Views.clearColor is harder to reach from here without the Views context,
        // but the SolidRect covers the background anyway.
    }

    fun startGame(skipIntro: Boolean = false) {
        freezeCamera = false
        cameraStiffness = 0.2
        currentAnimationJob?.cancel()
        if (skipIntro) {
            if (::ninja.isInitialized) ninja.skipIntro()
            if (::dragon.isInitialized) dragon.skipIntro(ninja)
        } else {
            // Run intros in parallel
            currentAnimationJob = animationScope.launch {
                if (::ninja.isInitialized) launch { ninja.runIntroSequence(manager) }
                if (::dragon.isInitialized) launch { dragon.runCinematicIntro(manager) }
            }
        }
    }

    fun startMenuIdle() {
        freezeCamera = false
        manager.updateNinjaScreenX(500f)
        cameraStiffness = 1.0
        targetNinjaScreenX = 500.0
        currentNinjaScreenX = 500.0
        if (::ninja.isInitialized) {
            ninja.visible = true
            ninja.x = 500.0
            ninja.y = floorY + 95.0
            ninja.triggerWin()
        }
        if (::dragon.isInitialized) {
            dragon.setIdle()
        }
    }

    fun restartGame() {
        freezeCamera = false
        currentAnimationJob?.cancel()
        cameraStiffness = 0.2
        targetNinjaScreenX = 500.0
        currentNinjaScreenX = 500.0
        if (::ninja.isInitialized) {
            ninja.x = 500.0
            ninja.resetForRestart()
        }
        if (::dragon.isInitialized) {
            dragon.resetForRestart()
        }
        // Run the full intro sequence
        currentAnimationJob = animationScope.launch {
            if (::ninja.isInitialized) launch { ninja.runIntroSequence(manager) }
            if (::dragon.isInitialized) launch { dragon.runCinematicIntro(manager) }
        }
    }

    fun runWinSequence(immediate: Boolean = false) {
        currentAnimationJob?.cancel()
        currentAnimationJob = animationScope.launch {
            if (::dragon.isInitialized) launch { dragon.runWinFlyAway() }
            
            cameraStiffness = if (immediate) 0.4 else 0.05
            targetNinjaScreenX = 500.0
            
            if (!immediate) {
                delay(2000)
            }

            freezeCamera = true
            
            if (::ninja.isInitialized) ninja.triggerWin()
        }
    }

    fun runDefeatSequence() {
        currentAnimationJob?.cancel()
        if (::ninja.isInitialized) {
            ninja.visible = false
        }
        if (::dragon.isInitialized) {
            dragon.visible = false
        }
    }

    fun runSolveSequence() {
        currentAnimationJob?.cancel()
        if (::dragon.isInitialized) {
            currentAnimationJob = animationScope.launch {
                dragon.runWinFlyAway()
            }
        }
        if (::ninja.isInitialized) {
            ninja.triggerWin() // Instantly go to stand
            // After stopping, switch to sit sprite after a short delay
            animationScope.launch {
                delay(500)
                if (::sitSprite.isInitialized) {
                    ninja.swapToSitSprite(sitSprite)
                }
            }
        }
        cameraStiffness = 1.0 // Snap to center
        targetNinjaScreenX = 500.0
        currentNinjaScreenX = 500.0
    }

    fun setSpeedMultiplier(multiplier: Float) {
        if (::ninja.isInitialized) {
            ninja.setSpeedMultiplier(multiplier)
        }
    }

    fun setDistanceMultiplier(multiplier: Float) {
        if (::dragon.isInitialized) {
            dragon.setDistanceMultiplier(multiplier)
        }
    }

    fun setNinjaScreenX(value: Float) {
        targetNinjaScreenX = value.toDouble()
        // No snapping — all transitions are smoothed in update()
    }

    fun swapNinjaToReadSprite() {
        if (::ninja.isInitialized && ::readSprite.isInitialized) {
            ninja.swapToReadSprite(readSprite)
        }
    }

    fun revertNinjaToStandSprite() {
        if (::ninja.isInitialized) {
            ninja.revertToStandSprite()
        }
    }

    fun swapNinjaToSitSprite() {
        if (::ninja.isInitialized && ::sitSprite.isInitialized) {
            ninja.swapToSitSprite(sitSprite)
        }
    }

    fun revertNinjaToRunSprite() {
        if (::ninja.isInitialized) {
            ninja.revertToRunSprite()
        }
    }

    fun update(dt: Double, aggression: Float, enableCloudDrift: Boolean = true) {
        if (!::ninja.isInitialized || !::dragon.isInitialized) return

        val actualCloudDrift = enableCloudDrift && !freezeCamera
        
        if (!freezeCamera) {
            // Smoothly move currentNinjaScreenX toward targetNinjaScreenX.
            // Large jumps (puzzle progress) use a very low stiffness so the ninja
            // drifts to the new position slowly instead of teleporting.
            val diff = targetNinjaScreenX - currentNinjaScreenX
            if (kotlin.math.abs(diff) > 0.1) {
                val effectiveStiffness = when {
                    kotlin.math.abs(diff) > 150 -> 0.015  // very very slow drift for big jumps
                    kotlin.math.abs(diff) > 50  -> 0.06   // gentle ease for medium shifts
                    else                        -> cameraStiffness  // normal follow
                }
                currentNinjaScreenX += diff * (1.0 - kotlin.math.exp(-effectiveStiffness * dt))
            } else {
                currentNinjaScreenX = targetNinjaScreenX
            }
        }
        
        // Use 60.0 offset to keep his feet at the grass line with the new larger 400x400 sprite
        ninja.update(dt, floorY + 95.0)
        dragon.update(dt, currentNinjaScreenX)
        dragon.updateAggression(aggression)

        // CAMERA FOLLOW: Keep the ninja at currentNinjaScreenX (virtualWidth = 1000)
        this.x = currentNinjaScreenX - ninja.x

        // Update Parallax based on Ninja's world position
        layers.forEach { it.update(ninja.x, dt, actualCloudDrift) }
        
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
