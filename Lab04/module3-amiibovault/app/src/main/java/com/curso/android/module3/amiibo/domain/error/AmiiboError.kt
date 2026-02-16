package com.curso.android.module3.amiibo.domain.error

/**
 * =============================================================================
 * AMIIBO ERROR - Tipos de Errores Específicos
 * =============================================================================
 *
 * CONCEPTO: Sealed Class para Errores
 * -----------------------------------
 * Usar sealed class para errores proporciona:
 * 1. Type-safety: El compilador verifica que manejes todos los casos
 * 2. Exhaustividad: when() te obliga a cubrir todos los tipos
 * 3. Contexto: Cada tipo puede tener datos específicos
 * 4. Mensajes claros: Mejor UX con errores descriptivos
 *
 * ¿POR QUÉ NO USAR EXCEPCIONES GENÉRICAS?
 * ----------------------------------------
 * Una Exception genérica pierde contexto:
 * - ¿Fue un error de red? ¿De parsing? ¿De base de datos?
 * - El usuario no sabe qué hacer para solucionarlo
 * - Dificulta el debugging y analytics
 *
 * Con sealed class:
 * - Sabes exactamente qué falló
 * - Puedes mostrar mensajes específicos
 * - Puedes decidir si reintentar o no
 *
 * USO EN EL VIEWMODEL:
 * ```kotlin
 * try {
 *     repository.refreshAmiibos()
 * } catch (e: AmiiboError) {
 *     when (e) {
 *         is AmiiboError.Network -> showRetryButton()
 *         is AmiiboError.Parse -> reportToAnalytics()
 *         is AmiiboError.Database -> clearCacheAndRetry()
 *         is AmiiboError.Unknown -> logAndReport()
 *     }
 * }
 * ```
 *
 * =============================================================================
 */
sealed class AmiiboError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Error de red - No hay conexión o el servidor no responde
     *
     * Causas comunes:
     * - Sin conexión a internet
     * - Timeout al conectar
     * - DNS resolution failed
     * - SSL handshake failed
     *
     * Acción recomendada: Mostrar botón de reintentar
     */
    class Network(
        message: String = "Error de conexión. Verifica tu internet.",
        cause: Throwable? = null
    ) : AmiiboError(message, cause) {

        /**
         * @return true si el error es por timeout
         */
        fun isTimeout(): Boolean {
            return cause?.message?.contains("timeout", ignoreCase = true) == true
        }
    }

    /**
     * Error de parsing - El JSON no se pudo convertir
     *
     * Causas comunes:
     * - API cambió su formato de respuesta
     * - Campos requeridos faltantes
     * - Tipos de datos incorrectos
     *
     * Acción recomendada: Reportar a analytics, no reintentar
     */
    class Parse(
        message: String = "Error al procesar los datos del servidor.",
        cause: Throwable? = null
    ) : AmiiboError(message, cause)

    /**
     * Error de base de datos - Room falló al guardar/leer
     *
     * Causas comunes:
     * - Disco lleno
     * - Base de datos corrupta
     * - Conflicto de concurrencia
     *
     * Acción recomendada: Limpiar cache y reintentar
     */
    class Database(
        message: String = "Error al guardar los datos localmente.",
        cause: Throwable? = null
    ) : AmiiboError(message, cause)

    /**
     * Error desconocido - Algo inesperado ocurrió
     *
     * Catch-all para errores no categorizados.
     *
     * Acción recomendada: Log detallado + reportar a Crashlytics
     */
    class Unknown(
        message: String = "Ocurrió un error inesperado.",
        cause: Throwable? = null
    ) : AmiiboError(message, cause)
}

/**
 * Tipo de error simplificado para UI
 *
 * Útil para cuando solo necesitas el tipo sin los detalles.
 */
enum class ErrorType {
    NETWORK,
    PARSE,
    DATABASE,
    UNKNOWN;

    companion object {
        fun from(error: AmiiboError): ErrorType = when (error) {
            is AmiiboError.Network -> NETWORK
            is AmiiboError.Parse -> PARSE
            is AmiiboError.Database -> DATABASE
            is AmiiboError.Unknown -> UNKNOWN
        }
    }
}
