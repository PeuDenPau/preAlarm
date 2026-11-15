# kotlinx-serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.peudenhau.prealarm.**$$serializer { *; }
-keepclassmembers class com.peudenhau.prealarm.** { *** Companion; }
-keepclasseswithmembers class com.peudenhau.prealarm.** { kotlinx.serialization.KSerializer serializer(...); }
