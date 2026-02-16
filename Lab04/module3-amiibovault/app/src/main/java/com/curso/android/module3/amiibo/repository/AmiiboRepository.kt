package com.curso.android.module3.amiibo.repository

import android.database.sqlite.SQLiteException
import com.curso.android.module3.amiibo.data.local.dao.AmiiboDao
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.data.remote.api.AmiiboApiService
import com.curso.android.module3.amiibo.data.local.entity.AmiiboDetailEntity
import com.curso.android.module3.amiibo.data.remote.model.AmiiboDetail
import com.curso.android.module3.amiibo.data.remote.model.toDetail
import com.curso.android.module3.amiibo.data.remote.model.toDomainModel
import com.curso.android.module3.amiibo.data.remote.model.toEntities
import com.curso.android.module3.amiibo.data.remote.model.toEntity
import com.curso.android.module3.amiibo.domain.error.AmiiboError
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import java.io.IOException

/**
 * ============================================================================
 * AMIIBO REPOSITORY - Patrón Repository (Offline-First)
 * ============================================================================
 *
 * El Repository es el intermediario entre las fuentes de datos y el ViewModel.
 * Implementa el patrón "Single Source of Truth" (única fuente de verdad):
 *
 *           ┌─────────────────────────────────────────────────────────┐
 *           │                     REPOSITORY                          │
 *           │                                                         │
 *           │   ┌─────────────┐         ┌─────────────┐             │
 *   API ──>│   │   REMOTE    │ ──────> │   LOCAL     │ ────> UI     │
 *           │   │  (Retrofit) │         │   (Room)    │             │
 *           │   └─────────────┘         └─────────────┘             │
 *           │                                                         │
 *           │   1. Fetch from API                                    │
 *           │   2. Save to DB                                        │
 *           │   3. UI observes DB (Flow)                             │
 *           └─────────────────────────────────────────────────────────┘
 *
 * PRINCIPIO OFFLINE-FIRST:
 * -----------------------
 * 1. La BASE DE DATOS es la única fuente de verdad
 * 2. La UI SIEMPRE lee de la base de datos (nunca directamente de la red)
 * 3. Los datos de red se GUARDAN en la DB antes de mostrarse
 * 4. La app funciona sin conexión (muestra datos en cache)
 *
 * BENEFICIOS:
 * -----------
 * - Experiencia offline: Los datos persisten entre sesiones
 * - Consistencia: Una sola fuente de datos para la UI
 * - Performance: Lecturas rápidas desde DB local
 * - Simplicidad: El ViewModel solo observa el Flow de Room
 *
 * ============================================================================
 */
class AmiiboRepository(
    private val amiiboDao: AmiiboDao,
    private val amiiboApiService: AmiiboApiService
) {

    /**
     * =========================================================================
     * OBSERVAR AMIIBOS (FLUJO REACTIVO)
     * =========================================================================
     *
     * Expone el Flow de Room para que el ViewModel pueda observar.
     *
     * Flow<List<AmiiboEntity>>:
     * - Emite automáticamente cuando los datos cambian
     * - El ViewModel convierte esto a StateFlow para la UI
     * - Nunca termina (es un stream infinito)
     *
     * FLUJO DE DATOS:
     * Room DB ──> Flow ──> ViewModel ──> StateFlow ──> Compose UI
     *
     * Cuando llamamos a refreshAmiibos(), Room detecta los cambios
     * y este Flow emite automáticamente la nueva lista.
     */
    fun observeAmiibos(): Flow<List<AmiiboEntity>> {
        return amiiboDao.getAllAmiibos()
    }

    /**
     * =========================================================================
     * REFRESCAR AMIIBOS (SINCRONIZACIÓN)
     * =========================================================================
     *
     * Descarga datos frescos de la API y los guarda en la base de datos.
     *
     * FLUJO:
     * 1. Llama a la API (Retrofit)
     * 2. Convierte DTOs a Entities
     * 3. Reemplaza todos los datos en Room (transacción atómica)
     * 4. Room notifica automáticamente al Flow observeAmiibos()
     *
     * MANEJO DE ERRORES TIPADOS:
     * -------------------------
     * En lugar de propagar excepciones genéricas, este método lanza
     * errores específicos usando la sealed class AmiiboError.
     *
     * Esto permite al ViewModel/UI:
     * - Mostrar mensajes específicos por tipo de error
     * - Decidir si reintentar (Network) o no (Parse)
     * - Reportar a analytics según el tipo
     *
     * suspend: Función suspendible (ejecutar en Coroutine)
     *
     * @throws AmiiboError.Network si hay error de conexión
     * @throws AmiiboError.Parse si el JSON no se puede parsear
     * @throws AmiiboError.Database si Room falla al guardar
     * @throws AmiiboError.Unknown para errores no categorizados
     */
    /**
     * Refresca todos los amiibos desde la API.
     *
     * Descarga TODOS los amiibos y los guarda en Room.
     * La paginación se hace localmente desde la base de datos.
     */
    suspend fun refreshAmiibos() {
        try {
            // 1. Obtener TODOS los datos de la API
            // Retrofit ejecuta esto en un hilo de background automáticamente
            val response = amiiboApiService.getAllAmiibos()

            // 2. Convertir DTOs a Entities (todos los resultados)
            // Usamos la función de extensión toEntities() definida en AmiiboDto.kt
            val entities = response.amiibo.toEntities()

            // 3. Guardar en la base de datos (reemplaza todo)
            // replaceAll() es una @Transaction que:
            //   a) Elimina todos los registros existentes
            //   b) Inserta los nuevos registros
            // Si algo falla, se hace rollback automático
            try {
                amiiboDao.replaceAll(entities)
            } catch (e: SQLiteException) {
                // Error de base de datos (disco lleno, corrupción, etc.)
                throw AmiiboError.Database(cause = e)
            }

            // 4. NO necesitamos retornar nada
            // Room notifica automáticamente al Flow de observeAmiibos()

        } catch (e: AmiiboError) {
            // Re-lanzar errores ya tipados (ej: Database del catch interno)
            throw e
        } catch (e: IOException) {
            // Error de red: sin conexión, timeout, DNS, etc.
            // IOException es la clase base para errores de I/O en Java
            throw AmiiboError.Network(cause = e)
        } catch (e: SerializationException) {
            // Error de parsing: JSON malformado o campos faltantes
            // kotlinx.serialization lanza esto cuando falla el parsing
            throw AmiiboError.Parse(cause = e)
        } catch (e: Exception) {
            // Catch-all para errores inesperados
            // Siempre incluir el error original como causa para debugging
            throw AmiiboError.Unknown(cause = e)
        }
    }

    /**
     * =========================================================================
     * PAGINACIÓN - Obtener página de Amiibos
     * =========================================================================
     *
     * Carga una página específica de amiibos desde la base de datos local.
     *
     * @param page Número de página (empezando en 0)
     * @param pageSize Tamaño de página
     * @return Lista de amiibos para esa página
     */
    suspend fun getAmiibosPage(page: Int, pageSize: Int): List<AmiiboEntity> {
        val offset = page * pageSize
        return amiiboDao.getAmiibosPage(limit = pageSize, offset = offset)
    }

    /**
     * Obtiene el total de amiibos en la base de datos.
     */
    suspend fun getTotalCount(): Int {
        return amiiboDao.getTotalCount()
    }

    /**
     * Verifica si hay más páginas disponibles.
     */
    suspend fun hasMorePages(currentPage: Int, pageSize: Int): Boolean {
        val total = getTotalCount()
        val loaded = (currentPage + 1) * pageSize
        return loaded < total
    }

    companion object {
        /** Tamaño de página por defecto */
        const val DEFAULT_PAGE_SIZE = 20

        /** Opciones de tamaño de página disponibles */
        val PAGE_SIZE_OPTIONS = listOf(20, 50, 100)
    }

    /**
     * =========================================================================
     * OBTENER DETALLE DE UN AMIIBO (OFFLINE-FIRST)
     * =========================================================================
     *
     * Obtiene información detallada de un Amiibo. Primero revisa la base de
     * datos local. Si no existe, lo obtiene de la API y lo guarda en cache.
     *
     * FLUJO:
     * 1. Buscar en Room por nombre
     * 2. Si existe -> retornar desde cache
     * 3. Si NO existe -> llamar API, guardar en Room, retornar
     *
     * @param name Nombre del Amiibo a consultar
     * @return AmiiboDetail con toda la información del Amiibo
     * @throws AmiiboError si hay error de red, parsing, base de datos o desconocido
     */
    suspend fun getAmiiboDetail(name: String): AmiiboDetail {
        try {
            // 1. Buscar en cache local
            val cachedDetail = try {
                amiiboDao.getDetailByName(name)
            } catch (e: SQLiteException) {
                throw AmiiboError.Database(cause = e)
            }

            if (cachedDetail != null) {
                // Retornar desde cache
                return cachedDetail.toDomainModel()
            }

            // 2. No está en cache, obtener de la API
            val response = amiiboApiService.getAmiiboDetail(name)
            val detail = response.amiibo.first().toDetail()

            // 3. Guardar en cache para futuras consultas
            try {
                amiiboDao.insertDetail(detail.toEntity())
            } catch (e: SQLiteException) {
                throw AmiiboError.Database(cause = e)
            }

            return detail

        } catch (e: AmiiboError) {
            throw e
        } catch (e: IOException) {
            throw AmiiboError.Network(cause = e)
        } catch (e: SerializationException) {
            throw AmiiboError.Parse(cause = e)
        } catch (e: NoSuchElementException) {
            // first() lanza esto si la lista está vacía (Amiibo no encontrado)
            throw AmiiboError.Parse(
                message = "No se encontró el Amiibo '$name'",
                cause = e
            )
        } catch (e: Exception) {
            throw AmiiboError.Unknown(cause = e)
        }
    }

    /**
     * =========================================================================
     * OBTENER CONTEO (UTILIDAD)
     * =========================================================================
     *
     * Útil para verificar si hay datos en cache o mostrar estadísticas.
     */
    fun getAmiiboCount(): Flow<Int> {
        return amiiboDao.getCount()
    }
}

/**
 * ============================================================================
 * ESTRATEGIAS DE SINCRONIZACIÓN ALTERNATIVAS
 * ============================================================================
 *
 * 1. REFRESH ON DEMAND (implementada aquí):
 *    - El usuario o la app decide cuándo sincronizar
 *    - Simple y predecible
 *    - Ideal para datos que no cambian frecuentemente
 *
 * 2. CACHE THEN NETWORK:
 *    ```kotlin
 *    fun getAmiibos(): Flow<List<AmiiboEntity>> = flow {
 *        // Primero emite cache
 *        emitAll(amiiboDao.getAllAmiibos().take(1))
 *        // Luego actualiza desde red
 *        try {
 *            refreshAmiibos()
 *        } catch (e: Exception) {
 *            // Ignora error si hay cache
 *        }
 *    }
 *    ```
 *
 * 3. STALE WHILE REVALIDATE:
 *    ```kotlin
 *    suspend fun getAmiibosWithRevalidation(): Flow<List<AmiiboEntity>> {
 *        // Lanza refresh en background sin bloquear
 *        coroutineScope {
 *            launch { runCatching { refreshAmiibos() } }
 *        }
 *        // Retorna datos de cache inmediatamente
 *        return amiiboDao.getAllAmiibos()
 *    }
 *    ```
 *
 * 4. TIME-BASED CACHE:
 *    ```kotlin
 *    suspend fun getAmiibosIfStale(maxAgeMinutes: Int = 30) {
 *        val lastSync = preferences.getLastSyncTime()
 *        val now = System.currentTimeMillis()
 *        if (now - lastSync > maxAgeMinutes * 60 * 1000) {
 *            refreshAmiibos()
 *            preferences.setLastSyncTime(now)
 *        }
 *    }
 *    ```
 *
 * 5. INCREMENTAL SYNC (para datasets grandes):
 *    ```kotlin
 *    suspend fun syncIncremental() {
 *        val lastModified = preferences.getLastModified()
 *        val newItems = api.getAmiibosModifiedSince(lastModified)
 *        amiiboDao.upsertAll(newItems.toEntities())  // Update or Insert
 *    }
 *    ```
 *
 * ============================================================================
 */
    suspend fun searchAmiibosPage(
        query: String,
        page: Int,
        pageSize: Int
    ): List<AmiiboEntity> {
        val offset = page * pageSize
        return amiiboDao.searchAmiibosPage(query, pageSize, offset)
    }

    suspend fun getSearchCount(query: String): Int {
        return amiiboDao.getSearchCount(query)
    }

    suspend fun hasMoreSearchPages(
        query: String,
        currentPage: Int,
        pageSize: Int
    ): Boolean {
        val total = getSearchCount(query)
        val loaded = (currentPage + 1) * pageSize
        return loaded < total
    }

