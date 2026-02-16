package com.curso.android.module3.amiibo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ============================================================================
 * AMIIBO ENTITY - Modelo de Datos Local (Room)
 * ============================================================================
 *
 * Una Entity representa una tabla en la base de datos SQLite.
 * Room usa esta clase para:
 * 1. Crear la estructura de la tabla (CREATE TABLE)
 * 2. Mapear filas de la tabla a objetos Kotlin
 * 3. Generar queries type-safe
 *
 * ANOTACIONES CLAVE:
 * -----------------
 * @Entity: Marca esta clase como una tabla de Room
 *   - tableName: Nombre de la tabla en SQLite (opcional, usa el nombre de la clase si no se especifica)
 *
 * @PrimaryKey: Marca el campo como clave primaria
 *   - Cada Entity DEBE tener al menos una @PrimaryKey
 *   - autoGenerate = true generaría IDs automáticos (no lo usamos aquí porque la API nos da el ID)
 *
 * PATRÓN OFFLINE-FIRST:
 * --------------------
 * Esta Entity es la "Single Source of Truth" (única fuente de verdad).
 * La UI SIEMPRE lee de esta tabla, nunca directamente de la red.
 * Flujo: API -> Entity -> UI
 *
 * ============================================================================
 */
@Entity(tableName = "amiibos")
data class AmiiboEntity(
    /**
     * ID único del Amiibo (viene de la API como "head" + "tail")
     * Ejemplo: "00000000-00000002"
     *
     * Usamos el ID de la API como PrimaryKey porque:
     * - Es único y estable
     * - Permite detectar duplicados al sincronizar
     * - Facilita actualizaciones (REPLACE strategy)
     */
    @PrimaryKey
    val id: String,

    /**
     * Nombre del personaje Amiibo
     * Ejemplo: "Mario", "Link", "Pikachu"
     */
    val name: String,

    /**
     * Serie del juego al que pertenece
     * Ejemplo: "Super Mario", "The Legend of Zelda", "Pokémon"
     */
    val gameSeries: String,

    /**
     * URL de la imagen del Amiibo
     * La API de Amiibo proporciona imágenes en formato PNG
     * Coil usará esta URL para cargar la imagen
     */
    val imageUrl: String
)

/**
 * ============================================================================
 * NOTAS ADICIONALES SOBRE ROOM ENTITIES
 * ============================================================================
 *
 * 1. TIPOS SOPORTADOS:
 *    - Primitivos: Int, Long, Float, Double, Boolean
 *    - String
 *    - ByteArray
 *    - Para otros tipos, usar @TypeConverter
 *
 * 2. COLUMNAS OPCIONALES:
 *    - Por defecto, cada propiedad es una columna
 *    - Usar @Ignore para excluir propiedades
 *    - Usar @ColumnInfo(name = "custom_name") para nombres personalizados
 *
 * 3. RELACIONES:
 *    - @ForeignKey para claves foráneas
 *    - @Relation para queries con JOINs
 *    - @Embedded para objetos anidados
 *
 * 4. ÍNDICES:
 *    - @Entity(indices = [...]) para búsquedas más rápidas
 *    - Útil para campos que se usan frecuentemente en WHERE
 *
 * ============================================================================
 */
