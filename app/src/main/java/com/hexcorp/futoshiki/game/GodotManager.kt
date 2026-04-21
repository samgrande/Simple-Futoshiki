package com.hexcorp.futoshiki.game

import org.godotengine.godot.GodotFragment
import org.godotengine.godot.GodotLib

class GodotManager(private val fragment: GodotFragment) {

    /**
     * Call this when a Futoshiki mistake is made.
     * aggressionLevel should be 0.0 (calm) to 1.0 (max chaos).
     */
    fun updateDragonAggression(aggressionLevel: Float) {
        // We must talk to Godot on the Render Thread to avoid crashes
        fragment.getGodot()?.runOnRenderThread {
            // Call the 'update_aggression' function in Godot.
            // Note: In Godot 4, direct native calls require an object ID. 
            // 0 is used here as a placeholder; for a real setup, consider using a GodotPlugin.
            GodotLib.calldeferred(0, "update_aggression", arrayOf(aggressionLevel))
        }
    }

    fun setBackgroundTransparent() {
        fragment.view?.apply {
            // Ensures the Dragon doesn't block touches to the Futoshiki grid
            isClickable = false
            isFocusable = false
        }
    }

    /**
     * Pauses or resumes the Godot engine's processing.
     */
    fun setPaused(isPaused: Boolean) {
        fragment.getGodot()?.runOnRenderThread {
            // Call the 'set_paused' function in Godot
            GodotLib.calldeferred(0, "set_paused", arrayOf(isPaused))
        }
    }
}