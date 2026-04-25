package com.tecnosue.qhayhoy.ui.receta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecnosue.qhayhoy.data.RecetaExternaRepository
import com.tecnosue.qhayhoy.data.RecetaRepository
import com.tecnosue.qhayhoy.domain.Ingrediente
import com.tecnosue.qhayhoy.domain.OrigenReceta
import com.tecnosue.qhayhoy.domain.Receta
import kotlinx.coroutines.Job
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
    val operacionExitosa: Boolean = false,

    // --- Estado para "Descubrir recetas" (RF3.2 / RF3.3) ---
    val recetasExternas: List<Receta> = emptyList(),
    val cargandoExternas: Boolean = false,
    val errorExternas: String? = null,
    val recetaExternaSeleccionada: Receta? = null,
    val cargandoDetalleExterno: Boolean = false,
    val importandoReceta: Boolean = false,
    val importacionExitosa: Boolean = false,
    val filtroDieta: FiltroDieta = FiltroDieta.VEGETARIANA
)

/**
 * Filtros disponibles en la pantalla "Descubrir recetas".
 */
enum class FiltroDieta {
    VEGETARIANA,
    VEGANA
}

/**
 * ViewModel que gestiona las operaciones sobre recetas (RF3.1).
 *
 * Cubre:
 *  - Listado reactivo de recetas de la Casa activa
 *  - Creación, edición y eliminación de recetas propias
 *  - Edición progresiva de una receta (añadir ingredientes, pasos)
 */
class RecetaViewModel(
    private val recetaRepository: RecetaRepository,
    private val recetaExternaRepository: RecetaExternaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecetaUiState())
    val uiState: StateFlow<RecetaUiState> = _uiState.asStateFlow()

    /**
     * Inicia la observación de recetas de la Casa indicada.
     * Se llama desde la pantalla de lista al tener disponible casaActivaId.
     */
    /**
     * Job que mantiene la suscripción al Flow de recetas de la Casa actual.
     * Permite cancelar la observación anterior al cambiar de Casa, evitando
     * que dos flujos compitan emitiendo en paralelo.
     */
    private var jobObservacion: Job? = null
    private var casaObservadaId: String? = null

    /**
     * Inicia la observación reactiva de recetas para la Casa indicada.
     * Si ya estaba observando esa misma Casa, no hace nada (idempotente).
     * Si estaba observando otra Casa, cancela el flujo anterior y limpia
     * el estado para evitar mostrar datos obsoletos durante la transición.
     */
    fun observarRecetas(casaId: String) {

        if (casaObservadaId == casaId)  return


        jobObservacion?.cancel()
        _uiState.value = _uiState.value.copy(recetas = emptyList())

        casaObservadaId = casaId
        jobObservacion = recetaRepository.observarRecetasDeCasa(casaId)
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

    // --- Operaciones de "Descubrir recetas" ---

    /**
     * Carga el listado inicial de recetas externas según el filtro actual.
     * Se llama al entrar en la pantalla "Descubrir".
     */
    fun cargarRecetasExternas() {
        _uiState.value = _uiState.value.copy(
            cargandoExternas = true,
            errorExternas = null
        )

        viewModelScope.launch {
            try {
                val lista = when (_uiState.value.filtroDieta) {
                    FiltroDieta.VEGETARIANA -> recetaExternaRepository.listarVegetarianas()
                    FiltroDieta.VEGANA -> recetaExternaRepository.listarVeganas()
                }
                _uiState.value = _uiState.value.copy(
                    recetasExternas = lista,
                    cargandoExternas = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargandoExternas = false,
                    errorExternas = e.message ?: "Error al cargar recetas externas"
                )
            }
        }
    }

    /**
     * Cambia el filtro de dieta y recarga las recetas externas.
     */
    fun cambiarFiltroDieta(nuevoFiltro: FiltroDieta) {
        if (_uiState.value.filtroDieta == nuevoFiltro) return
        _uiState.value = _uiState.value.copy(filtroDieta = nuevoFiltro)
        cargarRecetasExternas()
    }

    /**
     * Pide a TheMealDB el detalle completo de una receta externa.
     * Se llama al entrar en la pantalla de preview.
     *
     * El listado de TheMealDB devuelve solo nombre+miniatura;
     * los ingredientes solo vienen al pedir el detalle por id.
     */
    fun cargarDetalleReceptaExterna(idExterno: String) {
        _uiState.value = _uiState.value.copy(
            cargandoDetalleExterno = true,
            errorExternas = null,
            recetaExternaSeleccionada = null
        )

        viewModelScope.launch {
            try {
                val detalle = recetaExternaRepository.obtenerDetalle(idExterno)
                _uiState.value = _uiState.value.copy(
                    cargandoDetalleExterno = false,
                    recetaExternaSeleccionada = detalle,
                    errorExternas = if (detalle == null) "No se encontró la receta" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargandoDetalleExterno = false,
                    errorExternas = e.message ?: "Error al cargar el detalle"
                )
            }
        }
    }

    /**
     * Importa la receta externa actualmente seleccionada a la Casa indicada.
     * Marca la receta con origen EXTERNA y conserva el idExterno para
     * trazabilidad.
     */
    fun importarRecetaExterna(casaId: String, creadorId: String) {
        val receta = _uiState.value.recetaExternaSeleccionada ?: return

        _uiState.value = _uiState.value.copy(
            importandoReceta = true,
            importacionExitosa = false,
            errorExternas = null
        )

        viewModelScope.launch {
            try {
                val recetaParaImportar = receta.copy(creadaPor = creadorId)
                recetaRepository.importarRecetaExterna(
                    casaId = casaId,
                    receta = recetaParaImportar,
                    idExterno = receta.idExterno ?: ""
                )
                _uiState.value = _uiState.value.copy(
                    importandoReceta = false,
                    importacionExitosa = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    importandoReceta = false,
                    errorExternas = e.message ?: "Error al importar la receta"
                )
            }
        }
    }

    fun limpiarImportacionExitosa() {
        _uiState.value = _uiState.value.copy(importacionExitosa = false)
    }

    fun limpiarErrorExterno() {
        _uiState.value = _uiState.value.copy(errorExternas = null)
    }
}