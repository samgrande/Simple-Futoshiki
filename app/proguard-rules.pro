# Add project specific ProGuard rules here.
# AGP and R8 handle Compose, Kotlin, and AndroidX automatically.

# Godot engine — keep all JNI-bridged classes and their members intact.
-keep class org.godotengine.** { *; }
-keepclassmembers class org.godotengine.** { *; }
-keep class com.godot.** { *; }
