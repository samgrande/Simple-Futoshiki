extends CharacterBody2D

# --- TUNED FOR TIGHTER CHASE ---
@export var stiffness: float = 18.0        # Snappier response to stay near
@export var damping: float = 10.0          # Prevents jittering while close
@export var max_entrance_speed: float = 800.0 

# --- SLOW BUT TIGHT MOVEMENT ---
var time_passed: float = 0.0
@export var pulse_speed: float = 0.5       # Slow majestic cycles
@export var hover_breadth: float = 150.0   # REDUCED: Dragon won't drift far
@export var vertical_swing: float = 80.0   # Subtle height changes

var target: CharacterBody2D = null
@onready var sprite = $AnimatedSprite2D

func _ready():
	visible = false
	set_physics_process(false)
	motion_mode = CharacterBody2D.MOTION_MODE_FLOATING
	set_collision_mask_value(1, false) 
	run_cinematic_intro()

func find_player():
	var players = get_tree().get_nodes_in_group("ninja")
	for p in players:
		if p != self:
			target = p
			break

func run_cinematic_intro():
	await get_tree().create_timer(0.2).timeout
	find_player()
	
	if target:
		# PHASE 1: TEASER (Right to Left)
		global_position = target.global_position + Vector2(2500, -700)
		scale = Vector2(2.5, 2.5) 
		sprite.flip_h = true     
		visible = true
		var tween = create_tween().set_trans(Tween.TRANS_SINE).set_ease(Tween.EASE_IN_OUT)
		tween.tween_property(self, "global_position", target.global_position + Vector2(-2500, -700), 2.5)
		await tween.finished
		visible = false
		
		# PHASE 2: START CHASE
		await get_tree().create_timer(0.5).timeout 
		global_position = target.global_position + Vector2(-1500, -1000)
		scale = Vector2(1.0, 1.0) 
		if has_method("reset_physics_interpolation"):
			reset_physics_interpolation()
		visible = true
		set_physics_process(true)

func _physics_process(delta):
	if target == null:
		find_player()
		return

	time_passed += delta

	# 1. Flip & Animate
	sprite.flip_h = target.global_position.x < global_position.x
	if sprite.sprite_frames:
		sprite.play(sprite.sprite_frames.get_animation_names()[0])

	# 2. THE TIGHT CHASE MATH
	var wave = sin(time_passed * pulse_speed) 
	
	# CHANGED: Average distance is 250px. 
	# It will now cycle between 100px (Close) and 400px (Backing off).
	var dynamic_margin = 250.0 + (wave * hover_breadth)
	
	# Height stays tighter to the player as well
	var dynamic_float = 180.0 + (cos(time_passed * pulse_speed * 0.5) * vertical_swing)

	# 3. Target Position
	var side_dir = -dynamic_margin if target.velocity.x >= 0 else dynamic_margin
	var target_pos = target.global_position + Vector2(side_dir, -dynamic_float)
	
	# 4. Spring Physics
	var displacement = target_pos - global_position
	var spring_force = displacement * stiffness
	var damping_force = velocity * damping
	
	velocity += (spring_force - damping_force) * delta
	
	# Speed Management
	var distance_to_ninja = global_position.distance_to(target_pos)
	if distance_to_ninja > 400:
		velocity = velocity.limit_length(max_entrance_speed)
	else:
		# Keep speed consistent with the Ninja (250) so it stays in frame
		var min_chase_speed = target.velocity.length() + 80.0
		velocity = velocity.limit_length(max(330.0, min_chase_speed))

	move_and_slide()
	
	# Add this at the end of dragon.gd
func update_aggression(value: float):
	# This will be called from your Android Studio Kotlin code
	# value is 0.0 (calm) to 1.0 (aggressive)
	
	# Example: Make the sine wave faster as mistakes increase
	pulse_speed = 2.0 + (value * 8.0) 
	
	# Example: Make the dragon hover closer/tighter to the player
	stiffness = 5.0 + (value * 15.0)
	
	print("Dragon Aggression Level: ", value)
