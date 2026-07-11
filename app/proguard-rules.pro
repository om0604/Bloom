# Add project specific ProGuard rules here.
# Bloom — ProGuard configuration

# --- Kotlin ---
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# --- Gemini AI SDK ---
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- Compose ---
-keep class androidx.compose.** { *; }
