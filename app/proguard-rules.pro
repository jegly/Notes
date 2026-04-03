# =============================================================================
# Notes App — R8 rules (replaces legacy ProGuard boilerplate)
# R8 is the default shrinker/obfuscator since AGP 3.4.
# These rules supplement R8's built-in defaults.
# =============================================================================

# ---------------------------------------------------------------------------
# Argon2kt — native JNI library
# The argon2kt library loads native .so files at runtime via System.loadLibrary.
# Prevent R8 from removing the JNI bridge classes.
# ---------------------------------------------------------------------------
-keep class com.lambdapioneer.argon2kt.** { *; }
-keepclassmembers class com.lambdapioneer.argon2kt.** { *; }

# ---------------------------------------------------------------------------
# Android Keystore / security
# These classes are loaded reflectively by the Android security framework.
# ---------------------------------------------------------------------------
-keep class android.security.keystore.** { *; }
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }

# ---------------------------------------------------------------------------
# Room database
# Entity, DAO and database classes are accessed via reflection by Room.
# ---------------------------------------------------------------------------
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao class * { *; }

# ---------------------------------------------------------------------------
# Hilt — dependency injection
# Hilt generates code at compile time; R8 rules are included in Hilt's
# consumer-rules.pro automatically. These are belt-and-suspenders.
# ---------------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
}

# ---------------------------------------------------------------------------
# Kotlin coroutines
# Coroutines use reflection for debug mode; suppressed in release but kept
# for stack trace readability.
# ---------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ---------------------------------------------------------------------------
# DataStore / Preferences
# ---------------------------------------------------------------------------
-keep class androidx.datastore.** { *; }

# ---------------------------------------------------------------------------
# Glance (widgets)
# ---------------------------------------------------------------------------
-keep class androidx.glance.** { *; }

# ---------------------------------------------------------------------------
# Coil (image loading)
# ---------------------------------------------------------------------------
-keep class coil.** { *; }

# ---------------------------------------------------------------------------
# Kotlin serialization — keep metadata for reflection
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

# ---------------------------------------------------------------------------
# Crash / stack trace readability (release builds)
# Keep line numbers for crash reports without exposing source file names.
# ---------------------------------------------------------------------------
-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile
