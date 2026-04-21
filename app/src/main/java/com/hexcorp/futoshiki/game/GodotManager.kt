package com.hexcorp.futoshiki.game

import org.godotengine.godot.GodotFragment

class GodotManager(private val fragment: GodotFragment) {

    /**
     * Call this when a Futoshiki mistake is made.
     * aggressionLevel should be 0.0 (calm) to 1.0 (max chaos).
     */
    fun updateDragonAggression(aggressionLevel: Float) {
        // We must talk to Godot on the Render Thread to avoid crashes
        fragment.getGodot()?.runOnRenderThread {
            // In Godot 4.x, you typically use a GodotPlugin to communicate.
            // If you are using a custom bridge, replace this with your specific call.
            // fragment.getGodot()?.nativeCall("update_aggression", aggressionLevel)
        }
    }

    fun setBackgroundTransparent() {
        fragment.view?.apply {
            // Ensures the Dragon doesn't block touches to the Futoshiki grid
            isClickable = false
            isFocusable = false
        }
    }
}