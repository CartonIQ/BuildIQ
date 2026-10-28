# BuildIQ Proguard / R8 Rules

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class com.cartoniq.buildiq.data.** { *; }
-keep class com.cartoniq.buildiq.model.** { *; }
-keep class com.cartoniq.buildiq.backup.** { *; }

# Google Mobile Ads and UMP
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.android.ump.** {
   public *;
}

# Android Material & Navigation
-keepclassmembers class * extends androidx.navigation.NavController {
    public *;
}
-keep class com.google.android.material.** { *; }
