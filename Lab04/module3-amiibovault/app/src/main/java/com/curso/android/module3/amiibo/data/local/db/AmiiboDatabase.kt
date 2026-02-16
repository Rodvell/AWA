package com.curso.android.module3.amiibo.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.curso.android.module3.amiibo.data.local.dao.AmiiboDao
import com.curso.android.module3.amiibo.data.local.entity.AmiiboDetailEntity
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity

/**
 * ============================================================================
 * AMIIBO DATABASE - Configuración de Room Database
 * ============================================================================
 *
 * Esta clase abstracta define la configuración de la base de datos Room.
 * Room genera automáticamente la implementación en tiempo de compilación.
 *
 * ANOTACIÓN @Database:
 * --------------------
 * - entities: Lista de todas las tablas (@Entity) de la base de datos
 * - version: Versión del esquema (incrementar cuando cambias estructura)
 * - exportSchema: Si true, genera un archivo JSON del esquema (útil para migraciones)
 *
 * PATRÓN SINGLETON:
 * -----------------
 * La base de datos DEBE ser un singleton porque:
 * 1. Crear instancias es costoso (I/O)
 * 2. Múltiples instancias pueden causar problemas de concurrencia
 * 3. Room maneja el threading internamente
 *
 * En este proyecto, Koin maneja el singleton (single { ... })
 *
 * ============================================================================
 */
@Database(
    entities = [AmiiboEntity::class, AmiiboDetailEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AmiiboDatabase : RoomDatabase() {

    /**
     * Método abstracto que retorna el DAO de Amiibos.
     *
     * Room genera automáticamente la implementación que:
     * 1. Crea una instancia del DAO generado
     * 2. Lo conecta con esta base de datos
     * 3. Maneja el ciclo de vida del DAO
     *
     * NOTA: No necesitas implementar este método, Room lo hace por ti.
     */
    abstract fun amiiboDao(): AmiiboDao

    /**
     * Companion object para constantes relacionadas con la base de datos.
     * El nombre se usa al crear la instancia con Room.databaseBuilder()
     */
    companion object {
        /**
         * Nombre del archivo de la base de datos SQLite.
         * Se almacena en: /data/data/[package]/databases/amiibo_database
         */
        const val DATABASE_NAME = "amiibo_database"
    }
}

/**
 * ============================================================================
 * NOTAS SOBRE MIGRACIONES DE ROOM
 * ============================================================================
 *
 * Cuando cambias el esquema de la base de datos (agregar/eliminar columnas,
 * cambiar tipos, etc.), DEBES incrementar la versión y proporcionar una migración.
 *
 * OPCIÓN 1: Migración manual
 * ```kotlin
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         database.execSQL("ALTER TABLE amiibos ADD COLUMN favorite INTEGER DEFAULT 0")
 *     }
 * }
 *
 * Room.databaseBuilder(context, AmiiboDatabase::class.java, DATABASE_NAME)
 *     .addMigrations(MIGRATION_1_2)
 *     .build()
 * ```
 *
 * OPCIÓN 2: Migración destructiva (pierde datos)
 * ```kotlin
 * Room.databaseBuilder(context, AmiiboDatabase::class.java, DATABASE_NAME)
 *     .fallbackToDestructiveMigration()
 *     .build()
 * ```
 *
 * OPCIÓN 3: Auto-migration (Room 2.4+)
 * ```kotlin
 * @Database(
 *     entities = [AmiiboEntity::class],
 *     version = 2,
 *     autoMigrations = [
 *         AutoMigration(from = 1, to = 2)
 *     ]
 * )
 * ```
 *
 * ============================================================================
 */
