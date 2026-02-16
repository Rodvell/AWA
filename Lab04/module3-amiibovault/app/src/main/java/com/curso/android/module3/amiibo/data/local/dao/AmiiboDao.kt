package com.curso.android.module3.amiibo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.curso.android.module3.amiibo.data.local.entity.AmiiboDetailEntity
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import kotlinx.coroutines.flow.Flow

/**
 * ============================================================================
 * AMIIBO DAO - Data Access Object (Room)
 * ============================================================================
 *
 * Un DAO (Data Access Object) define las operaciones de base de datos.
 * Room genera automáticamente la implementación en tiempo de compilación (KSP).
 *
 * CARACTERÍSTICAS CLAVE:
 * ---------------------
 * 1. Es una interfaz (Room genera la implementación)
 * 2. Cada método representa una operación SQL
 * 3. Type-safe: errores SQL se detectan en compilación
 * 4. Soporta Coroutines (suspend functions) y Flow
 *
 * FLOW VS SUSPEND:
 * ----------------
 * - Flow<T>: Observa cambios continuamente (reactivo)
 *   → Ideal para la UI que necesita actualizaciones automáticas
 *
 * - suspend fun: Operación única (one-shot)
 *   → Ideal para inserts, updates, deletes
 *
 * ============================================================================
 */
@Dao
interface AmiiboDao {

    /**
     * =========================================================================
     * QUERY: Obtener todos los Amiibos
     * =========================================================================
     *
     * @Query: Define una consulta SQL personalizada
     *
     * RETORNA Flow<List<AmiiboEntity>>:
     * - Flow es un stream reactivo de Kotlin Coroutines
     * - Cada vez que la tabla cambia, Flow emite una nueva lista
     * - La UI puede observar este Flow y actualizarse automáticamente
     * - NO es necesario llamar a este método de nuevo después de un insert
     *
     * ORDER BY name ASC: Ordena alfabéticamente por nombre
     *
     * EJEMPLO DE USO EN VIEWMODEL:
     * ```kotlin
     * val amiibos: StateFlow<List<AmiiboEntity>> = dao.getAllAmiibos()
     *     .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
     * ```
     */
    @Query("SELECT * FROM amiibos ORDER BY name ASC")
    fun getAllAmiibos(): Flow<List<AmiiboEntity>>

    /**
     * =========================================================================
     * INSERT: Insertar lista de Amiibos
     * =========================================================================
     *
     * @Insert: Genera automáticamente el INSERT INTO statement
     *
     * OnConflictStrategy.REPLACE:
     * - Si ya existe un registro con el mismo PrimaryKey, lo REEMPLAZA
     * - Alternativas:
     *   - IGNORE: No hace nada si hay conflicto
     *   - ABORT: Lanza excepción si hay conflicto
     *   - ROLLBACK: Hace rollback de la transacción
     *
     * suspend: Es una función suspendible (Coroutine)
     * - Se ejecuta en un hilo de background automáticamente
     * - No bloquea el hilo principal
     *
     * NOTA: Room maneja automáticamente el threading para suspend functions
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(amiibos: List<AmiiboEntity>)

    /**
     * =========================================================================
     * DELETE: Eliminar todos los Amiibos
     * =========================================================================
     *
     * @Query con DELETE: Elimina registros que cumplan la condición
     * Sin WHERE: Elimina TODOS los registros
     *
     * ¿Por qué eliminar todo antes de insertar?
     * - Asegura consistencia con los datos de la API
     * - Elimina Amiibos que ya no existen en la API
     * - Evita datos huérfanos o desactualizados
     *
     * ALTERNATIVA: Sincronización incremental
     * - Más eficiente para grandes datasets
     * - Requiere tracking de cambios (timestamps, versiones)
     */
    @Query("DELETE FROM amiibos")
    suspend fun deleteAll()

    /**
     * =========================================================================
     * TRANSACTION: Reemplazar todos los Amiibos atómicamente
     * =========================================================================
     *
     * @Transaction: Ejecuta múltiples operaciones como una unidad atómica
     * - Si algo falla, se hace rollback de TODO
     * - Garantiza consistencia de datos
     *
     * Flujo:
     * 1. Elimina todos los registros existentes
     * 2. Inserta los nuevos registros
     *
     * Si el paso 2 falla, el paso 1 también se revierte.
     *
     * PATRÓN OFFLINE-FIRST:
     * Este método implementa la estrategia "refresh":
     * - Datos viejos se eliminan
     * - Datos nuevos de la API se insertan
     * - La UI observa los cambios automáticamente via Flow
     */
    @Transaction
    suspend fun replaceAll(amiibos: List<AmiiboEntity>) {
        deleteAll()
        insertAll(amiibos)
    }

    /**
     * =========================================================================
     * QUERY: Contar total de Amiibos
     * =========================================================================
     *
     * Útil para:
     * - Verificar si hay datos en cache
     * - Mostrar estadísticas al usuario
     * - Debugging
     *
     * Flow<Int>: Se actualiza automáticamente cuando cambia el count
     */
    @Query("SELECT COUNT(*) FROM amiibos")
    fun getCount(): Flow<Int>

    /**
     * =========================================================================
     * QUERY: Obtener Amiibos paginados
     * =========================================================================
     *
     * Implementa paginación del lado del cliente usando LIMIT y OFFSET.
     *
     * @param limit Número máximo de items a retornar
     * @param offset Número de items a saltar (para paginación)
     * @return Lista de Amiibos para la página actual
     *
     * Ejemplo:
     * - Página 1 (limit=20, offset=0): Items 1-20
     * - Página 2 (limit=20, offset=20): Items 21-40
     * - Página 3 (limit=20, offset=40): Items 41-60
     */
    @Query("SELECT * FROM amiibos ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getAmiibosPage(limit: Int, offset: Int): List<AmiiboEntity>

    @Query("""
    SELECT * FROM amiibos 
    WHERE name LIKE '%' || :query || '%'
    ORDER BY name ASC
    LIMIT :limit OFFSET :offset
""")
    suspend fun searchAmiibosPage(
        query: String,
        limit: Int,
        offset: Int
    ): List<AmiiboEntity>

    @Query("""
    SELECT COUNT(*) FROM amiibos
    WHERE name LIKE '%' || :query || '%'
""")
    suspend fun getSearchCount(query: String): Int

    /**
     * Obtiene el total de amiibos (para calcular si hay más páginas).
     */
    @Query("SELECT COUNT(*) FROM amiibos")
    suspend fun getTotalCount(): Int

    // =========================================================================
    // OPERACIONES PARA DETALLE DE AMIIBO
    // =========================================================================

    /**
     * Obtiene el detalle de un Amiibo por nombre.
     * Retorna null si no existe en la base de datos.
     */
    @Query("SELECT * FROM amiibo_details WHERE name = :name LIMIT 1")
    suspend fun getDetailByName(name: String): AmiiboDetailEntity?

    /**
     * Inserta o reemplaza el detalle de un Amiibo.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: AmiiboDetailEntity)
}

/**
 * ============================================================================
 * NOTAS ADICIONALES SOBRE DAOs
 * ============================================================================
 *
 * 1. QUERIES DINÁMICAS:
 *    ```kotlin
 *    @Query("SELECT * FROM amiibos WHERE name LIKE '%' || :search || '%'")
 *    fun search(search: String): Flow<List<AmiiboEntity>>
 *    ```
 *
 * 2. MÚLTIPLES PARÁMETROS:
 *    ```kotlin
 *    @Query("SELECT * FROM amiibos WHERE gameSeries = :series AND name = :name")
 *    suspend fun findBySeriesAndName(series: String, name: String): AmiiboEntity?
 *    ```
 *
 * 3. UPDATE:
 *    ```kotlin
 *    @Update
 *    suspend fun update(amiibo: AmiiboEntity)
 *    ```
 *
 * 4. DELETE ESPECÍFICO:
 *    ```kotlin
 *    @Delete
 *    suspend fun delete(amiibo: AmiiboEntity)
 *    ```
 *
 * ============================================================================
 */
