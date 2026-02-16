package com.curso.android.module3.amiibo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.vector.ImageVector
import coil3.compose.AsyncImage
import com.curso.android.module3.amiibo.R
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.domain.error.ErrorType
import com.curso.android.module3.amiibo.ui.viewmodel.AmiiboUiState
import com.curso.android.module3.amiibo.ui.viewmodel.AmiiboViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.unit.dp


/**
 * ============================================================================
 * AMIIBO LIST SCREEN - Pantalla Principal (Jetpack Compose)
 * ============================================================================
 *
 * Esta pantalla muestra la colección de Amiibos en un grid de 2 columnas.
 * Implementa el patrón de UI reactiva con:
 * - StateFlow para el estado
 * - when exhaustivo para manejar todos los estados
 * - Coil para carga asíncrona de imágenes
 *
 * ESTRUCTURA DE LA UI:
 * --------------------
 *
 *   ┌─────────────────────────────────────────┐
 *   │           TOP APP BAR                   │
 *   │  [Amiibo Vault]              [Refresh]  │
 *   ├─────────────────────────────────────────┤
 *   │                                         │
 *   │   ┌─────────┐    ┌─────────┐           │
 *   │   │  IMG    │    │  IMG    │           │
 *   │   │         │    │         │           │
 *   │   │  Name   │    │  Name   │           │
 *   │   │  Series │    │  Series │           │
 *   │   └─────────┘    └─────────┘           │
 *   │                                         │
 *   │   ┌─────────┐    ┌─────────┐           │
 *   │   │  IMG    │    │  IMG    │           │
 *   │   │  ...    │    │  ...    │           │
 *   │                                         │
 *   └─────────────────────────────────────────┘
 *
 * ============================================================================
 */

/**
 * Pantalla principal que muestra la lista de Amiibos.
 *
 * @OptIn(ExperimentalMaterial3Api::class):
 * - TopAppBar es experimental en Material3
 * - Requerido por las especificaciones del proyecto
 *
 * @param viewModel ViewModel inyectado por Koin
 *   - koinViewModel() busca el ViewModel en el contenedor de Koin
 *   - Equivalente a by viewModel() pero para Compose
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmiiboListScreen(
    onAmiiboClick: (String) -> Unit = {},
    viewModel: AmiiboViewModel = koinViewModel()
) {
    /**
     * collectAsStateWithLifecycle():
     * - Convierte StateFlow a State de Compose
     * - Respeta el lifecycle (pausa colección cuando la UI no es visible)
     * - Más eficiente que collectAsState() normal
     *
     * 'by' es delegación de propiedades:
     * - uiState se comporta como AmiiboUiState directamente
     * - Sin 'by' sería: uiState.value para acceder
     */
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pageSize by viewModel.pageSize.collectAsStateWithLifecycle()
    val hasMorePages by viewModel.hasMorePages.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val paginationError by viewModel.paginationError.collectAsStateWithLifecycle()

    // Estado para el dropdown del tamaño de página
    var showPageSizeDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            /**
             * TopAppBar de Material 3.
             *
             * Componentes:
             * - title: Título de la app
             * - actions: Selector de límite + botón refresh
             * - colors: Esquema de colores personalizado
             */
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    /**
                     * CONCEPTO: Dropdown para seleccionar tamaño de página
                     *
                     * Box envuelve el botón y el menú para posicionar
                     * correctamente el dropdown debajo del botón.
                     */
                    Box {
                        // Botón que muestra el tamaño de página actual
                        TextButton(
                            onClick = { showPageSizeDropdown = true }
                        ) {
                            Text(
                                text = "Página: $pageSize",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Menú desplegable con opciones
                        DropdownMenu(
                            expanded = showPageSizeDropdown,
                            onDismissRequest = { showPageSizeDropdown = false }
                        ) {
                            viewModel.pageSizeOptions.forEach { size ->
                                DropdownMenuItem(
                                    text = { Text("$size por página") },
                                    onClick = {
                                        viewModel.setPageSize(size)
                                        showPageSizeDropdown = false
                                    },
                                    leadingIcon = if (size == pageSize) {
                                        { Text("✓") }
                                    } else null
                                )
                            }
                        }
                    }

                    // Botón de refresh
                    IconButton(onClick = { viewModel.refreshAmiibos() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.retry)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        /**
         * =====================================================================
         * MANEJO DE ESTADOS CON WHEN EXHAUSTIVO
         * =====================================================================
         *
         * when sobre sealed interface garantiza que manejemos TODOS los casos.
         * Si agregas un nuevo estado al sealed interface, el compilador
         * te obligará a manejarlo aquí.
         *
         * Esto elimina errores comunes como:
         * - Olvidar manejar el estado de error
         * - Estados inconsistentes (loading + error al mismo tiempo)
         */
        when (val state = uiState) {
            // Estado de carga inicial
            is AmiiboUiState.Loading -> {
                LoadingContent(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            // Estado de éxito con datos
            is AmiiboUiState.Success -> {
                /**
                 * =====================================================================
                 * PULL-TO-REFRESH (Material 3)
                 * =====================================================================
                 *
                 * PullToRefreshBox es el componente oficial de Material 3 para
                 * implementar el patrón "pull-to-refresh" (deslizar hacia abajo
                 * para actualizar).
                 *
                 * CONCEPTO: Pull-to-Refresh
                 * -------------------------
                 * Es un patrón de UX muy común en apps móviles que permite al
                 * usuario actualizar el contenido deslizando hacia abajo desde
                 * la parte superior de la lista.
                 *
                 * Parámetros clave:
                 * - isRefreshing: Controla si se muestra el indicador de carga
                 * - onRefresh: Callback que se ejecuta cuando el usuario "suelta"
                 *
                 * VENTAJAS sobre LinearProgressIndicator manual:
                 * 1. Animación nativa del sistema (familiar para el usuario)
                 * 2. Gesture handling automático
                 * 3. Integración con el scroll del contenido
                 *
                 * NOTA: Requiere @OptIn(ExperimentalMaterial3Api::class)
                 */
                var searchText by remember { mutableStateOf("") }

                Column(modifier = Modifier.padding(paddingValues)) {

                    // 🔍 SEARCH FIELD
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            viewModel.onSearchQueryChanged(it)
                        },
                        label = { Text("Buscar Amiibo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )

                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refreshAmiibos() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Grid de Amiibos con paginación
                    AmiiboGrid(
                        amiibos = state.amiibos,
                        onAmiiboClick = onAmiiboClick,
                        hasMorePages = hasMorePages,
                        isLoadingMore = isLoadingMore,
                        paginationError = paginationError,
                        onLoadMore = { viewModel.loadNextPage() },
                        onRetryLoadMore = { viewModel.retryLoadMore() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            /**
             * Estado de error con tipo específico.
             *
             * CONCEPTO: Errores Tipados en UI
             * -------------------------------
             * El estado de error ahora incluye:
             * - errorType: Para mostrar iconos apropiados
             * - isRetryable: Para decidir si mostrar botón de reintentar
             *
             * Esto mejora la UX porque:
             * - El usuario ve un icono que representa el problema
             * - Solo ve "Reintentar" cuando tiene sentido
             */
            is AmiiboUiState.Error -> {
                if (state.cachedAmiibos.isNotEmpty()) {
                    // Hay datos en cache: mostrar datos + mensaje de error
                    Column(modifier = Modifier.padding(paddingValues)) {
                        ErrorBanner(
                            message = state.message,
                            errorType = state.errorType,
                            isRetryable = state.isRetryable,
                            onRetry = { viewModel.refreshAmiibos() }
                        )
                        AmiiboGrid(
                            amiibos = state.cachedAmiibos,
                            onAmiiboClick = onAmiiboClick,
                            hasMorePages = false,
                            isLoadingMore = false,
                            paginationError = null,
                            onLoadMore = {},
                            onRetryLoadMore = {},
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Sin cache: pantalla de error completa
                    ErrorContent(
                        message = state.message,
                        errorType = state.errorType,
                        isRetryable = state.isRetryable,
                        onRetry = { viewModel.refreshAmiibos() },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

/**
 * ============================================================================
 * COMPONENTES DE UI REUTILIZABLES
 * ============================================================================
 */

/**
 * Contenido mostrado durante la carga inicial.
 *
 * CircularProgressIndicator:
 * - Indicador de progreso indeterminado
 * - Material 3 style
 */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.loading_amiibos),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Contenido mostrado cuando hay error y no hay cache.
 *
 * CONCEPTO: Iconos por Tipo de Error
 * ----------------------------------
 * Cada tipo de error muestra un icono diferente para comunicar
 * visualmente la naturaleza del problema al usuario.
 *
 * @param message Mensaje de error
 * @param errorType Tipo de error para mostrar icono apropiado
 * @param isRetryable Si true, muestra botón de reintentar
 * @param onRetry Callback para reintentar
 */
@Composable
private fun ErrorContent(
    message: String,
    errorType: ErrorType,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Icono según el tipo de error
            Icon(
                imageVector = errorType.toIcon(),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = stringResource(R.string.error_loading),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Solo mostrar botón si el error es recuperable
            if (isRetryable) {
                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
    }
}

/**
 * Función de extensión para obtener el icono según el tipo de error.
 *
 * CONCEPTO: Extension Functions
 * ----------------------------
 * Las funciones de extensión permiten agregar métodos a clases
 * existentes sin modificarlas. Aquí agregamos toIcon() a ErrorType.
 */
private fun ErrorType.toIcon(): ImageVector = when (this) {
    ErrorType.NETWORK -> Icons.Default.CloudOff   // Sin conexión
    ErrorType.PARSE -> Icons.Default.Warning      // Error de datos
    ErrorType.DATABASE -> Icons.Default.Storage   // Error de BD
    ErrorType.UNKNOWN -> Icons.Default.Error      // Error genérico
}

/**
 * Banner de error mostrado sobre contenido existente.
 *
 * Útil cuando hay error pero tenemos datos en cache.
 * Muestra un icono pequeño según el tipo de error.
 *
 * @param errorType Tipo de error para mostrar icono apropiado
 * @param isRetryable Si true, muestra botón de reintentar
 */
@Composable
private fun ErrorBanner(
    message: String,
    errorType: ErrorType,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono pequeño según el tipo de error
            Icon(
                imageVector = errorType.toIcon(),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Solo mostrar botón si el error es recuperable
            if (isRetryable) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
    }
}

/**
 * Grid de Amiibos con soporte para paginación infinita.
 *
 * CONCEPTO: Infinite Scroll / Pagination
 * --------------------------------------
 * La paginación infinita carga más contenido cuando el usuario
 * se acerca al final de la lista. Esto mejora el rendimiento
 * al no cargar todos los datos de una vez.
 *
 * Implementación:
 * 1. Detectamos cuando el usuario está cerca del final (derivedStateOf)
 * 2. Llamamos a onLoadMore() para cargar la siguiente página
 * 3. Mostramos un indicador de carga o botón de reintentar al final
 *
 * @param amiibos Lista de Amiibos a mostrar
 * @param hasMorePages Indica si hay más páginas por cargar
 * @param isLoadingMore Indica si está cargando la siguiente página
 * @param paginationError Mensaje de error si falló la carga (null si no hay error)
 * @param onLoadMore Callback para cargar más items
 * @param onRetryLoadMore Callback para reintentar carga después de error
 */
@Composable
private fun AmiiboGrid(
    amiibos: List<AmiiboEntity>,
    onAmiiboClick: (String) -> Unit,
    hasMorePages: Boolean,
    isLoadingMore: Boolean,
    paginationError: String?,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    /**
     * =========================================================================
     * DERIVEDSTATEOF - Optimización de Compose
     * =========================================================================
     *
     * CONCEPTO: derivedStateOf vs LaunchedEffect
     * ------------------------------------------
     * Antes usábamos LaunchedEffect para detectar el scroll:
     * ```kotlin
     * LaunchedEffect(gridState.firstVisibleItemIndex, amiibos.size) {
     *     // Calcular si debemos cargar más...
     * }
     * ```
     *
     * PROBLEMA con LaunchedEffect:
     * - Se ejecuta en CADA cambio de firstVisibleItemIndex
     * - Incluso si el resultado del cálculo no cambia
     * - Genera recomposiciones innecesarias
     *
     * SOLUCIÓN con derivedStateOf:
     * - Solo notifica cuando el RESULTADO del cálculo cambia
     * - Más eficiente para valores derivados de otros estados
     * - Patrón recomendado para cálculos basados en scroll
     *
     * CUÁNDO USAR CADA UNO:
     * - derivedStateOf: Cuando necesitas un valor DERIVADO de otro estado
     *   y solo te importa cuando el resultado cambia
     * - LaunchedEffect: Cuando necesitas ejecutar efectos secundarios
     *   (llamadas a APIs, navegación, etc.)
     *
     * En este caso, shouldLoadMore es un Boolean derivado del estado del grid.
     * Solo nos importa cuando cambia de false a true.
     */
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            // Condición: estamos a 6 items del final Y hay más páginas
            // Y no estamos cargando Y no hay error pendiente
            lastVisibleItem >= totalItems - 6 &&
                    hasMorePages &&
                    !isLoadingMore &&
                    paginationError == null &&
                    totalItems > 0
        }
    }

    /**
     * LaunchedEffect SOLO se ejecuta cuando shouldLoadMore cambia a true.
     * Esto es mucho más eficiente que observar firstVisibleItemIndex directamente.
     */
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        state = gridState,
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Items de Amiibos
        items(
            items = amiibos,
            key = { it.id }
        ) { amiibo ->
            AmiiboCard(
                amiibo = amiibo,
                onClick = { onAmiiboClick(amiibo.name) }
            )
        }

        // Indicador de carga al final (ocupa 2 columnas)
        if (isLoadingMore) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        /**
         * =====================================================================
         * ERROR DE PAGINACIÓN CON BOTÓN DE REINTENTAR
         * =====================================================================
         *
         * CONCEPTO: Errores Inline en Infinite Scroll
         * -------------------------------------------
         * Cuando falla la carga de más items, NO queremos:
         * - Mostrar una pantalla de error completa (perdemos los datos)
         * - Ignorar el error silenciosamente (mala UX)
         *
         * En su lugar, mostramos un componente inline al final de la lista
         * que permite al usuario:
         * 1. Ver que hubo un error
         * 2. Reintentar la carga sin perder su posición de scroll
         *
         * Este patrón es usado por apps como Twitter, Instagram, Reddit, etc.
         */
        if (paginationError != null) {
            item(span = { GridItemSpan(2) }) {
                PaginationErrorItem(
                    errorMessage = paginationError,
                    onRetry = onRetryLoadMore
                )
            }
        }

        // Mensaje cuando no hay más páginas
        if (!hasMorePages && amiibos.isNotEmpty() && paginationError == null) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "— Fin de la lista (${amiibos.size} amiibos) —",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * ============================================================================
 * COMPONENTE: Error de Paginación Inline
 * ============================================================================
 *
 * Muestra un mensaje de error y botón de reintentar al final de la lista
 * cuando falla la carga de más items.
 *
 * DISEÑO:
 * - Card con color de error suave
 * - Icono + mensaje + botón en layout horizontal compacto
 * - Botón outlined para diferenciarlo del botón principal
 *
 * @param errorMessage Mensaje de error a mostrar
 * @param onRetry Callback cuando el usuario presiona "Reintentar"
 */
@Composable
private fun PaginationErrorItem(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono y mensaje
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón de reintentar
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reintentar",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * Card individual para mostrar un Amiibo.
 *
 * Usa AsyncImage de Coil para cargar imágenes de forma asíncrona.
 *
 * @param amiibo Datos del Amiibo a mostrar
 */
@Composable
private fun AmiiboCard(
    amiibo: AmiiboEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen con fondo degradado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = amiibo.imageUrl,
                        contentDescription = stringResource(
                            R.string.amiibo_image_description,
                            amiibo.name
                        ),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                // Información del Amiibo
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Nombre del Amiibo
                        Text(
                            text = amiibo.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Chip con la serie del juego
                        Surface(
                            modifier = Modifier.padding(top = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = amiibo.gameSeries,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ============================================================================
 * NOTAS ADICIONALES SOBRE COMPOSE
 * ============================================================================
 *
 * 1. RECOMPOSICIÓN:
 *    - Compose solo recompone lo que cambia
 *    - Usar 'key' en listas para optimizar
 *    - remember {} para cachear valores entre recomposiciones
 *
 * 2. PREVIEWS:
 *    ```kotlin
 *    @Preview(showBackground = true)
 *    @Composable
 *    fun AmiiboCardPreview() {
 *        AmiiboVaultTheme {
 *            AmiiboCard(
 *                amiibo = AmiiboEntity(
 *                    id = "1",
 *                    name = "Mario",
 *                    gameSeries = "Super Mario",
 *                    imageUrl = "https://example.com/mario.png"
 *                )
 *            )
 *        }
 *    }
 *    ```
 *
 * 3. PULL TO REFRESH (requiere material3 + material):
 *    ```kotlin
 *    PullToRefreshBox(
 *        isRefreshing = state.isRefreshing,
 *        onRefresh = { viewModel.refreshAmiibos() }
 *    ) {
 *        AmiiboGrid(...)
 *    }
 *    ```
 *
 * ============================================================================
 */
