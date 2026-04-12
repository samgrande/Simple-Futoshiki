# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK's default proguard file.

# Keep the application class
-keep class com.hexcorp.futoshiki.** { *; }

# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Kotlin
-keep class kotlin.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
