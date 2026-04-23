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
        y = ninja.y - 460.0f
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
        y = ninja.y - 400.0f
        sprite.scaleX = 1.0
        sprite.scaleY = 1.0
        visible = true
        isChasing = true
        // Set initial velocity to match Ninja so it doesn't fall behind immediately
        velocityX = 250.0f
    }

    fun update(dt: Double) {
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

        // GRADUAL CLOSING LOGIC
        // Starts far and approaches the ninja over time
        val startMargin = 600.0f
        val minMargin = 200.0f // Increased minimum gap to prevent clipping
        val closingSpeed = 0.2f + (currentAggression * 0.3f)
        val progress = (1.0 - exp(-chaseTime * closingSpeed)).toFloat() // 0.0 to 1.0
        
        val baseMargin = startMargin + (minMargin - startMargin) * progress
        
        val wave = sin(timePassed * pulseSpeed)
        // Reduce hover breadth as it gets closer for a more "focused" catch
        val dynamicMargin = baseMargin + (wave * hoverBreadth * (1.0f - progress * 0.8f))
        val dynamicFloat = 160.0f + (cos(timePassed * pulseSpeed * 0.5f) * verticalSwing * (1.0f - progress * 0.5f))

        // 3. Target Position
        val sideDir = -dynamicMargin
        val targetX = ninja.x + sideDir
        val targetY = ninja.y - dynamicFloat

        // 4. Spring Physics with Dynamic Damping
        val displacementX = targetX - x
        val displacementY = targetY - y

        // SLOW DOWN BEFORE CATCH: Use damping relative to the ninja's speed (approx 250)
        // This prevents the dragon from falling behind due to world-space damping
        val ninjaSpeedX = 250.0f
        
        val slowDownRange = 1.0f - progress // Transitions to 0
        val effectiveStiffness = stiffness * (1.0f + progress * 2.0f) // Get "stronger" as it gets closer
        val effectiveDamping = damping + (progress * 15.0f)

        val springForceX = displacementX * effectiveStiffness.toDouble()
        val springForceY = displacementY * effectiveStiffness.toDouble()
        
        // Damp relative to ninja's speed so we don't fight the forward movement
        val relativeVelX = velocityX - ninjaSpeedX
        val dampingForceX = relativeVelX.toDouble() * effectiveDamping.toDouble()
        val dampingForceY = velocityY.toDouble() * effectiveDamping.toDouble()

        velocityX += ((springForceX - dampingForceX) * dt).toFloat()
        velocityY += ((springForceY - dampingForceY) * dt).toFloat()

        // Speed Management - ensure we can always keep up with the ninja
        val minSpeedNeeded = ninjaSpeedX + 50.0f
        val currentMaxSpeed = max(minSpeedNeeded, maxEntranceSpeed * (0.6f + 0.4f * slowDownRange))
        val currentSpeed = sqrt(velocityX.toDouble() * velocityX + velocityY * velocityY)
        if (currentSpeed > currentMaxSpeed) {
            val ratio = currentMaxSpeed / currentSpeed
            velocityX *= ratio.toFloat()
            velocityY *= ratio.toFloat()
        }

        x += velocityX * dt
        y += velocityY * dt

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
}
