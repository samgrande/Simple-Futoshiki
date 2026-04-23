# Plan: Godot to KorGE Migration

## Goal
Migrate the mini-game assets, scripts, and scene structure from the removed Godot engine to the current KorGE implementation. The game features a Ninja being chased by a Dragon across a parallax background, with the Dragon's behavior driven by the puzzle game's "aggression" state.

## 1. Asset Mapping
### Sprites (`app/src/main/assets/sprites`)
| Godot Resource | KorGE Target | Note |
|---|---|---|
| `ninja.png`, `stand.png`, `jump.png` | `Image` / `Sprite` | Map to Stand, Run, Jump animations |
| `dragon.png` | `Image` / `Sprite` | Dragon entity sprite |
| `mountains back.png` | `Image` | Background Parallax Layer 3 |
| `cloud1.png`, `cloud2.png`, `gate.png`, `Tree.png`, `statue.png` | `Image` | Mid-ground Parallax Layer 2 |
| `ground.png` | `Image` | Foreground Parallax Layer 1 |

### Logic (`app/src/main/assets/scenes`)
| Godot Script | Kotlin Implementation | Key Logic to Port |
|---|---|---|
| `ninja.gd` | `NinjaEntity.kt` | Intro sequence, gravity, auto-run, animation state machine |
| `dragon.gd` | `DragonEntity.kt` | Spring physics (stiffness/damping), sine-wave hovering, aggression scaling |
| `game.tscn` | `GameWorld.kt` | View hierarchy, Parallax layers, Camera2D behavior |

## 2. Technical Implementation Phases

### Phase 1: Asset & Animation Setup
- [ ] Analyze `ninja.png` and `dragon.png` to determine if they are sprite sheets.
- [ ] Create an animation mapping system in KorGE to handle "Stand", "Running", and "Jumping" states.
- [ ] Implement a sprite loader utility to handle assets from the Android assets folder.

### Phase 2: Entity Implementation
- [ ] **Ninja Entity**:
    - Implement `NinjaEntity` extending `View`.
    - Port the `run_intro_sequence()` logic (timing-based state changes).
    - Implement basic physics (vertical velocity, gravity).
    - Implement the `auto_run` movement (constant rightward velocity).
- [ ] **Dragon Entity**:
    - Implement `DragonEntity` extending `View`.
    - Port the "Teaser" intro (Right $\rightarrow$ Left fly-by).
    - Implement the "Tight Chase" spring physics:
        - `displacement = target_pos - current_pos`
        - `force = (displacement * stiffness) - (velocity * damping)`
    - Implement the sine-wave hovering for height and distance.
    - Create the `updateAggression(value: Float)` method to scale `pulse_speed` and `stiffness`.

### Phase 3: Environment & World Construction
- [ ] **Parallax System**:
    - Create `ParallaxLayer` views that repeat based on a `repeat_size`.
    - Implement three layers with different scroll speeds:
        - Layer 1 (Ground): Fast
        - Layer 2 (Clouds/Trees): Medium
        - Layer 3 (Mountains): Slow
- [ ] **Game World**:
    - Create a `GameWorld` class to coordinate the Ninja, Dragon, and Parallax layers.
    - Implement a virtual camera that follows the Ninja's X position.

### Phase 4: Integration & UI Wiring
- [ ] **KorGEView Update**:
    - Replace the placeholder rotating rectangle in `KorGEView.kt` with the `GameWorld`.
    - Pass the `aggression` value from the ViewModel into the `DragonEntity` via the `KorGEGameManager`.
- [ ] **Lifecycle Management**:
    - Ensure the game loop starts/stops correctly with the `GameScreen` lifecycle.

## 3. Verification Plan
- [ ] **Intro Sequence**: Verify Ninja stands $\rightarrow$ turns $\rightarrow$ runs.
- [ ] **Chase Physics**: Verify Dragon follows Ninja smoothly without jittering.
- [ ] **Aggression Test**: Verify that increasing aggression makes the Dragon move more erratically/closer.
- [ ] **Parallax Check**: Ensure background layers move at relative speeds to create depth.
- [ ] **Performance**: Check for frame drops on Android devices.
