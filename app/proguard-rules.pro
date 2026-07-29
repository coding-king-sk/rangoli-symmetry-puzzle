# Compose and Kotlin metadata are handled by the default AGP rules.
# Keep our pure-Kotlin domain enums intact (used by name in tests/telemetry).
-keepclassmembers enum com.rehan.rangoli.domain.** { *; }
