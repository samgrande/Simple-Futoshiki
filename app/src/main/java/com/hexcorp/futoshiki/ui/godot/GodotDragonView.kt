package com.hexcorp.futoshiki.ui.godot

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
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

fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

class MyGodotFragment : GodotFragment() {
    override fun getCommandLine(): List<String> = emptyList()

    override fun onAttach(context: android.content.Context) {
        val args = arguments ?: android.os.Bundle()
        if (!args.containsKey("use_transparent_background")) {
            args.putBoolean("use_transparent_background", true)
            args.putInt("background_color", android.graphics.Color.TRANSPARENT)
            arguments = args
        }
        super.onAttach(context)
    }

    override fun onGodotSetupCompleted() {
        super.onGodotSetupCompleted()
        godot?.let {
            it.enableImmersiveMode(false)
            it.enableEdgeToEdge(false)
        }
        // Hide Godot's input artifact (EditText) if it exists
        view?.let { hideEditTexts(it) }
    }

    private fun hideEditTexts(view: View) {
        if (view is android.widget.EditText) {
            view.visibility = View.GONE
            view.isFocusable = false
            view.isEnabled = false
        } else if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                hideEditTexts(view.getChildAt(i))
            }
        }
    }
}

private object GodotContainer {
    val id = View.generateViewId()
}

@Composable
fun GodotDragonView(
    modifier: Modifier = Modifier,
    godotFragment: GodotFragment? = null,
    onReady: (GodotFragment) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    
    if (activity == null) return

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = GodotContainer.id
                isClickable = false
                isFocusable = false
                
                post {
                    try {
                        val fm = activity.supportFragmentManager
                        // Use the provided reference or find by tag
                        var fragment = godotFragment ?: (fm.findFragmentByTag("godot_dragon") as? GodotFragment)
                        
                        val transaction = fm.beginTransaction()
                        
                        if (fragment == null) {
                            Log.d("GodotDragonView", "Creating NEW GodotFragment")
                            val newFragment = MyGodotFragment().apply {
                                arguments = android.os.Bundle().apply {
                                    putBoolean("xr_mode", false)
                                    putBoolean("use_transparent_background", true)
                                    putInt("background_color", android.graphics.Color.TRANSPARENT)
                                }
                            }
                            transaction.add(id, newFragment, "godot_dragon")
                            fragment = newFragment
                        } else {
                            Log.d("GodotDragonView", "Re-attaching EXISTING GodotFragment")
                            // Ensure it's not attached to an old view
                            (fragment.view?.parent as? android.view.ViewGroup)?.removeView(fragment.view)
                            transaction.replace(id, fragment, "godot_dragon")
                        }
                        
                        transaction.commitAllowingStateLoss()
                        onReady(fragment)
                    } catch (e: Exception) {
                        Log.e("GodotDragonView", "Error in GodotFragment transaction", e)
                    }
                }
            }
        },
        update = { _ -> }
    )
}
