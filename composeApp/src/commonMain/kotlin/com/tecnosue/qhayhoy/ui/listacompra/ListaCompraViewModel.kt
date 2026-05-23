package com.tecnosue.qhayhoy.ui.listacompra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecnosue.qhayhoy.data.ListaCompraRepository
import com.tecnosue.qhayhoy.data.MenuRepository
import com.tecnosue.qhayhoy.data.RecetaRepository
import com.tecnosue.qhayhoy.domain.CalculadorListaCompra
import com.tecnosue.qhayhoy.domain.ItemCompra
import com.tecnosue.qhayhoy.domain.MenuSemanal
import com.tecnosue.qhayhoy.domain.Receta
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ListaCompraUiState(
    val items: List<ItemCompra> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val menuExiste: Boolean = true
)

class ListaCompraViewModel(
    private val menuRepository: MenuRepository,
    private val recetaRepository: RecetaRepository,
    private val listaCompraRepository: ListaCompraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaCompraUiState())
    val uiState: StateFlow<ListaCompraUiState> = _uiState.asStateFlow()

    private var jobObservacion: Job? = null

    /**
     * Observa simultáneamente: menú semanal, recetas de la Casa y items comprados.
     * Cada cambio en cualquiera de ellos recalcula la lista de la compra.
     */
    fun observarLista(casaId: String, semanaId: String) {
        jobObservacion?.cancel()
        _uiState.value = _uiState.value.copy(cargando = true, error = null)

        val menuFlow = menuRepository.observarMenuDeLaSemana(casaId, semanaId)
        val recetasFlow = recetaRepository.observarRecetasDeCasa(casaId)
        val compradosFlow = listaCompraRepository.observarComprados(casaId, semanaId)

        jobObservacion = combine(
            menuFlow,
            recetasFlow,
            compradosFlow
        ) { menu: MenuSemanal?, recetas: List<Receta>, comprados: List<String> ->
            Triple(menu, recetas, comprados)
        }.onEach { (menu, recetas, comprados) ->
            if (menu == null) {
                _uiState.value = _uiState.value.copy(
                    items = emptyList(),
                    cargando = false,
                    menuExiste = false
                )
            } else {
                val items = CalculadorListaCompra.calcular(menu, recetas, comprados)
                _uiState.value = _uiState.value.copy(
                    items = items,
                    cargando = false,
                    menuExiste = true,
                    error = null
                )
            }
        }.launchIn(viewModelScope)
    }

    fun cambiarEstadoComprado(
        casaId: String,
        semanaId: String,
        clave: String,
        comprado: Boolean
    ) {
        viewModelScope.launch {
            try {
                listaCompraRepository.cambiarEstadoComprado(casaId, semanaId, clave, comprado)
                // El flow reactivo actualizará la UI.
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al actualizar la lista"
                )
            }
        }
    }
}

