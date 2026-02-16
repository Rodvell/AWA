package com.curso.android.module3.amiibo

import android.app.Application
import com.curso.android.module3.amiibo.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * ============================================================================
 * AMIIBO APPLICATION - Punto de Entrada de la Aplicación
 * ============================================================================
 *
 * La clase Application es el primer componente que se crea cuando la app inicia.
 * Se usa para:
 * 1. Inicializar librerías que necesitan contexto (Koin, Coil, etc.)
 * 2. Configurar dependencias globales
 * 3. Registrar callbacks del ciclo de vida
 *
 * IMPORTANTE:
 * - Esta clase DEBE estar registrada en AndroidManifest.xml
 *   con android:name=".AmiiboApplication"
 * - Solo existe UNA instancia durante todo el lifecycle de la app
 * - onCreate() se llama ANTES de cualquier Activity
 *
 * ============================================================================
 */
class AmiiboApplication : Application() {

    /**
     * =========================================================================
     * INICIALIZACIÓN DE LA APLICACIÓN
     * =========================================================================
     *
     * onCreate() se llama cuando la app se crea por primera vez.
     * Es el lugar ideal para inicializar:
     * - Dependency Injection (Koin, Dagger/Hilt)
     * - Crash reporting (Firebase Crashlytics)
     * - Analytics
     * - Librerías de terceros
     *
     * NOTA: Mantener onCreate() lo más ligero posible.
     * Inicializaciones pesadas deben hacerse en background.
     */
    override fun onCreate() {
        super.onCreate()

        // Inicializar Koin (Dependency Injection)
        initializeKoin()
    }

    /**
     * =========================================================================
     * CONFIGURACIÓN DE KOIN
     * =========================================================================
     *
     * startKoin { } configura el contenedor de dependencias.
     *
     * Componentes:
     * - androidLogger(): Activa logging de Koin en Logcat
     * - androidContext(): Provee el contexto de la app a los módulos
     * - modules(): Lista de módulos con definiciones de dependencias
     *
     * Una vez iniciado, Koin está disponible globalmente para:
     * - by inject() en Activities/Fragments
     * - koinViewModel() en Compose
     * - get() en módulos
     */
    private fun initializeKoin() {
        startKoin {
            /**
             * androidLogger():
             * - Imprime logs de Koin en Logcat
             * - Level.DEBUG: Verbose (solo para desarrollo)
             * - Level.INFO: Normal
             * - Level.ERROR: Solo errores (producción)
             */
            androidLogger(Level.DEBUG)

            /**
             * androidContext():
             * - Hace disponible el contexto de la aplicación
             * - Se usa con androidContext() dentro de los módulos
             * - Necesario para Room, SharedPreferences, etc.
             */
            androidContext(this@AmiiboApplication)

            /**
             * modules():
             * - Lista de módulos Koin a cargar
             * - Cada módulo define un conjunto de dependencias
             * - Pueden cargarse múltiples módulos
             *
             * Ejemplo con múltiples módulos:
             * modules(networkModule, databaseModule, viewModelModule)
             */
            modules(appModule)
        }
    }
}

/**
 * ============================================================================
 * NOTAS ADICIONALES SOBRE APPLICATION CLASS
 * ============================================================================
 *
 * 1. LIFECYCLE:
 *    - onCreate(): Cuando la app inicia
 *    - onTerminate(): Nunca se llama en dispositivos reales (solo emulador)
 *    - onLowMemory(): El sistema necesita liberar memoria
 *    - onTrimMemory(level): Oportunidad de liberar recursos
 *
 * 2. CONTEXTO:
 *    - Application es un Context
 *    - Usar applicationContext para singletons que sobreviven Activities
 *    - NUNCA guardar referencia a Activity en Application (memory leak)
 *
 * 3. PROCESS DEATH:
 *    - El sistema puede matar el proceso en background
 *    - Al restaurar, onCreate() se llama de nuevo
 *    - Los singletons en memoria se pierden
 *    - Usar Room/SharedPreferences para datos persistentes
 *
 * 4. MULTI-PROCESS:
 *    - Si usas android:process en Manifest, Application se crea por proceso
 *    - Cada proceso tiene su propia instancia de Application
 *
 * ============================================================================
 */
