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
    private val frameHeight: Int,
    private val manager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager
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

    // Internal scope for entrance animations (cancelled on reset)
    private val entityScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var entranceJob: Job? = null

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

    suspend fun runCinematicIntro(manager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager) {
        while (manager.isPaused) delay(100)
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

        var elapsed = 0L
        while (elapsed < duration * 1000) {
            if (!manager.isPaused) {
                val progress = elapsed.toFloat() / (duration * 1000)
                x = startX + (endX - startX) * progress
                elapsed += 16
            }
            delay(16)
        }

        visible = false
        var wait = 0L
        while (wait < 500) {
            if (!manager.isPaused) wait += 16
            delay(16)
        }

        // PHASE 2: SLIDE IN FROM THE LEFT EDGE
        // X: drive a screen-space offset so the slide is correct even as ninja runs.
        // Y: use the same dynamicFloat formula as update() so there is zero snap on handoff.
        alpha = 0.0
        sprite.scaleX = 1.0
        sprite.scaleY = 1.0
        visible = true

        val enterDuration = 4200L   // slow, dramatic entrance
        var enterElapsed  = 0L
        while (enterElapsed < enterDuration) {
            if (!manager.isPaused) {
                val t     = enterElapsed.toFloat() / enterDuration.toFloat()
                // Ease-in-out sine: starts gently, smooth arc, decelerates softly
                val eased = (-(cos(PI.toFloat() * t) - 1f) / 2f)
                val screenOffset = -1000f * (1f - eased)
                x = ninja.x - 400.0 + screenOffset
                val dynamicFloat = 220.0f + (cos(timePassed * pulseSpeed * 0.5f) * verticalSwing)
                y = ninja.y - dynamicFloat.toDouble()
                alpha = eased.toDouble()
                enterElapsed += 16
            }
            delay(16)
        }
        // Land exactly where update() would place it
        val dynamicFloatFinal = 220.0f + (cos(timePassed * pulseSpeed * 0.5f) * verticalSwing)
        x     = ninja.x - 400.0
        y     = ninja.y - dynamicFloatFinal.toDouble()
        alpha = 1.0
        isChasing = true
        velocityX = 250.0f
    }

    fun skipIntro(ninja: NinjaEntity) {
        // X: screen-space offset tracks ninja live.
        // Y: matches update()'s dynamicFloat formula so there is no snap on handoff.
        entranceJob?.cancel()
        isChasing = false
        sprite.scaleX = 1.0
        sprite.scaleY = 1.0
        alpha = 0.0
        val initDynamic = 220.0f + (cos(timePassed * pulseSpeed * 0.5f) * verticalSwing)
        x = ninja.x - 400.0 - 800.0   // start off-screen (screen offset -800)
        y = ninja.y - initDynamic.toDouble()
        visible = true
        entranceJob = entityScope.launch {
            val enterDuration = 2200L   // slow, dramatic
            var enterElapsed  = 0L
            while (enterElapsed < enterDuration) {
                val t     = enterElapsed.toFloat() / enterDuration.toFloat()
                // Ease-in-out sine: smooth arc entry
                val eased = (-(cos(PI.toFloat() * t) - 1f) / 2f)
                val screenOffset = -800f * (1f - eased)
                x = ninja.x - 400.0 + screenOffset
                val dynamicFloat = 220.0f + (cos(timePassed * pulseSpeed * 0.5f) * verticalSwing)
                y = ninja.y - dynamicFloat.toDouble()
                alpha = eased.toDouble()
                enterElapsed += 16
                delay(16)
            }
            val dynamicFloatFinal = 220.0f + (cos(timePassed * pulseSpeed * 0.5f) * verticalSwing)
            x     = ninja.x - 400.0
            y     = ninja.y - dynamicFloatFinal.toDouble()
            alpha = 1.0
            isChasing = true
            velocityX = 250.0f
        }
    }

    fun setIdle() {
        isChasing = false
        visible = false
    }

    suspend fun runWinFlyAway() {
        isChasing = false
        // Fly away to the left quickly
        val startX = x
        val startY = y
        val duration = 2.5f
        
        var elapsed = 0L
        while (elapsed < duration * 1000) {
            if (!manager.isPaused) {
                val progress = elapsed.toFloat() / (duration * 1000)
                // Curve slightly upward and fast to the left
                x = startX - (1200.0f * progress)
                y = startY - (300.0f * sin(progress * PI.toFloat()))
                elapsed += 16
            }
            delay(16)
        }
        visible = false
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
        pulseSpeed = 2.0f + (value * 1.0f)
    }

    fun setDistanceMultiplier(multiplier: Float) {
        distanceMultiplier = multiplier
    }

    fun resetForRestart() {
        entranceJob?.cancel()
        entranceJob = null
        timePassed = 0.0f
        pulseSpeed = 0.5f
        hoverBreadth = 150.0f
        verticalSwing = 80.0f
        velocityX = 0.0f
        velocityY = 0.0f
        isChasing = false
        chaseTime = 0.0f
        currentAggression = 0.0f
        distanceMultiplier = 1.0f
        stiffness = 18.0f
        damping = 10.0f
        currentFrame = 0
        animationTimer = 0.0f
        alpha = 1.0
        visible = false
        x = 0.0
        y = 0.0
        // Reset animation to frame 0
        val firstFrame = spriteSheet.slice(RectangleInt(0, 0, frameWidth, frameHeight))
        sprite.bitmap = firstFrame
    }
}
