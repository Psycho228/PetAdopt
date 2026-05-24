# PetAdopt ProGuard/R8 Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }

# Keep Supabase classes
-keep class io.github.jan.supabase.** { *; }
-keep class com.russhwolf.settings.** { *; }

# Keep Ktor classes
-keep class io.ktor.** { *; }

# Keep AWS SDK classes
-keep class com.amazonaws.** { *; }
-keep class software.amazon.awssdk.** { *; }

# Keep data classes (serialization)
-keep class com.example.petadopt.data.model.** { *; }
-keep class com.example.petadopt.viewmodel.QuestionnaireState { *; }
-keep class com.example.petadopt.viewmodel.QuestionnaireAnswer { *; }

# Keep Gson/Kotlinx.serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

# Keep kotlinx.serialization
-keep class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.json.** { *; }

# Keep serializers (kotlinx.serialization)
-keep class com.example.petadopt.**Serializer { *; }

# Keep GigaChat DTOs
-keep class com.example.petadopt.data.model.GigaChatRiskAssessment { *; }
-keep class com.example.petadopt.data.model.RiskAssessmentRecord { *; }

# Keep BuildConfig
-keep class com.example.petadopt.BuildConfig { *; }

# Keep data classes for JSON serialization
-keep class com.example.petadopt.data.model.Application { *; }
-keep class com.example.petadopt.data.model.Pet { *; }
-keep class com.example.petadopt.data.model.User { *; }
-keep class com.example.petadopt.data.model.QuestionnaireAnswer { *; }

# No warnings for missing classes (optional)
-dontwarn io.github.jan.supabase.**
-dontwarn com.russhwolf.settings.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn kotlin.coroutines.**
-dontwarn android.**

# Optimize (optional)
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Remove logging in release (optional)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
