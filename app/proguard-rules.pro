# Optimized ProGuard rules for Production

# Firebase specific rules (handled by Firebase SDK mostly, but keeping these for safety)
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep your data models (REQUIRED for Firestore/Auth serialization)
-keep class com.sangeetsetu.app.model.** { *; }

# Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Credentials and Auth
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Compose - Usually handled by default, but ensuring icons and runtime are kept
-keep class androidx.compose.material.icons.** { *; }

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
