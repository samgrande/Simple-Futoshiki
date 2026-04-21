extends CharacterBody2D

# --- Dragon Animation & Interaction ---
@onready var sprite = $AnimatedSprite2D

var target_pos = Vector2(100, -200) # Relative to ninja or screen
var current_aggression = 0.0
var time_passed = 0.0
var ninja = null

func _ready():
	print("Dragon: _ready() called")
	visible = true
	modulate = Color(1, 1, 1, 1)
	z_index = 10 # Ensure it's on top of background

	if sprite:
		sprite.play("dragon")
		sprite.scale = Vector2(0.8, 0.8) # Significantly increased base scale

	# Find ninja and teleport nearby immediately
	ninja = get_tree().get_first_node_in_group("ninja")
	if ninja:
		global_position = ninja.global_position + Vector2(-200, -100)
	else:
		global_position = Vector2(200, 200)

func _process(delta):
	time_passed += delta

	# Find ninja if not found yet
	if not ninja:
		ninja = get_tree().get_first_node_in_group("ninja")

	# Target position relative to ninja
	var base_target = Vector2(200, 200)
	if ninja:
		base_target = ninja.global_position + Vector2(-150, -80)

	# Idle hover movement
	var hover_offset = Vector2(
		sin(time_passed * 1.5) * 30.0,
		cos(time_passed * 1.2) * 20.0
	)

	# Smoothly move towards target position
	global_position = global_position.lerp(base_target + hover_offset, delta * 2.0)

	# Aggression effects
	if sprite:
		sprite.speed_scale = 1.0 + current_aggression * 1.5
		# Base scale 0.6 (increased from 0.5) + aggression boost
		var s = 0.6 + current_aggression * 0.24
		sprite.scale = Vector2(s, s)

func update_aggression(value: float):
	print("Dragon: Updating aggression to ", value)
	current_aggression = clamp(value, 0.0, 1.0)

	# When aggressive, maybe move closer to top
	if current_aggression > 0.5:
		target_pos.y = 350
	else:
		target_pos.y = 500

func set_target_position(x: float, y: float):
	target_pos = Vector2(x, y)
