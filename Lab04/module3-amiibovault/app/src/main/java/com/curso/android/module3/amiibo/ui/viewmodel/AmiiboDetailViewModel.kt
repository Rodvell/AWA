package com.curso.android.module3.amiibo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module3.amiibo.data.remote.model.AmiiboDetail
import com.curso.android.module3.amiibo.repository.AmiiboRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados posibles de la pantalla de detalle.
 */
sealed interface AmiiboDetailUiState {
    data object Loading : AmiiboDetailUiState
    data class Success(val amiibo: AmiiboDetail) : AmiiboDetailUiState
    data class Error(val message: String) : AmiiboDetailUiState
}

/**
 * ViewModel para la pantalla de detalle de un Amiibo.
 *
 * @param amiiboName Nombre del Amiibo a mostrar (pasado como parámetro de navegación)
 * @param repository Repositorio para obtener los datos
 */
class AmiiboDetailViewModel(
    private val amiiboName: String,
    private val repository: AmiiboRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AmiiboDetailUiState>(AmiiboDetailUiState.Loading)
    val uiState: StateFlow<AmiiboDetailUiState> = _uiState.asStateFlow()

    init {
        loadAmiiboDetail()
    }

    /**
     * Carga el detalle del Amiibo desde la API.
     */
    fun loadAmiiboDetail() {
        viewModelScope.launch {
            _uiState.value = AmiiboDetailUiState.Loading
            try {
                val detail = repository.getAmiiboDetail(amiiboName)
                _uiState.value = AmiiboDetailUiState.Success(detail)
            } catch (e: Exception) {
                _uiState.value = AmiiboDetailUiState.Error(
                    e.message ?: "Error al cargar el detalle"
                )
            }
        }
    }
}
