extends CharacterBody2D

const SPEED = 250.0 
const JUMP_VELOCITY = -370.0

@onready var sprite = $AnimatedSprite2D

var is_intro = true
var auto_run = false

func _ready():
	run_intro_sequence()

func run_intro_sequence():
	# 1. INITIAL STATE: Standing still, looking Right
	# This is when the dragon starts spawning off-screen to the right.
	sprite.flip_h = false
	sprite.play("Stand")
	await get_tree().create_timer(0.5).timeout 
	
	# 2. THE TRACKING: Dragon is now flying through the sky (Right -> Left)
	# Ninja continues looking Right as the dragon enters the frame
	sprite.flip_h = false 
	await get_tree().create_timer(1.2).timeout 
	
	# Ninja "follows" the dragon by turning Left as it passes over him
	sprite.flip_h = true
	await get_tree().create_timer(1.3).timeout 
	
	# 3. THE REACTION: Dragon has vanished to the left. 
	# Ninja looks back Right, realizes the chase is starting, and bolts!
	sprite.flip_h = false
	
	# Small "panic" pause before running
	await get_tree().create_timer(0.3).timeout
	
	is_intro = false
	auto_run = true

func _physics_process(delta: float) -> void:
	if not is_on_floor():
		velocity += get_gravity() * delta

	if is_intro:
		velocity.x = 0
	else:
		if auto_run:
			velocity.x = SPEED
			sprite.flip_h = false # Running towards the right
		else:
			var direction := Input.get_axis("ui_left", "ui_right")
			if direction:
				velocity.x = direction * SPEED
				sprite.flip_h = (direction < 0)
			else:
				velocity.x = move_toward(velocity.x, 0, SPEED)

	update_animations()
	move_and_slide()

	# Loop back for the demo/UI box
	if position.x > 2000:
		position.x = -500

func update_animations():
	if not is_on_floor():
		sprite.play("Jumping")
	elif is_intro:
		sprite.play("Stand")
	elif abs(velocity.x) > 0.1:
		sprite.play("Running")
	else:
		sprite.play("Stand")
