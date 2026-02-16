package com.curso.android.module3.amiibo.data.remote.model

import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ============================================================================
 * AMIIBO DTOs - Data Transfer Objects (Red)
 * ============================================================================
 *
 * Los DTOs (Data Transfer Objects) representan la estructura del JSON de la API.
 * Son diferentes de las Entities porque:
 * 1. Reflejan exactamente la estructura del JSON (anidamiento, nombres de campos)
 * 2. Pueden tener campos que no necesitamos guardar localmente
 * 3. Se transforman a Entities antes de guardarse
 *
 * API RESPONSE STRUCTURE:
 * ----------------------
 * {
 *   "amiibo": [
 *     {
 *       "amiiboSeries": "Super Smash Bros.",
 *       "character": "Mario",
 *       "gameSeries": "Super Mario",
 *       "head": "00000000",
 *       "image": "https://...",
 *       "name": "Mario",
 *       "release": { ... },
 *       "tail": "00000002",
 *       "type": "Figure"
 *     },
 *     ...
 *   ]
 * }
 *
 * KOTLINX SERIALIZATION:
 * ---------------------
 * @Serializable: Genera código de serialización/deserialización en compilación
 * @SerialName: Mapea nombres de JSON a nombres de propiedades Kotlin
 *
 * Ventajas sobre Gson/Moshi:
 * - Nativo de Kotlin (null safety, default values)
 * - Generación de código en compilación (más rápido en runtime)
 * - Soporte para sealed classes y value classes
 *
 * ============================================================================
 */

/**
 * Wrapper de la respuesta de la API.
 *
 * La API de Amiibo envuelve la lista en un objeto con campo "amiibo".
 * Este wrapper maneja ese nivel de anidamiento.
 *
 * JSON: { "amiibo": [...] }
 *         ↓ se mapea a
 * Kotlin: AmiiboResponse(amiibo = listOf(...))
 */
@Serializable
data class AmiiboResponse(
    /**
     * Lista de Amiibos devuelta por la API.
     *
     * @SerialName no es necesario aquí porque el nombre de la propiedad
     * coincide exactamente con el campo JSON ("amiibo").
     */
    val amiibo: List<AmiiboDto>
)

/**
 * DTO que representa un Amiibo individual de la API.
 *
 * Solo mapeamos los campos que necesitamos. Los demás campos del JSON
 * son ignorados automáticamente por kotlinx.serialization.
 *
 * CAMPOS DE LA API QUE IGNORAMOS:
 * - amiiboSeries: Serie del Amiibo (no del juego)
 * - type: Tipo (Figure, Card, Yarn)
 * - release: Fechas de lanzamiento por región
 * - games3DS, gamesSwitch, gamesWiiU: Juegos compatibles
 */
@Serializable
data class AmiiboDto(
    /**
     * Primera parte del ID único (8 caracteres hexadecimales)
     * Ejemplo: "00000000"
     */
    val head: String,

    /**
     * Segunda parte del ID único (8 caracteres hexadecimales)
     * Ejemplo: "00000002"
     */
    val tail: String,

    /**
     * Nombre del Amiibo
     * Ejemplo: "Mario", "Link"
     */
    val name: String,

    /**
     * Serie del juego al que pertenece
     * Ejemplo: "Super Mario", "The Legend of Zelda"
     *
     * @SerialName: El JSON usa "gameSeries" (camelCase), que coincide,
     * pero lo incluimos para ser explícitos.
     */
    @SerialName("gameSeries")
    val gameSeries: String,

    /**
     * URL de la imagen del Amiibo
     * Ejemplo: "https://raw.githubusercontent.com/.../image.png"
     */
    val image: String
)

/**
 * ============================================================================
 * FUNCIÓN DE MAPEO: DTO -> Entity
 * ============================================================================
 *
 * Convierte un AmiiboDto (datos de red) a AmiiboEntity (datos locales).
 *
 * ¿POR QUÉ SEPARAR DTO Y ENTITY?
 * -----------------------------
 * 1. Desacoplamiento: Cambios en la API no afectan la base de datos
 * 2. Flexibilidad: Podemos transformar/combinar datos durante el mapeo
 * 3. Claridad: Cada clase tiene una responsabilidad clara
 *
 * El ID se construye combinando head + tail de la API.
 * Esto garantiza unicidad porque la API usa esta combinación como identificador.
 */
fun AmiiboDto.toEntity(): AmiiboEntity {
    return AmiiboEntity(
        // Combinamos head y tail para crear un ID único
        // Formato: "00000000-00000002"
        id = "$head-$tail",
        name = name,
        gameSeries = gameSeries,
        imageUrl = image
    )
}

/**
 * Extensión para convertir una lista completa de DTOs a Entities.
 * Útil cuando procesamos la respuesta de la API.
 */
fun List<AmiiboDto>.toEntities(): List<AmiiboEntity> {
    return map { it.toEntity() }
}

/**
 * ============================================================================
 * NOTAS ADICIONALES SOBRE KOTLINX SERIALIZATION
 * ============================================================================
 *
 * 1. CAMPOS OPCIONALES (nullables con valor por defecto):
 *    ```kotlin
 *    @Serializable
 *    data class Example(
 *        val required: String,
 *        val optional: String? = null  // Si no está en JSON, usa null
 *    )
 *    ```
 *
 * 2. IGNORAR CAMPOS:
 *    ```kotlin
 *    @Serializable
 *    data class Example(
 *        val name: String,
 *        @Transient
 *        val computed: Int = 0  // No se serializa/deserializa
 *    )
 *    ```
 *
 * 3. ENUMS:
 *    ```kotlin
 *    @Serializable
 *    enum class Type {
 *        @SerialName("Figure") FIGURE,
 *        @SerialName("Card") CARD,
 *        @SerialName("Yarn") YARN
 *    }
 *    ```
 *
 * 4. POLYMORPHISM (sealed classes):
 *    ```kotlin
 *    @Serializable
 *    sealed class Response {
 *        @Serializable
 *        data class Success(val data: String) : Response()
 *        @Serializable
 *        data class Error(val message: String) : Response()
 *    }
 *    ```
 *
 * ============================================================================
 */
