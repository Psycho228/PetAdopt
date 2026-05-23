plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.petadopt"

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.petadopt"
        minSdk = 24
        targetSdk = 34

        // S3 конфигурация (временно вшиты для проверки)
        buildConfigField("String", "S3_ACCESS_KEY", "\"N8Z0ZYU4W3IHSGZKBBN5\"")
        buildConfigField("String", "S3_SECRET_KEY", "\"Yu7Z54MtphmMqXB0zOZSIaqWYCphil1gXOyywWKm\"")
        buildConfigField("String", "S3_BUCKET_NAME", "\"pet-photos\"")
        
        // GigaChat конфигурация
        buildConfigField("String", "GIGACHAT_CLIENT_ID", "\"019e516c-1cd7-7e6a-abb0-cfa756884880\"")
        buildConfigField("String", "GIGACHAT_SCOPE", "\"GIGACHAT_API_PERS\"")
        buildConfigField("String", "GIGACHAT_AUTH_KEY", "\"MDE5ZTUxNmMtMWNkNy03ZTZhLWFiYjAtY2ZhNzU2ODg0ODgwOjU2NjM4MzA3LTFhMmUtNDBjNy1iMTc4LWQwOGZhOGZhMWM4Zg==\"")
    }
}

dependencies {
    // AWS SDK for S3
    implementation(libs.aws.sdk.s3)

    // Supabase
    implementation(libs.supabase.core)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.gotrue)
    implementation(libs.supabase.storage)

    // Ktor (для Supabase на Android и GigaChat)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // AndroidX
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    // Lifecycle для ViewModel в Compose
    implementation(libs.lifecycle.viewmodel.compose)

    // Compose
    implementation(libs.compose.material3)
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}

kapt {
    correctErrorTypes = true
}