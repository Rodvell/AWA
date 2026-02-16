package com.curso.android.module3.amiibo.di

import androidx.room.Room
import com.curso.android.module3.amiibo.data.local.db.AmiiboDatabase
import com.curso.android.module3.amiibo.data.remote.api.AmiiboApiService
import com.curso.android.module3.amiibo.repository.AmiiboRepository
import com.curso.android.module3.amiibo.ui.viewmodel.AmiiboDetailViewModel
import com.curso.android.module3.amiibo.ui.viewmodel.AmiiboViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * ============================================================================
 * KOIN APP MODULE - Configuración de Inyección de Dependencias
 * ============================================================================
 *
 * Koin es un framework de DI (Dependency Injection) ligero para Kotlin.
 * A diferencia de Dagger/Hilt, NO usa generación de código ni reflexión.
 *
 * CONCEPTOS CLAVE DE KOIN:
 * ------------------------
 * - module { }: Define un contenedor de dependencias
 * - single { }: Crea una instancia SINGLETON (una sola para toda la app)
 * - factory { }: Crea una NUEVA instancia cada vez que se solicita
 * - viewModel { }: Crea ViewModels con lifecycle awareness
 * - get(): Obtiene una dependencia ya registrada
 *
 * GRAFO DE DEPENDENCIAS DE ESTA APP:
 * ----------------------------------
 *
 *   ┌─────────────────────────────────────────────────────────────────┐
 *   │                        KOIN CONTAINER                           │
 *   │                                                                 │
 *   │   ┌──────────────┐                                             │
 *   │   │   OkHttp     │                                             │
 *   │   │   Client     │                                             │
 *   │   └──────┬───────┘                                             │
 *   │          │                                                      │
 *   │          ▼                                                      │
 *   │   ┌──────────────┐     ┌──────────────┐                        │
 *   │   │   Retrofit   │     │    Room      │                        │
 *   │   │   (API)      │     │   Database   │                        │
 *   │   └──────┬───────┘     └──────┬───────┘                        │
 *   │          │                     │                                │
 *   │          │    ┌────────────────┘                                │
 *   │          │    │                                                 │
 *   │          ▼    ▼                                                 │
 *   │   ┌──────────────┐                                             │
 *   │   │  Repository  │◄──── single { AmiiboRepository(get(),get())}│
 *   │   └──────┬───────┘                                             │
 *   │          │                                                      │
 *   │          ▼                                                      │
 *   │   ┌──────────────┐                                             │
 *   │   │  ViewModel   │◄──── viewModel { AmiiboViewModel(get()) }   │
 *   │   └──────────────┘                                             │
 *   │                                                                 │
 *   └─────────────────────────────────────────────────────────────────┘
 *
 * ============================================================================
 */

/**
 * Configuración de Json para kotlinx.serialization.
 *
 * ignoreUnknownKeys = true:
 * - Ignora campos del JSON que no están en nuestras data classes
 * - La API de Amiibo tiene muchos campos que no necesitamos
 * - Sin esto, lanzaría excepción al encontrar campos desconocidos
 */
private val json = Json {
    ignoreUnknownKeys = true  // Ignora campos del JSON que no mapeamos
    isLenient = true          // Más tolerante con JSON mal formado
}

/**
 * Módulo principal de Koin que define todas las dependencias de la app.
 *
 * ORDEN DE DEFINICIÓN:
 * -------------------
 * 1. Networking (OkHttp, Retrofit)
 * 2. Database (Room)
 * 3. Repository (depende de API y DB)
 * 4. ViewModel (depende de Repository)
 */
val appModule = module {

    // =========================================================================
    // NETWORKING - OkHttpClient
    // =========================================================================
    /**
     * Cliente HTTP base para Retrofit.
     *
     * single { }: Crea UN solo cliente para toda la app (eficiente)
     *
     * HttpLoggingInterceptor:
     * - Loguea requests y responses en Logcat
     * - Útil para debugging
     * - En producción, usar Level.NONE o Level.BASIC
     *
     * TIMEOUTS
     * --------
     * Los timeouts evitan que la app se quede colgada esperando una respuesta.
     *
     * - connectTimeout: Tiempo máximo para establecer conexión TCP
     * - readTimeout: Tiempo máximo para recibir datos una vez conectado
     * - writeTimeout: Tiempo máximo para enviar datos (body del request)
     *
     * ¿POR QUÉ 15 SEGUNDOS?
     * - Mejora la UX: El usuario no espera demasiado para ver un error
     * - Balance: Suficiente para conexiones lentas, no demasiado largo
     * - Móviles: Las conexiones móviles son más rápidas que antes
     * - API Amiibo: Es una API rápida, 15s es más que suficiente
     *
     * En APIs muy lentas (ej: generación de IA), considera 30-60 segundos.
     */
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // BODY: Loguea todo (headers + body)
            // En producción cambiar a BASIC o NONE
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)  // Tiempo para establecer conexión
            .readTimeout(15, TimeUnit.SECONDS)     // Tiempo para leer respuesta
            .writeTimeout(15, TimeUnit.SECONDS)    // Tiempo para enviar request
            .build()
    }

    // =========================================================================
    // NETWORKING - Retrofit (API Service)
    // =========================================================================
    /**
     * Instancia de Retrofit configurada para la API de Amiibo.
     *
     * Componentes:
     * - baseUrl: URL base de la API
     * - client: OkHttpClient con interceptors
     * - converterFactory: Convierte JSON <-> Kotlin objects
     *
     * asConverterFactory(): Extensión de kotlinx-serialization-converter
     * - Usa nuestro objeto Json configurado
     * - "application/json" indica el Content-Type
     */
    single {
        Retrofit.Builder()
            .baseUrl(AmiiboApiService.BASE_URL)
            .client(get())  // get() obtiene el OkHttpClient definido arriba
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    /**
     * Servicio de API de Amiibo.
     *
     * Retrofit.create() genera la implementación de la interfaz.
     * Cada método de la interfaz se convierte en una llamada HTTP real.
     */
    single<AmiiboApiService> {
        get<Retrofit>().create(AmiiboApiService::class.java)
    }

    // =========================================================================
    // DATABASE - Room
    // =========================================================================
    /**
     * Base de datos Room (SQLite).
     *
     * single { }: DEBE ser singleton porque:
     * - Crear instancias es costoso (I/O)
     * - Múltiples instancias causan problemas de concurrencia
     * - Room maneja threading internamente
     *
     * Room.databaseBuilder():
     * - androidContext(): Contexto de la aplicación (Koin lo provee)
     * - AmiiboDatabase::class.java: Clase de la base de datos
     * - DATABASE_NAME: Nombre del archivo SQLite
     *
     * =========================================================================
     * FALLBACK TO DESTRUCTIVE MIGRATION
     * =========================================================================
     *
     * ⚠️ SOLO PARA DESARROLLO - NO USAR EN PRODUCCIÓN
     *
     * fallbackToDestructiveMigration(dropAllTables = true):
     * - Si la versión de DB cambia y no hay migración definida, BORRA TODO
     * - Los datos del usuario se PIERDEN al actualizar la app
     * - Útil durante desarrollo para iterar rápidamente
     *
     * ¿QUÉ PASA EN PRODUCCIÓN?
     * ------------------------
     * En producción, cuando cambias el schema de la BD, debes:
     *
     * 1. MIGRACIÓN MANUAL (recomendado):
     *    ```kotlin
     *    val MIGRATION_1_2 = object : Migration(1, 2) {
     *        override fun migrate(db: SupportSQLiteDatabase) {
     *            db.execSQL("ALTER TABLE amiibos ADD COLUMN favorite INTEGER DEFAULT 0")
     *        }
     *    }
     *
     *    Room.databaseBuilder(...)
     *        .addMigrations(MIGRATION_1_2)
     *        .build()
     *    ```
     *
     * 2. AUTO-MIGRATION (Room 2.4+):
     *    ```kotlin
     *    @Database(
     *        version = 2,
     *        autoMigrations = [
     *            AutoMigration(from = 1, to = 2)
     *        ]
     *    )
     *    ```
     *
     * VER MÁS: https://developer.android.com/training/data-storage/room/migrating-db-versions
     * =========================================================================
     */
    single {
        Room.databaseBuilder(
            androidContext(),
            AmiiboDatabase::class.java,
            AmiiboDatabase.DATABASE_NAME
        )
            // ⚠️ SOLO DESARROLLO: En producción usar addMigrations()
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    /**
     * DAO de Amiibos.
     *
     * El DAO se obtiene de la instancia de Database.
     * Room genera la implementación automáticamente.
     */
    single {
        get<AmiiboDatabase>().amiiboDao()
    }

    // =========================================================================
    // REPOSITORY
    // =========================================================================
    /**
     * Repositorio que combina datos locales y remotos.
     *
     * Dependencias:
     * - AmiiboDao (get()): Para operaciones de base de datos
     * - AmiiboApiService (get()): Para llamadas a la API
     *
     * single { }: Un repositorio para toda la app
     */
    single {
        AmiiboRepository(
            amiiboDao = get(),
            amiiboApiService = get()
        )
    }

    // =========================================================================
    // VIEWMODEL
    // =========================================================================
    /**
     * ViewModel para la pantalla de Amiibos.
     *
     * viewModel { }: DSL especial de Koin para ViewModels
     * - Integra con el lifecycle de Android
     * - Sobrevive a configuration changes (rotación)
     * - Se destruye cuando la Activity/Fragment se destruye permanentemente
     *
     * A diferencia de Hilt donde usas @HiltViewModel,
     * en Koin solo declaras el ViewModel aquí y lo inyectas con:
     * - by viewModel() en Activity/Fragment
     * - koinViewModel() en Compose
     */
    viewModel {
        AmiiboViewModel(repository = get())
    }

    /**
     * ViewModel para el detalle de un Amiibo.
     *
     * Este ViewModel requiere un parámetro (nombre del Amiibo) que se pasa
     * desde la navegación usando parametersOf().
     */
    viewModel { (amiiboName: String) ->
        AmiiboDetailViewModel(amiiboName = amiiboName, repository = get())
    }
}

/**
 * ============================================================================
 * NOTAS ADICIONALES SOBRE KOIN
 * ============================================================================
 *
 * 1. MÚLTIPLES MÓDULOS:
 *    ```kotlin
 *    val networkModule = module { ... }
 *    val databaseModule = module { ... }
 *    val viewModelModule = module { ... }
 *
 *    // En Application:
 *    startKoin {
 *        modules(networkModule, databaseModule, viewModelModule)
 *    }
 *    ```
 *
 * 2. QUALIFIERS (múltiples implementaciones del mismo tipo):
 *    ```kotlin
 *    single(named("production")) { ProductionApi() }
 *    single(named("mock")) { MockApi() }
 *
 *    // Uso:
 *    val api: Api = get(named("production"))
 *    ```
 *
 * 3. SCOPES (dependencias con lifecycle limitado):
 *    ```kotlin
 *    scope<MyActivity> {
 *        scoped { ScopedDependency() }
 *    }
 *    ```
 *
 * 4. PARÁMETROS EN RUNTIME:
 *    ```kotlin
 *    factory { (id: String) -> DetailViewModel(id, get()) }
 *
 *    // Uso:
 *    val vm: DetailViewModel by viewModel { parametersOf("123") }
 *    ```
 *
 * ============================================================================
 */
