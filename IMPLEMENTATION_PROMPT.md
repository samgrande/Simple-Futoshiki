# Futoshiki Game Scoring & Position System - Implementation Prompt

## Project Overview
You are implementing a scoring and position control system for a Futoshiki puzzle game in Kotlin/Android. The ninja character runs from a dragon - player progress is tied to puzzle completion.

## Architecture
- **Platform**: Android with Jetpack Compose + KorGE (game engine)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose

## Requirements

### 1. Score System (GameState.kt)
Add score tracking to the game state data class:
- `score: Int = 60` - Starting score (60 points)
- `completedRowsCount: Int = 0` - Track completed rows
- `completedColumnsCount: Int = 0` - Track completed columns
- `caught: Boolean = false` - Player caught by dragon

File: `app/src/main/java/com/hexcorp/futoshiki/game/GameState.kt`

### 2. Column Validation (PuzzleEngine.kt)
Add functions to detect completed columns (mirrors existing row functions):

```kotlin
fun getCompletedColumns(grid: List<List<Int>>, size: Int, errors: Set<String>): Set<Int> {
    if (errors.isNotEmpty()) return emptySet()
    val completed = mutableSetOf<Int>()
    for (c in 0 until size) {
        val colValues = (0 until size).map { grid[it][c] }
        if (colValues.all { it != 0 } && colValues.toSet().size == size) {
            completed.add(c)
        }
    }
    return completed
}

fun getCompletedColumnsCount(grid: List<List<Int>>, size: Int, errors: Set<String>): Int {
    return getCompletedColumns(grid, size, errors).size
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/game/PuzzleEngine.kt`

### 3. Position Control Manager (KorGEGameManager.kt)
Replace existing boost system with direct position control:

```kotlin
package com.hexcorp.futoshiki.ui.korge

import com.hexcorp.futoshiki.game.entities.GameWorld
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class KorGEGameManager {
    private val _aggression = MutableStateFlow(0f)
    val aggression = _aggression.asStateFlow()

    private val _playerPosition = MutableStateFlow(600.0)
    val playerPosition = _playerPosition.asStateFlow()

    var gameWorld: GameWorld? = null

    fun updateAggression(value: Float) {
        _aggression.value = value
    }

    fun updatePlayerPosition(score: Int) {
        // Score 60 = 600px ahead of dragon
        // Score 0 = at dragon position
        _playerPosition.value = (score * 10).toDouble()
    }

    fun getPlayerPosition(): Double = _playerPosition.value

    fun resetPosition() {
        _playerPosition.value = 600.0
    }
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/ui/korge/KorGEGameManager.kt`

### 4. Ninja Position Control (NinjaEntity.kt)
Add `setPosition()` and simplify `runIntroSequence()`:

```kotlin
fun setPosition(newX: Double) {
    x = newX
}

fun startRunning() {
    autoRun = true
    isIntro = false
}
```

Simplify `runIntroSequence()` to just set initial state:
```kotlin
suspend fun runIntroSequence() {
    currentState = NinjaAnimationState.STAND
    sprite.scaleX = 1.0
    delay(500)
    isIntro = false
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/game/entities/NinjaEntity.kt`

### 5. Stationary Dragon (DragonEntity.kt)
Simplify to stationary with hover animation only:

```kotlin
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
    private val frameHeight: Int
) : Container() {

    private var timePassed = 0.0f
    private var pulseSpeed = 0.5f
    private var hovered = false

    private lateinit var sprite: KorgeImage
    private var currentFrame = 0
    private var animationTimer = 0.0f
    private val frameDuration = 0.1f

    init {
        val firstFrame = spriteSheet.slice(RectangleInt(0, 0, frameWidth, frameHeight))
        sprite = KorgeImage(firstFrame)
        sprite.anchor(Anchor.CENTER)
        addChild(sprite)
        x = 0.0
        y = -400.0
        sprite.scaleX = 1.0
        sprite.scaleY = 1.0
        visible = true
    }

    fun setTarget(ninja: View) {}

    suspend fun runCinematicIntro() {}

    fun update(dt: Double) {
        timePassed += dt.toFloat()
        hovered = sin(timePassed * pulseSpeed) > 0
        y = -400.0 + (if (hovered) -30.0 else 30.0)
        updateAnimation(dt.toFloat())
    }

    private fun updateAnimation(dt: Float) {
        animationTimer += dt
        if (animationTimer >= frameDuration) {
            animationTimer = 0.0f
            currentFrame = (currentFrame + 1) % 10
            val newSubImage = spriteSheet.slice(RectangleInt(
                currentFrame * frameWidth, 0, frameWidth, frameHeight
            ))
            sprite.bitmap = newSubImage
        }
    }

    fun updateAggression(value: Float) {
        pulseSpeed = 2.0f + (value * 8.0f)
    }

    fun setPosition(newX: Double) {
        x = newX
    }
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/game/entities/DragonEntity.kt`

### 6. Game World Position Control (GameWorld.kt)
Simplify to control player position directly:

```kotlin
private var floorY = 410.0
private var playerPosition = 600.0

fun startGame() {
    playerPosition = 600.0
    GlobalScope.launch {
        if (::ninja.isInitialized) {
            ninja.setPosition(playerPosition)
            ninja.startRunning()
        }
    }
}

fun setPlayerPosition(position: Double) {
    playerPosition = position
    if (::ninja.isInitialized) {
        ninja.setPosition(position)
    }
}

fun update(dt: Double, aggression: Float) {
    if (!::ninja.isInitialized || !::dragon.isInitialized) return
    
    ninja.update(dt, floorY + 95.0)
    dragon.update(dt)
    dragon.updateAggression(aggression)

    // Camera follows ninja
    this.x = 500.0 - ninja.x
    layers.forEach { it.update(ninja.x) }
    children.firstOrNull { it is SolidRect }?.let { it.x = ninja.x }
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/game/entities/GameWorld.kt`

### 7. View Model Score Logic (FutoshikiViewModel.kt)
Core score calculation on each input:

```kotlin
fun inputNumber(num: Int) {
    val st = _state.value
    val (r, c) = st.selected ?: return
    if (st.won || st.puzzle == null || st.caught) return
    if (st.puzzle.initial[r][c] != 0) return

    val newGrid = st.grid.mapIndexed { ri, row ->
        if (ri == r) row.toMutableList().also { it[c] = num } else row
    }
    val errors = validateGrid(newGrid, st.size, st.puzzle)
    
    val currentCompletedRows = getCompletedRowsCount(newGrid, st.size, errors)
    val currentCompletedColumns = getCompletedColumnsCount(newGrid, st.size, errors)
    
    // Calculate score: 60 + rows*10 + cols*10 - error penalty
    var newScore = 60
    newScore += currentCompletedRows * 10
    newScore += currentCompletedColumns * 10
    if (errors.isNotEmpty()) {
        newScore -= 10
    }
    newScore = newScore.coerceAtLeast(0)
    
    korgeManager.updatePlayerPosition(newScore)
    
    val allRowsComplete = currentCompletedRows == st.size
    val allColsComplete = currentCompletedColumns == st.size
    val won = allRowsComplete && allColsComplete && errors.isEmpty()
    val caught = newScore <= 0 && !won

    _state.update { it.copy(
        grid = newGrid,
        errors = errors,
        won = won,
        completedRowsCount = currentCompletedRows,
        completedColumnsCount = currentCompletedColumns,
        score = newScore,
        caught = caught
    ) }

    if (caught) {
        stopTimer()
        korgeManager.updateAggression(1.0f)
        viewModelScope.launch {
            delay(300)
            _state.update { it.copy(showCongrats = true) }
        }
    } else if (won) {
        stopTimer()
        korgeManager.updateAggression(0.5f)
        viewModelScope.launch {
            delay(800)
            _state.update { it.copy(showCongrats = true) }
        }
    } else if (errors.isNotEmpty()) {
        korgeManager.updateAggression(0.7f)
    } else {
        korgeManager.updateAggression(0f)
    }
}
```

In `newGame()`:
```kotlin
korgeManager.resetPosition()
_state.update { st ->
    st.copy(
        screen = Screen.GAME,
        size = size,
        puzzle = puzzle,
        grid = grid,
        selected = null,
        errors = emptySet(),
        won = false,
        isSolved = false,
        showCongrats = false,
        timerSeconds = 0,
        timerRunning = true,
        gameKey = st.gameKey + 1,
        completedRowsCount = 0,
        completedColumnsCount = 0,
        score = 60,
        caught = false
    )
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/game/FutoshikiViewModel.kt`

### 8. Game View Update (KorGEView.kt)
Pass player position to game world:

```kotlin
@Composable
fun KorGEView(
    aggression: Float,
    playerPosition: Double = 600.0,
    modifier: Modifier = Modifier
) {
    val state = remember { KorGEState() }
    state.aggression = aggression
    state.playerPosition = playerPosition

    AndroidView(
        factory = { context ->
            KorgeAndroidView(context).also { view ->
                view.loadModule(Korge(
                    backgroundColor = if (isDark) Colors[accentHex] else Colors["#f5f2f2"],
                    virtualSize = Size(1000, 500),
                    displayMode = KorgeDisplayMode(ScaleMode.COVER, Anchor.BOTTOM_CENTER, clipBorders = true),
                    main = {
                        val world = GameWorld(assets, isDark, accentHex)
                        state.world = world
                        addChild(world)
                        world.setupWorld()
                        world.startGame()
                        addUpdater { dt ->
                            state.world?.setPlayerPosition(state.playerPosition)
                            world.update(dt.seconds, state.aggression)
                        }
                    }
                ))
            }
        },
        update = { _ -> },
        modifier = modifier
    )
}

private class KorGEState {
    var world: GameWorld? = null
    var aggression: Float = 0f
    var playerPosition: Double = 600.0
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/ui/korge/KorGEView.kt`

### 9. Score Display Component (ScorePill.kt)
Create new UI component for score display:

```kotlin
package com.hexcorp.futoshiki.ui.components.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.ui.theme.FutoshikiColors
import com.hexcorp.futoshiki.ui.theme.LocalIsDark
import com.hexcorp.futoshiki.ui.theme.ReemKufi

@Composable
fun ScorePill(
    score: Int,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val scoreColor = when {
        score <= 0 -> Color(0xFF4CAF50)      // Green when caught
        score < 30 -> Color(0xFFFF5722)    // Orange-Red when low
        score < 50 -> Color(0xFFFF9800)    // Orange when medium
        else -> if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32)  // Green when high
    }
    
    val scoreBg by animateColorAsState(
        targetValue = FutoshikiColors.timerBg(),
        animationSpec = tween(300),
        label = "scoreBg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(scoreBg)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "SCORE",
            color = FutoshikiColors.timerText().copy(alpha = 0.6f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = ReemKufi,
            letterSpacing = 1.sp
        )
        Text(
            text = if (score >= 0) "+$score" else "$score",
            color = scoreColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ReemKufi,
            letterSpacing = 1.sp
        )
    }
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/ui/components/shared/ScorePill.kt`

### 10. Game Header with Score (GameHeader.kt)
Update to include score display:

```kotlin
@Composable
fun GameHeader(
    size: Int,
    timerSeconds: Int,
    score: Int,
    won: Boolean,
    caught: Boolean,
    isPaused: Boolean,
    showTabs: Boolean,
    onTitleClick: () -> Unit,
    onTimerClick: () -> Unit,
    onSizeChange: (Int) -> Unit,
    animatedBg: Color,
    animatedBorder: Color,
    headerH: Dp,
    tabH: Dp,
    containerCoordinates: LayoutCoordinates?,
    onPillPositioned: (Offset, Offset) -> Unit,
    hideGameContent: Boolean
) {
    // ... existing structure ...
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScorePill(
            score = score,
            modifier = Modifier.graphicsLayer { alpha = if (hideGameContent) 0f else 1f }
        )
        TimerPill(/* ... existing params ... */)
    }
    // ...
}
```

Add import: `import com.hexcorp.futoshiki.ui.components.shared.ScorePill`

File: `app/src/main/java/com/hexcorp/futoshiki/ui/screens/game/GameHeader.kt`

### 11. Game End Modal (WinModal.kt → GameEndModal.kt)
Create dual-mode modal for win and caught states:

```kotlin
package com.hexcorp.futoshiki.ui.screens.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexcorp.futoshiki.R
import com.hexcorp.futoshiki.ui.components.shared.formatTimer
import com.hexcorp.futoshiki.ui.theme.ReemKufi
import com.hexcorp.futoshiki.ui.theme.accentColor
import kotlinx.coroutines.delay

@Composable
fun GameEndModal(
    isWin: Boolean,
    score: Int,
    timerSeconds: Int,
    onPlayAgain: () -> Unit
) {
    BackHandler { onPlayAgain() }

    var visible by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val vibrator = remember { 
        context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator 
    }

    LaunchedEffect(Unit) {
        visible = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1000, easing = LinearOutSlowInEasing),
        label = "modalAlpha"
    )

    var buttonBounds by remember { mutableStateOf<Rect?>(null) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isShaking by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isShaking = true
            // Vibrate feedback
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200L)
                }
            }
        } else {
            isShaking = false
        }
    }

    val shakeTransition = rememberInfiniteTransition(label = "shakeTransition")
    val shakeAnim by shakeTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
    ) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
            RainBackground(buttonBounds)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1.3f))

                if (isWin) {
                    Image(
                        painter = painterResource(id = R.drawable.kanji_congrats),
                        contentDescription = "Congratulations",
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .aspectRatio(180f / 312f)
                            .graphicsLayer {
                                translationX = if (isShaking) shakeAnim else 0f
                                rotationZ = if (isShaking) shakeAnim * 0.5f else 0f
                            }
                    )
                    Spacer(modifier = Modifier.height(72.dp))
                    Text(
                        text = "C O N G R A T U L A T I O N",
                        color = accentColor(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = ReemKufi,
                        letterSpacing = 2.sp
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.kanji_congrats),
                        contentDescription = "Try Again",
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .aspectRatio(180f / 312f)
                            .graphicsLayer {
                                translationX = if (isShaking) shakeAnim else 0f
                                rotationZ = if (isShaking) shakeAnim * 0.5f else 0f
                            }
                    )
                    Spacer(modifier = Modifier.height(72.dp))
                    Text(
                        text = "T R Y   A G A I N",
                        color = Color(0xFFFF5722),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = ReemKufi,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = if (isWin) "S O L V E D   I N" else "T H E   D R A G O N",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = ReemKufi,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val timeStr = formatTimer(timerSeconds)
                Text(
                    text = timeStr,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ReemKufi
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isWin) "SCORE: $score" else "REACHED: $score",
                    color = if (isWin) Color(0xFF4CAF50) else Color(0xFFFF5722),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = ReemKufi
                )

                Spacer(modifier = Modifier.weight(1f))

                val buttonText = if (isWin) "PLAY AGAIN" else "TRY AGAIN"
                val buttonColor = if (isWin) accentColor() else Color(0xFFFF5722)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp)
                        .onGloballyPositioned { buttonBounds = it.boundsInRoot() }
                        .border(2.dp, buttonColor, RoundedCornerShape(32.dp))
                        .clickable { onPlayAgain() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ReemKufi
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/ui/screens/game/WinModal.kt`

### 12. Game Screen Updates (GameScreen.kt)
Update KorGEView call:
```kotlin
if (!state.isSolved) {
    KorGEView(
        aggression = viewModel.korgeManager.aggression.collectAsState().value,
        playerPosition = viewModel.korgeManager.playerPosition.collectAsState().value.toDouble(),
        modifier = Modifier.fillMaxWidth().height(korgeHeight)
    )
```

Update GameHeader call:
```kotlin
GameHeader(
    size = size,
    timerSeconds = state.timerSeconds,
    score = state.score,
    won = won,
    caught = state.caught,
    isPaused = isPaused,
    // ... rest of params
)
```

Update modal call:
```kotlin
if (state.showCongrats) {
    val isWin = state.won && !state.caught
    GameEndModal(
        isWin = isWin,
        score = state.score,
        timerSeconds = state.timerSeconds,
        onPlayAgain = { viewModel.newGame(size) }
    )
}
```

File: `app/src/main/java/com/hexcorp/futoshiki/ui/screens/game/GameScreen.kt`

## Game Logic Summary

| Scenario | Score Change | Position | UI |
|----------|-------------|----------|-----|
| Start game | 60 | 600px | Timer starts |
| Complete row | +10 | +100px | Score updates |
| Complete column | +10 | +100px | Score updates |
| Error in grid | -10 | -100px | Score updates, aggression increases |
| Score ≤ 0 | caught | At dragon | "TRY AGAIN" modal |
| All rows + cols filled | win | Escaped | "CONGRATULATIONS" modal |

## Build Verification
Run: `./gradlew :app:compileDebugKotlin`