package com.hexcorp.futoshiki.game.entities

import korlibs.image.bitmap.*
import korlibs.korge.view.Image as KorgeImage
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.time.*
import kotlinx.coroutines.*

class NinjaEntity(
    private val standSheet: Bitmap,
    private val runSheet: Bitmap,
    private val jumpSheet: Bitmap,
    private val frameWidth: Int,
    private val frameHeight: Int,
    private val manager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager
) : Container() {

    private var currentState = NinjaAnimationState.STAND
    private var isIntro = true
    private var autoRun = false
    private var velocityY = 0.0f
    private var velocityX = 0.0f
    private var speedMultiplier = 1.0f

    private val GRAVITY = 0.5f
    private val BASE_SPEED = 250.0f
    private val JUMP_VELOCITY = -370.0f

    private lateinit var sprite: KorgeImage
    private var currentFrame = 0
    private var animationTimer = 0.0f
    private val frameDuration = 0.1f // 10 FPS

    init {
        // Initialize the sprite component with stand sheet
        val firstFrame = standSheet.slice(RectangleInt(0, 0, frameWidth, frameHeight))
        sprite = KorgeImage(firstFrame)
        sprite.anchor(Anchor.BOTTOM_CENTER)
        addChild(sprite)
    }

    suspend fun runIntroSequence(manager: com.hexcorp.futoshiki.ui.korge.KorGEGameManager) {
        suspend fun waitPausable(ms: Long) {
            var e = 0L
            while (e < ms) {
                if (!manager.isPaused) e += 16
                delay(16)
            }
        }

        // 1. INITIAL STATE: Standing still, looking Right
        currentState = NinjaAnimationState.STAND
        sprite.scaleX = 1.0
        waitPausable(500)

        // 2. THE TRACKING: Dragon is now flying through the sky (Right -> Left)
        waitPausable(1200)

        // Ninja "follows" the dragon by turning Left as it passes over him
        sprite.scaleX = -1.0
        waitPausable(1300)

        // 3. THE REACTION: Dragon has vanished to the left.
        // Ninja looks back Right, realizes the chase is starting, and bolts!
        sprite.scaleX = 1.0
        waitPausable(300)

        isIntro = false
        autoRun = true
        manager.signalRunningStarted()
    }

    fun skipIntro() {
        x = 100.0
        isIntro = false
        autoRun = true
        manager.signalRunningStarted()
    }

    fun triggerWin() {
        autoRun = false
        velocityX = 0.0f
        velocityY = 0.0f
        currentState = NinjaAnimationState.STAND
    }

    fun update(dt: Double, floorY: Double) {
        if (isIntro) {
            velocityX = 0.0f
        } else {
            if (autoRun) {
                velocityX = BASE_SPEED * speedMultiplier
                sprite.scaleX = 1.0 // Running towards the right
            }
        }

        // Apply Gravity
        velocityY += GRAVITY

        // Update Position
        x += (velocityX * dt).toFloat()
        y += (velocityY * dt).toFloat()

        // Basic Floor Collision
        if (y >= floorY) {
            y = floorY
            velocityY = 0.0f
            if (kotlin.math.abs(velocityX) > 0.1f) {
                currentState = NinjaAnimationState.RUNNING
            } else {
                currentState = NinjaAnimationState.STAND
            }
        } else {
            currentState = NinjaAnimationState.JUMPING
        }

        updateAnimation(dt.toFloat())
    }

    private fun updateAnimation(dt: Float) {
        animationTimer += dt
        if (animationTimer >= frameDuration) {
            animationTimer = 0.0f
            
            // Choose the correct sheet based on state
            val currentSheet = when (currentState) {
                NinjaAnimationState.STAND -> standSheet
                NinjaAnimationState.RUNNING -> runSheet
                NinjaAnimationState.JUMPING -> jumpSheet
            }

            // Number of frames might differ per sheet, adjust here if needed
            val framesInSheet = 10 
            currentFrame = (currentFrame + 1) % framesInSheet

            // We assume each sheet is a single row of animation for that specific state
            val newSubImage = currentSheet.slice(RectangleInt(
                currentFrame * frameWidth,
                0, // All specialized sheets are now row 0
                frameWidth,
                frameHeight
            ))

            sprite.bitmap = newSubImage
        }
    }

    fun jump() {
        if (!isIntro && velocityY == 0.0f) {
            velocityY = JUMP_VELOCITY
        }
    }

    fun setSpeedMultiplier(multiplier: Float) {
        speedMultiplier = multiplier
    }

    fun getSpeedMultiplier(): Float = speedMultiplier

    fun resetForRestart() {
        // Don't reset x/y - let the intro sequence handle positioning
        isIntro = true
        autoRun = false
        velocityY = 0.0f
        velocityX = 0.0f
        currentState = NinjaAnimationState.STAND
        currentFrame = 0
        animationTimer = 0.0f
        sprite.scaleX = 1.0
        // Reset to stand sheet
        val firstFrame = standSheet.slice(RectangleInt(0, 0, frameWidth, frameHeight))
        sprite.bitmap = firstFrame
    }
}
