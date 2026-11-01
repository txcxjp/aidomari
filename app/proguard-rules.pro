# Proguard rules for Aidomari
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
