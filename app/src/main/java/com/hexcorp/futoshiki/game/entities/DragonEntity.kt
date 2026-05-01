package com.hexcorp.futoshiki.game.entities

import korlibs.image.bitmap.*
import korlibs.korge.view.Image as KorgeImage
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.time.*
import kotlinx.coroutines.*
import kotlin.math.*

class DragonEntity(
    private val spriteSheet: Bitmap,
    private val frameWidth: Int,
    private val frameHeight: Int
) : Container() {

    // --- TUNED FOR TIGHTER CHASE ---
    private var stiffness: Float = 18.0f
    private var damping: Float = 10.0f
    private val maxEntranceSpeed = 800.0f

    // --- MOVEMENT ---
    private var timePassed = 0.0f
    private var pulseSpeed = 0.5f
    private var hoverBreadth = 150.0f
    private var verticalSwing = 80.0f

    private var target: View? = null
    private var velocityX = 0.0f
    private var velocityY = 0.0f
    private var isChasing = false
    private var chaseTime = 0.0f
    private var currentAggression = 0.0f
    private var distanceMultiplier = 1.0f

    private lateinit var sprite: KorgeImage
    private var currentFrame = 0
    private var animationTimer = 0.0f
    private val frameDuration = 0.1f

    init {
        // Initialize the sprite component
        val firstFrame = spriteSheet.slice(RectangleInt(0, 0, frameWidth, frameHeight))
        sprite = KorgeImage(firstFrame)
        sprite.anchor(Anchor.CENTER)
        addChild(sprite)
        visible = false
    }

    fun setTarget(ninja: View) {
        target = ninja
    }

    suspend fun runCinematicIntro() {
        delay(200)
        val ninja = target ?: return

        // PHASE 1: TEASER (Right -> Left)
        x = ninja.x + 1000.0f
        y = ninja.y - 560.0f
        sprite.scaleX = -1.0
        sprite.scaleY = 1.0
        visible = true

        // Simple tween from Right to Left
        val duration = 3.0f
        val startX = x
        val endX = ninja.x - 1000.0f

        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < duration * 1000) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / (duration * 1000)
            x = startX + (endX - startX) * progress
            delay(16)
        }

        visible = false
        delay(500)

        // PHASE 2: START CHASE
        x = ninja.x - 400.0f
        y = ninja.y - 500.0f
        sprite.scaleX = 1.0
        sprite.scaleY = 1.0
        visible = true
        isChasing = true
        // Set initial velocity to match Ninja so it doesn't fall behind immediately
        velocityX = 250.0f
    }

    fun skipIntro(ninja: NinjaEntity) {
        x = ninja.x - 400.0f
        y = ninja.y - 500.0f
        sprite.scaleX = 1.0
        sprite.scaleY = 1.0
        visible = true
        isChasing = true
        velocityX = 250.0f
    }

    fun update(dt: Double, ninjaScreenX: Double) {
        val ninja = target ?: return

        timePassed += dt.toFloat()

        // 1. Flip & Animate
        if (isChasing) {
            sprite.scaleX = if (ninja.x < x) -1.0 else 1.0
        }

        // 2. THE TIGHT CHASE MATH
        if (!isChasing) {
            updateAnimation(dt.toFloat())
            return
        }

        chaseTime += dt.toFloat()

        // STATIONARY ON SCREEN LOGIC
        // Keep the dragon at a fixed screen position (e.g., 100px from the left edge)
        // This means it won't move forward when the ninja moves forward.
        val targetScreenX = 100.0
        val actualMargin = ninjaScreenX - targetScreenX
        
        // Keep the vertical hovering motion - Increased offset to 300.0 to prevent ground clipping
        val dynamicFloat = 220.0f + (cos(timePassed * pulseSpeed * 0.5f) * verticalSwing)
 
        // 3. Target Position
        val targetX = ninja.x - actualMargin
        val targetY = ninja.y - dynamicFloat.toDouble()
 
        // 4. Update Position directly
        x = targetX
        y = targetY

        updateAnimation(dt.toFloat())
    }

    private fun updateAnimation(dt: Float) {
        animationTimer += dt
        if (animationTimer >= frameDuration) {
            animationTimer = 0.0f
            currentFrame = (currentFrame + 1) % 10 // Assuming 10 frames for the dragon

            val newSubImage = spriteSheet.slice(RectangleInt(
                currentFrame * frameWidth,
                0, // Assuming 1 row for dragon
                frameWidth,
                frameHeight
            ))

            sprite.bitmap = newSubImage
        }
    }

    fun updateAggression(value: Float) {
        currentAggression = value
        pulseSpeed = 2.0f + (value * 8.0f)
        stiffness = 5.0f + (value * 15.0f)
    }

    fun setDistanceMultiplier(multiplier: Float) {
        distanceMultiplier = multiplier
    }
}
