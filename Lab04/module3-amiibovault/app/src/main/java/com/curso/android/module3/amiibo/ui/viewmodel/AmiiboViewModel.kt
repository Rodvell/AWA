package com.curso.android.module3.amiibo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.domain.error.AmiiboError
import com.curso.android.module3.amiibo.domain.error.ErrorType
import com.curso.android.module3.amiibo.repository.AmiiboRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * AMIIBO UISTATE - Estado de la UI (Sealed Interface)
 * ============================================================================
 *
 * UiState representa todos los posibles estados de la pantalla.
 * Usar sealed interface garantiza que manejemos TODOS los casos en la UI.
 *
 * PATRÓN UISTATE:
 * ---------------
 * En lugar de tener múltiples variables separadas:
 * ```kotlin
 * // ❌ Antipatrón
 * val isLoading: Boolean
 * val error: String?
 * val data: List<Amiibo>?
 * ```
 *
 * Usamos un solo estado que representa TODAS las posibilidades:
 * ```kotlin
 * // ✅ Patrón UiState
 * sealed interface UiState {
 *     object Loading : UiState
 *     data class Success(val data: List<Amiibo>) : UiState
 *     data class Error(val message: String) : UiState
 * }
 * ```
 *
 * BENEFICIOS:
 * -----------
 * 1. Type-safe: El compilador verifica que manejemos todos los casos
 * 2. Exclusividad: Solo puede estar en UN estado a la vez
 * 3. Claridad: El código de UI es más legible con when exhaustivo
 * 4. Testeable: Fácil de verificar estados en tests
 *
 * SEALED INTERFACE VS SEALED CLASS:
 * ---------------------------------
 * - sealed interface: Más flexible, permite herencia múltiple
 * - sealed class: Puede tener constructor con parámetros comunes
 * - En Kotlin moderno, sealed interface es preferido
 *
 * ============================================================================
 * ------------------
 *
 *   ┌─────────────────────────────────────────────────────────────────┐
 *   │                                                                 │
 *   │    ┌──────────┐         ┌──────────────┐       ┌──────────┐   │
 *   │    │   VIEW   │ ◄────── │  VIEWMODEL   │ ◄──── │  MODEL   │   │
 *   │    │ (Compose)│         │              │       │ (Repo)   │   │
 *   │    └────┬─────┘         └──────────────┘       └──────────┘   │
 *   │         │                      ▲                              │
 *   │         │    User Actions      │                              │
 *   │         └──────────────────────┘                              │
 *   │                                                                 │
 *   └─────────────────────────────────────────────────────────────────┘
 *
 * FLUJO DE DATOS (Unidirectional Data Flow):
 * 1. UI observa uiState (StateFlow)
 * 2. Usuario hace acción → llama a función del ViewModel
 * 3. ViewModel actualiza el estado
 * 4. UI se recompone automáticamente con el nuevo estado
 *
 * ============================================================================
 */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        resetPagination()
    }

class AmiiboViewModel(
    private val repository: AmiiboRepository
) : ViewModel() {

    /**
     * Estado interno mutable.
     * Solo el ViewModel puede modificar este estado.
     */
    private val _uiState = MutableStateFlow<AmiiboUiState>(AmiiboUiState.Loading)

    /**
     * Estado público inmutable para la UI.
     *
     * StateFlow:
     * - Similar a LiveData pero de Kotlin Coroutines
     * - Siempre tiene un valor (no nullable)
     * - La UI se suscribe y recibe actualizaciones automáticas
     * - Ideal para Jetpack Compose
     *
     * asStateFlow(): Convierte MutableStateFlow a StateFlow inmutable
     */
    val uiState: StateFlow<AmiiboUiState> = _uiState.asStateFlow()

    /**
     * =========================================================================
     * PAGINACIÓN
     * =========================================================================
     *
     * Implementamos paginación del lado del cliente:
     * 1. Todos los datos se descargan de la API y se guardan en Room
     * 2. La UI carga páginas desde Room usando LIMIT/OFFSET
     * 3. El usuario puede configurar el tamaño de página (20, 50, 100)
     */

    /** Tamaño de página actual */
    private val _pageSize = MutableStateFlow(AmiiboRepository.DEFAULT_PAGE_SIZE)
    val pageSize: StateFlow<Int> = _pageSize.asStateFlow()

    /** Página actual (empezando en 0) */
    private val _currentPage = MutableStateFlow(0)

    /** Lista acumulada de amiibos cargados */
    private val _loadedAmiibos = MutableStateFlow<List<AmiiboEntity>>(emptyList())

    /** Indica si hay más páginas por cargar */
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    /** Indica si está cargando la siguiente página */
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    /**
     * =========================================================================
     * ERROR DE PAGINACIÓN
     * =========================================================================
     *
     * CONCEPTO: Errores Granulares en Paginación
     * ------------------------------------------
     * A diferencia del error principal (que afecta toda la pantalla), el error
     * de paginación solo afecta la carga de más items. Esto permite:
     *
     * 1. Mantener los datos ya cargados visibles
     * 2. Mostrar un botón de "Reintentar" inline al final de la lista
     * 3. No interrumpir la experiencia del usuario
     *
     * Es un patrón común en apps con infinite scroll como Twitter, Instagram, etc.
     */
    private val _paginationError = MutableStateFlow<String?>(null)
    val paginationError: StateFlow<String?> = _paginationError.asStateFlow()

    /** Opciones de tamaño de página disponibles */
    val pageSizeOptions: List<Int> = AmiiboRepository.PAGE_SIZE_OPTIONS

    /**
     * Flow de amiibos desde la base de datos.
     *
     * stateIn(): Convierte Flow a StateFlow
     * - viewModelScope: Se cancela cuando el ViewModel se destruye
     * - SharingStarted.WhileSubscribed(5000): Mantiene activo 5s después
     *   de que el último suscriptor se va (optimización para rotación)
     * - emptyList(): Valor inicial mientras se carga
     */
    private val amiibosFromDb: StateFlow<List<AmiiboEntity>> = repository
        .observeAmiibos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Inicialización del ViewModel.
     *
     * init { } se ejecuta cuando se crea el ViewModel.
     * Aquí configuramos la observación de datos y cargamos inicialmente.
     */
    init {
        // Observar cambios en la base de datos
        observeDatabaseChanges()
        // Cargar datos iniciales
        refreshAmiibos()
    }

    /**
     * =========================================================================
     * OBSERVAR CAMBIOS EN LA BASE DE DATOS
     * =========================================================================
     *
     * Configura la observación reactiva del Flow de Room.
     * Cada vez que los datos cambian, actualiza el UiState.
     */
    private fun observeDatabaseChanges() {
        viewModelScope.launch {
            amiibosFromDb.collect { amiibos ->
                // Solo actualiza a Success si hay datos o no estamos en Loading inicial
                val currentState = _uiState.value
                if (amiibos.isNotEmpty()) {
                    _uiState.value = AmiiboUiState.Success(
                        amiibos = amiibos,
                        isRefreshing = currentState is AmiiboUiState.Success &&
                                (currentState as? AmiiboUiState.Success)?.isRefreshing == true
                    )
                }
            }
        }
    }

    /**
     * =========================================================================
     * REFRESCAR AMIIBOS
     * =========================================================================
     *
     * Descarga datos frescos de la API.
     * Llamado desde:
     * - init {} al iniciar
     * - Pull-to-refresh de la UI
     * - Botón de reintentar en caso de error
     *
     * MANEJO DE ESTADOS:
     * 1. Si hay datos existentes → Success con isRefreshing = true
     * 2. Si no hay datos → Loading
     * 3. En éxito → Success (automático por el Flow de Room)
     * 4. En error → Error con datos en cache si existen
     */
    /**
     * =========================================================================
     * CAMBIAR TAMAÑO DE PÁGINA
     * =========================================================================
     *
     * Actualiza el tamaño de página y reinicia la paginación.
     *
     * @param newSize Nuevo tamaño de página
     */
    fun setPageSize(newSize: Int) {
        if (newSize != _pageSize.value && newSize in pageSizeOptions) {
            _pageSize.value = newSize
            resetPagination()
            loadFirstPage()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()


    /**
     * Reinicia el estado de paginación.
     * Limpia también cualquier error de paginación pendiente.
     */
    private fun resetPagination() {
        _currentPage.value = 0
        _loadedAmiibos.value = emptyList()
        _hasMorePages.value = true
        _paginationError.value = null  // Limpiar error al reiniciar
    }

    /**
     * =========================================================================
     * CARGAR SIGUIENTE PÁGINA (Infinite Scroll)
     * =========================================================================
     *
     * Llamado cuando el usuario hace scroll hasta el final de la lista.
     *
     * MANEJO DE ERRORES EN PAGINACIÓN:
     * --------------------------------
     * Si falla la carga de más items:
     * 1. NO cambiamos el estado principal (los datos existentes siguen visibles)
     * 2. Guardamos el error en _paginationError
     * 3. La UI muestra un botón "Reintentar" al final de la lista
     * 4. El usuario puede reintentar sin perder su posición de scroll
     */
    fun loadNextPage() {
        // Evitar cargas duplicadas o si hay error pendiente
        if (_isLoadingMore.value || !_hasMorePages.value || _paginationError.value != null) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            _paginationError.value = null  // Limpiar error previo

            try {
                val nextPage = _currentPage.value + 1
                val newItems = if (_searchQuery.value.isBlank()) {
                    repository.getAmiibosPage(nextPage, _pageSize.value)
                } else {
                    repository.searchAmiibosPage(
                        query = _searchQuery.value,
                        page = nextPage,
                        pageSize = _pageSize.value
                    )
                }


                if (newItems.isNotEmpty()) {
                    _currentPage.value = nextPage
                    _loadedAmiibos.value = _loadedAmiibos.value + newItems
                    _hasMorePages.value = if (_searchQuery.value.isBlank()) {
                        repository.hasMorePages(nextPage, _pageSize.value)
                    } else {
                        repository.hasMoreSearchPages(
                            query = _searchQuery.value,
                            currentPage = nextPage,
                            pageSize = _pageSize.value
                        )
                    }


                    _uiState.value = AmiiboUiState.Success(
                        amiibos = _loadedAmiibos.value,
                        isRefreshing = false
                    )
                } else {
                    _hasMorePages.value = false
                }
            } catch (e: Exception) {
                // Guardar error para mostrar botón de reintentar
                _paginationError.value = e.message ?: "Error al cargar más items"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * =========================================================================
     * REINTENTAR CARGA DE PÁGINA
     * =========================================================================
     *
     * Llamado cuando el usuario presiona "Reintentar" después de un error
     * de paginación. Limpia el error y vuelve a intentar cargar.
     */
    fun retryLoadMore() {
        _paginationError.value = null
        loadNextPage()
    }

    /**
     * Carga la primera página de datos.
     */
    private fun loadFirstPage() {
        viewModelScope.launch {
            try {
                val firstPageItems = repository.getAmiibosPage(0, _pageSize.value)
                _currentPage.value = 0
                _loadedAmiibos.value = firstPageItems
                _hasMorePages.value = repository.hasMorePages(0, _pageSize.value)

                _uiState.value = AmiiboUiState.Success(
                    amiibos = firstPageItems,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = AmiiboUiState.Error(
                    message = "Error al cargar datos",
                    isRetryable = true
                )
            }
        }
    }

    fun refreshAmiibos() {
        viewModelScope.launch {
            // Determinar estado durante la carga
            val currentAmiibos = _loadedAmiibos.value
            if (currentAmiibos.isEmpty()) {
                // No hay cache, mostrar loading
                _uiState.value = AmiiboUiState.Loading
            } else {
                // Hay cache, mostrar datos con indicador de refresh
                _uiState.value = AmiiboUiState.Success(
                    amiibos = currentAmiibos,
                    isRefreshing = true
                )
            }

            try {
                // Llamar al repositorio para refrescar TODOS los datos desde la API
                repository.refreshAmiibos()

                // Reiniciar paginación y cargar primera página
                resetPagination()
                val firstPageItems = repository.getAmiibosPage(0, _pageSize.value)
                _loadedAmiibos.value = firstPageItems
                _hasMorePages.value = repository.hasMorePages(0, _pageSize.value)

                _uiState.value = AmiiboUiState.Success(
                    amiibos = firstPageItems,
                    isRefreshing = false
                )

            } catch (e: AmiiboError) {
                /**
                 * MANEJO DE ERRORES TIPADOS
                 * -------------------------
                 * Capturamos AmiiboError (sealed class) para proporcionar:
                 * 1. Mensajes específicos por tipo de error
                 * 2. Indicar si se puede reintentar
                 * 3. Tipo de error para que la UI muestre iconos apropiados
                 *
                 * Esto mejora la UX porque el usuario sabe:
                 * - Si es su conexión (Network) → revisar WiFi/datos
                 * - Si es del servidor (Parse) → esperar y reintentar después
                 * - Si es local (Database) → reiniciar app o liberar espacio
                 */
                val cachedAmiibos = _loadedAmiibos.value
                val errorType = ErrorType.from(e)

                // Determinar si el error es recuperable con un reintento
                val isRetryable = when (e) {
                    is AmiiboError.Network -> true   // Puede mejorar la conexión
                    is AmiiboError.Parse -> false    // Requiere fix en API/app
                    is AmiiboError.Database -> true  // Puede liberarse espacio
                    is AmiiboError.Unknown -> true   // Vale la pena reintentar
                }

                _uiState.value = AmiiboUiState.Error(
                    message = e.message,
                    errorType = errorType,
                    isRetryable = isRetryable,
                    cachedAmiibos = cachedAmiibos
                )
            } catch (e: Exception) {
                // Catch-all para errores no tipados (no debería llegar aquí)
                val cachedAmiibos = _loadedAmiibos.value
                _uiState.value = AmiiboUiState.Error(
                    message = e.message ?: "Error desconocido al cargar datos",
                    errorType = ErrorType.UNKNOWN,
                    isRetryable = true,
                    cachedAmiibos = cachedAmiibos
                )
            }
        }
    }
}

/**
 * ============================================================================
 * NOTAS ADICIONALES SOBRE VIEWMODELS
 * ============================================================================
 *
 * 1. viewModelScope:
 *    - Scope de coroutines ligado al lifecycle del ViewModel
 *    - Se cancela automáticamente cuando el ViewModel se destruye
 *    - Usa Dispatchers.Main por defecto
 *
 * 2. SavedStateHandle (para preservar estado en process death):
 *    ```kotlin
 *    class MyViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
 *        val searchQuery = savedStateHandle.getStateFlow("query", "")
 *
 *        fun updateQuery(query: String) {
 *            savedStateHandle["query"] = query
 *        }
 *    }
 *    ```
 *
 * 3. Parámetros de navegación con Koin:
 *    ```kotlin
 *    // En el módulo:
 *    viewModel { (id: String) -> DetailViewModel(id, get()) }
 *
 *    // En Compose:
 *    val viewModel: DetailViewModel = koinViewModel { parametersOf(amiiboId) }
 *    ```
 *
 * 4. Múltiples Flows combinados:
 *    ```kotlin
 *    val uiState = combine(
 *        amiibosFlow,
 *        searchQueryFlow,
 *        sortOrderFlow
 *    ) { amiibos, query, sort ->
 *        amiibos.filter { it.name.contains(query) }
 *               .sortedBy { if (sort == "name") it.name else it.gameSeries }
 *    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
 *    ```
 *
 * ============================================================================
 */
