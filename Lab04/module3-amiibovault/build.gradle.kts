// ============================================================================
// AMIIBO VAULT - Build Configuration (Root/Project Level)
// ============================================================================
// Este archivo configura los plugins a nivel de proyecto.
// Los plugins se declaran aquí pero se aplican en el módulo :app
//
// NOTA IMPORTANTE sobre KSP:
// - KSP (Kotlin Symbol Processing) reemplaza a KAPT
// - Es hasta 2x más rápido que KAPT
// - Genera código Kotlin nativo (no stubs de Java)
// - La versión de KSP DEBE coincidir con la versión de Kotlin
// ============================================================================

plugins {
    // -------------------------
    // Plugins de Android/Kotlin
    // -------------------------
    // 'alias' referencia plugins definidos en libs.versions.toml
    // 'apply false' significa: "declara pero no apliques aquí"
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // -------------------------
    // KSP - Kotlin Symbol Processing
    // -------------------------
    // Reemplaza a KAPT para procesamiento de anotaciones
    // Room lo usa para generar implementaciones de DAOs
    alias(libs.plugins.ksp) apply false

    // -------------------------
    // Kotlinx Serialization
    // -------------------------
    // Plugin del compilador para generar serializadores
    // Necesario para parsear JSON con @Serializable
    alias(libs.plugins.kotlin.serialization) apply false
}
