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
    manager: KorGEGameManager,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
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
                        manager.gameWorld = world
                        
                        world.setupWorld()
                        world.startGame(skipIntro = manager.introFinished)
                        
                        // Mark as finished so future re-compositions (theme changes) skip the intro
                        manager.introFinished = true
                        
                        addChild(world)

                        addUpdater { dt ->
                            if (!manager.isPaused) {
                                world.setSpeedMultiplier(manager.speedMultiplier.value)
                                world.setDistanceMultiplier(manager.distanceMultiplier.value)
                                world.setNinjaScreenX(manager.ninjaScreenX.value)
                                world.update(dt.seconds, manager.aggression.value)
                            }
                        }
                    }
                ))
            }
        },
        update = { _ ->
            manager.gameWorld?.updateTheme(isDark, accentHex)
            manager.isPaused = isPaused
        },
        modifier = modifier
    )
}

private class KorGEState {
    var world: GameWorld? = null
    var aggression: Float = 0f
    var speedMultiplier: Float = 1.0f
    var distanceMultiplier: Float = 1.0f
    var ninjaScreenX: Float = 500f
}
