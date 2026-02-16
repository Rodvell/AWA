package com.curso.android.module3.amiibo.data.remote.api

import com.curso.android.module3.amiibo.data.remote.model.AmiiboDetailResponse
import com.curso.android.module3.amiibo.data.remote.model.AmiiboResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * ============================================================================
 * AMIIBO API SERVICE - Definición de Endpoints (Retrofit)
 * ============================================================================
 *
 * Esta interfaz define los endpoints de la API REST usando Retrofit.
 * Retrofit genera automáticamente la implementación en runtime.
 *
 * RETROFIT BASICS:
 * ----------------
 * - Es un cliente HTTP type-safe para Android/Java
 * - Convierte interfaces en implementaciones ejecutables
 * - Usa anotaciones para definir HTTP method, URL, parámetros, etc.
 *
 * ANOTACIONES HTTP:
 * -----------------
 * @GET: HTTP GET request
 * @POST: HTTP POST request
 * @PUT: HTTP PUT request
 * @DELETE: HTTP DELETE request
 * @PATCH: HTTP PATCH request
 *
 * PARÁMETROS:
 * -----------
 * @Path: Reemplaza {placeholder} en la URL
 * @Query: Agrega ?key=value a la URL
 * @Body: Cuerpo del request (para POST/PUT)
 * @Header: Header personalizado
 *
 * ============================================================================
 */
interface AmiiboApiService {

    /**
     * Obtiene la lista completa de todos los Amiibos.
     *
     * ENDPOINT: GET https://www.amiiboapi.com/api/amiibo/
     *
     * @GET(""): Usa la URL base definida al crear Retrofit
     * - La URL base es: https://www.amiiboapi.com/api/amiibo/
     * - El path relativo es vacío ("")
     * - URL final: https://www.amiiboapi.com/api/amiibo/
     *
     * suspend: Es una función suspendible (Coroutine)
     * - Retrofit ejecuta el request en un hilo de background
     * - Automáticamente parsea el JSON a AmiiboResponse
     *
     * RESPUESTA ESPERADA:
     * ```json
     * {
     *   "amiibo": [
     *     { "name": "Mario", "gameSeries": "Super Mario", ... },
     *     { "name": "Link", "gameSeries": "Legend of Zelda", ... },
     *     ...
     *   ]
     * }
     * ```
     *
     * MANEJO DE ERRORES:
     * - Si el request falla, Retrofit lanza una excepción
     * - El Repository debe manejar estas excepciones con try/catch
     *
     * @return AmiiboResponse con la lista de todos los Amiibos
     * @throws IOException si hay error de red
     * @throws HttpException si el servidor retorna error (4xx, 5xx)
     */
    @GET("amiibo")
    suspend fun getAllAmiibos(): AmiiboResponse

    /**
     * Obtiene el detalle de un Amiibo específico por nombre, incluyendo juegos compatibles.
     *
     * ENDPOINT: GET https://www.amiiboapi.com/api/amiibo?name={name}&showgames
     *
     * @param name Nombre exacto del Amiibo
     * @return AmiiboDetailResponse con información completa incluyendo juegos
     */
    @GET("amiibo")
    suspend fun getAmiiboDetail(
        @Query("name") name: String,
        @Query("showgames") showGames: String = ""
    ): AmiiboDetailResponse

    companion object {
        /**
         * URL base de la API de Amiibo.
         *
         * La URL base incluye hasta "/api/" y cada método especifica
         * el endpoint específico (ej: "amiibo").
         */
        //const val BASE_URL = "https://www.amiiboapi.com/api/"
        const val BASE_URL = "https://amiiboapi-7eg6.onrender.com/api/"
    }
}

/**
 * ============================================================================
 * EJEMPLOS ADICIONALES DE RETROFIT
 * ============================================================================
 *
 * 1. PATH PARAMETERS:
 *    ```kotlin
 *    // URL: /api/amiibo?id=00000000-00000002
 *    @GET("api/amiibo")
 *    suspend fun getAmiiboById(@Query("id") id: String): AmiiboResponse
 *    ```
 *
 * 2. MÚLTIPLES QUERY PARAMS:
 *    ```kotlin
 *    // URL: /api/amiibo?name=mario&gameseries=super%20mario
 *    @GET("api/amiibo")
 *    suspend fun searchAmiibos(
 *        @Query("name") name: String?,
 *        @Query("gameseries") gameSeries: String?
 *    ): AmiiboResponse
 *    ```
 *
 * 3. HEADERS:
 *    ```kotlin
 *    @Headers("Accept: application/json")
 *    @GET("api/amiibo")
 *    suspend fun getAllAmiibosWithHeader(): AmiiboResponse
 *
 *    // O dinámicamente:
 *    @GET("api/amiibo")
 *    suspend fun getWithAuth(@Header("Authorization") token: String): AmiiboResponse
 *    ```
 *
 * 4. POST CON BODY:
 *    ```kotlin
 *    @POST("api/favorites")
 *    suspend fun addFavorite(@Body favorite: FavoriteRequest): FavoriteResponse
 *    ```
 *
 * 5. FORM DATA:
 *    ```kotlin
 *    @FormUrlEncoded
 *    @POST("api/login")
 *    suspend fun login(
 *        @Field("username") username: String,
 *        @Field("password") password: String
 *    ): LoginResponse
 *    ```
 *
 * ============================================================================
 */
