// ============================================================================
// AMIIBO VAULT - Build Configuration (App Module)
// ============================================================================
// Este archivo configura el módulo principal de la aplicación.
//
// CONCEPTOS CLAVE:
// 1. KSP vs KAPT:
//    - KAPT (Kotlin Annotation Processing Tool) está OBSOLETO
//    - KSP es más rápido y genera código Kotlin nativo
//    - Room usa KSP para generar implementaciones de DAOs
//
// 2. Version Catalog (libs.versions.toml):
//    - Centraliza todas las versiones en un solo lugar
//    - Permite type-safe references con 'libs.xxx'
// ============================================================================

plugins {
    // Plugin de aplicación Android
    alias(libs.plugins.android.application)
    // Soporte de Kotlin para Android
    alias(libs.plugins.kotlin.android)
    // Plugin de Compose para Kotlin 2.0+
    alias(libs.plugins.kotlin.compose)
    // KSP - Procesamiento de símbolos de Kotlin (reemplaza a KAPT)
    alias(libs.plugins.ksp)
    // Kotlinx Serialization - para parsear JSON
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.curso.android.module3.amiibo"

    // -------------------------
    // SDK Versions
    // -------------------------
    // compileSdk: Versión del SDK usada para COMPILAR
    // - Define qué APIs puedes usar en el código
    // - No afecta a los usuarios finales
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.curso.android.module3.amiibo"

        // minSdk: Versión MÍNIMA de Android requerida
        // - Usuarios con versiones anteriores NO pueden instalar la app
        // - API 24 = Android 7.0 (Nougat)
        minSdk = libs.versions.minSdk.get().toInt()

        // targetSdk: Versión de Android OBJETIVO
        // - Indica al sistema qué comportamientos espera la app
        // - Afecta cómo el sistema ejecuta la app
        targetSdk = libs.versions.targetSdk.get().toInt()

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        // Habilita Jetpack Compose
        compose = true
    }
}

dependencies {
    // =========================================================================
    // ANDROIDX CORE
    // =========================================================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // =========================================================================
    // JETPACK COMPOSE
    // =========================================================================
    // BOM (Bill of Materials): Asegura versiones compatibles de todas las libs de Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // =========================================================================
    // NAVIGATION COMPOSE
    // =========================================================================
    implementation(libs.androidx.navigation.compose)

    // Solo para desarrollo - herramientas de debugging de Compose
    debugImplementation(libs.androidx.ui.tooling)

    // =========================================================================
    // ROOM DATABASE (Persistencia Local)
    // =========================================================================
    // Room es el ORM recomendado por Google para SQLite en Android
    //
    // Componentes:
    // - room-runtime: Clases base (Database, Entity, Dao)
    // - room-ktx: Extensiones de Kotlin (Flow, suspend functions)
    // - room-compiler: Genera código en tiempo de compilación (usa KSP)
    //
    // IMPORTANTE: Usar 'ksp' en lugar de 'kapt' para el compiler
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)  // KSP genera las implementaciones de DAOs

    // =========================================================================
    // RETROFIT (Networking)
    // =========================================================================
    // Retrofit es un cliente HTTP type-safe para Android/Java
    //
    // Componentes:
    // - retrofit: Core library
    // - converter: Convierte JSON <-> Kotlin objects
    // - okhttp-logging: Interceptor para ver requests/responses en Logcat
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)

    // Kotlinx Serialization - parseo de JSON
    // Ventajas sobre Gson/Moshi:
    // - Nativo de Kotlin (soporta data classes, default values, nullability)
    // - Generación de código en compilación (más rápido en runtime)
    implementation(libs.kotlinx.serialization.json)

    // =========================================================================
    // COIL (Image Loading)
    // =========================================================================
    // Coil es una librería de carga de imágenes para Android
    // Diseñada para Kotlin y Coroutines
    //
    // Componentes:
    // - coil-compose: Composable AsyncImage para Jetpack Compose
    // - coil-network-okhttp: Backend de red usando OkHttp
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // =========================================================================
    // KOIN (Dependency Injection)
    // =========================================================================
    // Koin es un framework de inyección de dependencias ligero para Kotlin
    // No usa reflexión ni generación de código - es DSL puro
    //
    // Componentes:
    // - koin-android: Integración con Android (Context, ViewModel)
    // - koin-androidx-compose: Integración con Jetpack Compose
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
