package com.hexcorp.futoshiki.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.hexcorp.futoshiki.R
import kotlin.random.Random

enum class Sound(val resIds: IntArray) {
    BUTTON(intArrayOf(R.raw.tap)),
    SELECT(intArrayOf(R.raw.select)),
    TAP(intArrayOf(R.raw.tap)),
    ERROR(intArrayOf(R.raw.error)),
    WIN(intArrayOf(R.raw.success)),
    LOSS(intArrayOf(R.raw.loss)),
    START(intArrayOf(R.raw.card)),
    WRONG(intArrayOf(R.raw.wrong)),
    TOGGLE(intArrayOf(R.raw.toggle_on, R.raw.toggle_off)),
    SWIPE(intArrayOf(R.raw.swipe));

    fun randomResId(): Int = if (resIds.size == 1) resIds[0] else resIds[Random.nextInt(resIds.size)]
}

object SoundManager {
    private var pool: SoundPool? = null
    private val loadedIds = HashMap<Int, Int>()
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttrs)
            .build()

        Sound.entries.forEach { sound ->
            sound.resIds.forEach { resId ->
                loadedIds[resId] = soundPool.load(context, resId, 1)
            }
        }

        pool = soundPool
    }

    fun play(sound: Sound, speed: Float = 1f) {
        val soundPool = pool ?: return
        val resId = sound.randomResId()
        val streamId = loadedIds[resId] ?: return
        soundPool.play(streamId, 1f, 1f, 1, 0, speed)
    }

    fun release() {
        pool?.release()
        pool = null
        loadedIds.clear()
        initialized = false
    }
}
