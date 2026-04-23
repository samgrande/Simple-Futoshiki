package com.hexcorp.futoshiki.ui.korge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.hexcorp.futoshiki.game.entities.AssetManager
import com.hexcorp.futoshiki.game.entities.GameWorld
import korlibs.korge.Korge
import korlibs.korge.KorgeDisplayMode
import korlibs.korge.android.KorgeAndroidView
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.image.color.Colors
import korlibs.time.*

import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.accentColor
import androidx.compose.ui.graphics.toArgb

@Composable
fun KorGEView(
    aggression: Float,
    modifier: Modifier = Modifier
) {
    val state = remember { KorGEState() }
    state.aggression = aggression
    val isDark = LocalIsDark.current
    val accent = accentColor()
    val accentHex = String.format("#%06X", 0xFFFFFF and accent.toArgb())

    AndroidView(
        factory = { context ->
            KorgeAndroidView(context).also { view ->
                view.loadModule(Korge(
                    backgroundColor = if (isDark) Colors[accentHex] else Colors["#f5f2f2"],
                    virtualSize = Size(1000, 500),
                    displayMode = KorgeDisplayMode(ScaleMode.COVER, Anchor.BOTTOM_CENTER, clipBorders = true),
                    main = {
                        val assets = AssetManager()
                        val world = GameWorld(assets, isDark, accentHex)
                        state.world = world

                        addChild(world)
                        world.setupWorld()
                        world.startGame()

                        addUpdater { dt ->
                            world.update(dt.seconds, state.aggression)
                        }
                    }
                ))
            }
        },
        update = { _ ->
            // Aggression is updated via the state object in the updater
        },
        modifier = modifier
    )
}

private class KorGEState {
    var world: GameWorld? = null
    var aggression: Float = 0f
}
