// ============================================================================
// AMIIBO VAULT - Settings Configuration
// ============================================================================
// Este archivo configura:
// 1. Los repositorios de plugins (pluginManagement)
// 2. Los repositorios de dependencias (dependencyResolutionManagement)
// 3. Los módulos incluidos en el proyecto
// ============================================================================

pluginManagement {
    repositories {
        // Google's Maven repository - contiene plugins de Android
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Maven Central - repositorio principal de JVM
        mavenCentral()
        // Gradle Plugin Portal - plugins de Gradle
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS: Falla si un módulo define sus propios repositorios
    // Esto asegura que todas las dependencias vengan de aquí
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Nombre del proyecto raíz
rootProject.name = "AmiiboVault"

// Módulos incluidos en el proyecto
include(":app")
