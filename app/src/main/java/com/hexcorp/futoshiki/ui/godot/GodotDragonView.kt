package com.hexcorp.futoshiki.ui.godot

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import org.godotengine.godot.GodotFragment

@Composable
fun GodotDragonView(
    onReady: (GodotFragment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity ?: return
    val containerId = remember { View.generateViewId() }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = containerId
                // Ensure touch transparency for the Futoshiki grid
                isClickable = false
                isFocusable = false
                
                post {
                    // Using Bundle arguments as the specific AAR version 
                    // doesn't appear to have the Builder pattern exposed.
                    val godotFragment = GodotFragment()
                    val args = android.os.Bundle()
                    args.putString("main_scene_file", "res://game.tscn")
                    godotFragment.arguments = args
                        
                    activity.supportFragmentManager.beginTransaction()
                        .replace(id, godotFragment)
                        .commitAllowingStateLoss()
                        
                    onReady(godotFragment)
                }
            }
        }
    )
}
