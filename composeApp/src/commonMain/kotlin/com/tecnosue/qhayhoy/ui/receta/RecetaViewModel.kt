package com.tecnosue.qhayhoy.ui.receta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecnosue.qhayhoy.data.RecetaRepository
import com.tecnosue.qhayhoy.domain.Ingrediente
import com.tecnosue.qhayhoy.domain.OrigenReceta
import com.tecnosue.qhayhoy.domain.Receta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Estado de las pantallas relacionadas con recetas.
 *
 * Agrupa el listado, la receta actualmente en edición y los flags de UI.
 */
data class RecetaUiState(
    val recetas: List<Receta> = emptyList(),
    val recetaEnEdicion: Receta = Receta(),
    val cargando: Boolean = false,
    val error: String? = null,
    val operacionExitosa: Boolean = false
)

/**
 * ViewModel que gestiona las operaciones sobre recetas (RF3.1).
 *
 * Cubre:
 *  - Listado reactivo de recetas de la Casa activa
 *  - Creación, edición y eliminación de recetas propias
 *  - Edición progresiva de una receta (añadir ingredientes, pasos)
 */
class RecetaViewModel(
    private val recetaRepository: RecetaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecetaUiState())
    val uiState: StateFlow<RecetaUiState> = _uiState.asStateFlow()

    /**
     * Inicia la observación de recetas de la Casa indicada.
     * Se llama desde la pantalla de lista al tener disponible casaActivaId.
     */
    fun observarRecetas(casaId: String) {
        recetaRepository.observarRecetasDeCasa(casaId)
            .onEach { lista ->
                _uiState.value = _uiState.value.copy(recetas = lista)
            }
            .launchIn(viewModelScope)
    }

    // --- Gestión de la receta en edición ---

    /**
     * Inicia la edición de una receta nueva (en blanco).
     * Se llama al pulsar "+ Nueva receta".
     */
    fun iniciarNuevaReceta(creadorId: String) {
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = Receta(
                origen = OrigenReceta.PROPIA,
                creadaPor = creadorId
            ),
            error = null,
            operacionExitosa = false
        )
    }

    /**
     * Carga una receta existente al editor (para edición).
     */
    fun cargarRecetaParaEditar(receta: Receta) {
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = receta,
            error = null,
            operacionExitosa = false
        )
    }

    // --- Acciones de campos del editor ---

    fun onNombreChange(nuevoNombre: String) {
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = _uiState.value.recetaEnEdicion.copy(nombre = nuevoNombre)
        )
    }

    fun onRacionesChange(nuevasRaciones: Int) {
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = _uiState.value.recetaEnEdicion.copy(
                raciones = nuevasRaciones.coerceAtLeast(1)
            )
        )
    }

    fun onTiempoChange(nuevoTiempo: Int?) {
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = _uiState.value.recetaEnEdicion.copy(
                tiempoPreparacionMin = nuevoTiempo
            )
        )
    }

    // --- Gestión de ingredientes ---

    fun agregarIngrediente() {
        val actual = _uiState.value.recetaEnEdicion
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = actual.copy(
                ingredientes = actual.ingredientes + Ingrediente()
            )
        )
    }

    fun actualizarIngrediente(indice: Int, ingrediente: Ingrediente) {
        val actual = _uiState.value.recetaEnEdicion
        val nuevaLista = actual.ingredientes.toMutableList().apply {
            if (indice in indices) this[indice] = ingrediente
        }
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = actual.copy(ingredientes = nuevaLista)
        )
    }

    fun eliminarIngrediente(indice: Int) {
        val actual = _uiState.value.recetaEnEdicion
        val nuevaLista = actual.ingredientes.toMutableList().apply {
            if (indice in indices) removeAt(indice)
        }
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = actual.copy(ingredientes = nuevaLista)
        )
    }

    // --- Gestión de pasos ---

    fun agregarPaso() {
        val actual = _uiState.value.recetaEnEdicion
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = actual.copy(pasos = actual.pasos + "")
        )
    }

    fun actualizarPaso(indice: Int, texto: String) {
        val actual = _uiState.value.recetaEnEdicion
        val nuevaLista = actual.pasos.toMutableList().apply {
            if (indice in indices) this[indice] = texto
        }
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = actual.copy(pasos = nuevaLista)
        )
    }

    fun eliminarPaso(indice: Int) {
        val actual = _uiState.value.recetaEnEdicion
        val nuevaLista = actual.pasos.toMutableList().apply {
            if (indice in indices) removeAt(indice)
        }
        _uiState.value = _uiState.value.copy(
            recetaEnEdicion = actual.copy(pasos = nuevaLista)
        )
    }

    // --- Operaciones de persistencia ---

    fun guardarReceta(casaId: String) {
        val receta = _uiState.value.recetaEnEdicion
        if (receta.nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Introduce un nombre para la receta")
            return
        }

        _uiState.value = _uiState.value.copy(cargando = true, error = null)

        viewModelScope.launch {
            try {
                if (receta.id.isBlank()) {
                    // Nueva receta
                    recetaRepository.crearReceta(casaId, receta)
                } else {
                    // Edición de existente
                    recetaRepository.actualizarReceta(casaId, receta)
                }
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    operacionExitosa = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al guardar la receta"
                )
            }
        }
    }

    fun eliminarReceta(casaId: String, recetaId: String) {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)

        viewModelScope.launch {
            try {
                recetaRepository.eliminarReceta(casaId, recetaId)
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    operacionExitosa = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al eliminar la receta"
                )
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun limpiarOperacionExitosa() {
        _uiState.value = _uiState.value.copy(operacionExitosa = false)
    }
}