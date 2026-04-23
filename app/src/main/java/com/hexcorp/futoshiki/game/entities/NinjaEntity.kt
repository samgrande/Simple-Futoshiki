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
    private val frameHeight: Int
) : Container() {

    private var currentState = NinjaAnimationState.STAND
    private var isIntro = true
    private var autoRun = false
    private var velocityY = 0.0f
    private var velocityX = 0.0f

    private val GRAVITY = 0.5f
    private val SPEED = 250.0f
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

    suspend fun runIntroSequence() {
        // 1. INITIAL STATE: Standing still, looking Right
        currentState = NinjaAnimationState.STAND
        sprite.scaleX = 1.0
        delay(500)

        // 2. THE TRACKING: Dragon is now flying through the sky (Right -> Left)
        delay(1200)

        // Ninja "follows" the dragon by turning Left as it passes over him
        sprite.scaleX = -1.0
        delay(1300)

        // 3. THE REACTION: Dragon has vanished to the left.
        // Ninja looks back Right, realizes the chase is starting, and bolts!
        sprite.scaleX = 1.0
        delay(300)

        isIntro = false
        autoRun = true
    }

    fun update(dt: Double, floorY: Double) {
        if (isIntro) {
            velocityX = 0.0f
        } else {
            if (autoRun) {
                velocityX = SPEED
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
}
