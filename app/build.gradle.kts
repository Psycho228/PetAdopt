plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

import java.io.FileInputStream
import java.util.Properties

// Загрузка секретов из .env файла
val envProperties = Properties().apply {
    val envFile = file("${rootDir}/.env")
    if (envFile.exists()) {
        load(FileInputStream(envFile))
    } else {
        println("⚠️  Файл .env не найден! Используйте .env.example как шаблон.")
    }
}

fun getEnv(key: String, default: String = ""): String {
    return (envProperties[key] as? String) ?: default
}

fun getEnvField(key: String, default: String = ""): String {
    val value = getEnv(key, default)
    return value.replace("\"", "\\\"")
}

android {
    namespace = "com.example.petadopt"

    buildFeatures {
        compose = true
        buildConfig = true
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
        
        // Секреты загружаются из .env файла
        buildConfigField("String", "S3_ACCESS_KEY", "\"${getEnvField("S3_ACCESS_KEY", "")}\"")
        buildConfigField("String", "S3_SECRET_KEY", "\"${getEnvField("S3_SECRET_KEY", "")}\"")
        buildConfigField("String", "S3_BUCKET_NAME", "\"${getEnvField("S3_BUCKET_NAME", "pet-photos")}\"")
        buildConfigField("String", "S3_ENDPOINT_URL", "\"${getEnvField("S3_ENDPOINT_URL", "https://s3.regru.cloud")}\"")
        
        buildConfigField("String", "GIGACHAT_CLIENT_ID", "\"${getEnvField("GIGACHAT_CLIENT_ID", "")}\"")
        buildConfigField("String", "GIGACHAT_SCOPE", "\"${getEnvField("GIGACHAT_SCOPE", "GIGACHAT_API_PERS")}\"")
        buildConfigField("String", "GIGACHAT_AUTH_KEY", "\"${getEnvField("GIGACHAT_AUTH_KEY", "")}\"")
        
        buildConfigField("String", "SUPABASE_URL", "\"${getEnvField("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${getEnvField("SUPABASE_ANON_KEY", "")}\"")
        
        // Проверка наличия секретов при сборке
        val s3AccessKey = getEnv("S3_ACCESS_KEY", "")
        val gigaChatClientId = getEnv("GIGACHAT_CLIENT_ID", "")
        
        if (s3AccessKey.isEmpty() || gigaChatClientId.isEmpty()) {
            println("⚠️  ВНИМАНИЕ: Некоторые секреты не найдены в .env файле!")
            println("   Создайте .env файл на основе .env.example")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Можно включить signing для release (нужно настроить keystore)
            // signingConfig = signingConfigs.getByName("debug") // для тестирования
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
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
    implementation(libs.graphics.path)
    // Lifecycle для ViewModel в Compose
    implementation(libs.lifecycle.viewmodel.compose)

    // Compose
    implementation(libs.compose.material3)
    implementation(libs.compose.material.iconsExtended)
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
