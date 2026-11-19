# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# ---- Kotlin ----
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract ** *Dao();
}
-keep class com.annhonii.cloudygallery.data.local.db.** { *; }

# ---- Hilt ----
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keepclasseswithmembernames class * {
    @dagger.hilt.* <methods>;
    @dagger.hilt.* <fields>;
}
-dontwarn dagger.hilt.**

# ---- Ktor ----
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.netty.** { *; }
-dontwarn io.netty.**
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**

# ---- Coil ----
-keep class coil.** { *; }
-dontwarn coil.**

# ---- OkHttp ----
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** { *; }
-dontwarn okio.**

# ---- Kotlinx Serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.annhonii.cloudygallery.**$$serializer { *; }
-keepclassmembers class com.annhonii.cloudygallery.** {
    *** Companion;
}
-keepclasseswithmembers class com.annhonii.cloudygallery.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- App entities ----
-keep class com.annhonii.cloudygallery.** { *; }
-keepclassmembers class com.annhonii.cloudygallery.** { *; }

# ---- WorkManager ----
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ---- Encrypted SharedPreferences ----
-keep class androidx.security.crypto.** { *; }

# ---- Compose ----
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ---- Remove logging in release ----
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
