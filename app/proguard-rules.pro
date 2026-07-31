# ─── Kotlin / coroutines ──────────────────────────────────────────────────────
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ─── Jetpack Compose ─────────────────────────────────────────────────────────
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ─── DataStore (Preferences) ─────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.protobuf.** { *; }
-dontwarn androidx.datastore.**

# ─── App model + enums ───────────────────────────────────────────────────────
# Enum valueOf/values are reflected on by Kotlin's `entries` and by DataStore keys.
-keepclassmembers enum com.rehan.rangoli.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static ** getEntries();
}
-keep class com.rehan.rangoli.domain.** { *; }

# ─── Misc ────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses, Signature, SourceFile, LineNumberTable
