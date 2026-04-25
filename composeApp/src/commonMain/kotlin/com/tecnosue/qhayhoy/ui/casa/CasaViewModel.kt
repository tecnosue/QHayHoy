package com.tecnosue.qhayhoy.ui.casa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecnosue.qhayhoy.data.CasaRepository
import com.tecnosue.qhayhoy.domain.Casa
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Estado de las pantallas de gestión de Casa.
 *
 * Contiene los campos de los formularios (nombre, código), el listado
 * de casas del usuario, y los flags de UI (cargando, error, éxito).
 */
data class CasaUiState(
    val nombreNuevaCasa: String = "",
    val codigoInvitacion: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val casas: List<Casa> = emptyList(),
    val casaSeleccionada: Casa? = null,
    val operacionExitosa: Boolean = false
)

/**
 * ViewModel que gestiona las operaciones relacionadas con las Casas (RF2):
 * crear una nueva Casa, unirse a una existente, y observar las Casas
 * a las que pertenece el usuario.
 */
class CasaViewModel(
    private val casaRepository: CasaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasaUiState())
    val uiState: StateFlow<CasaUiState> = _uiState.asStateFlow()

    /**
     * Comienza a observar las Casas del usuario actual.
     * Se llama desde la UI al entrar en la pantalla principal de Casas,
     * pasando el ID del usuario autenticado.
     */
    fun observarCasas(usuarioId: String) {
        casaRepository.observarCasasDelUsuario(usuarioId)
            .onEach { listaCasas ->
                _uiState.value = _uiState.value.copy(casas = listaCasas)
            }
            .launchIn(viewModelScope)
    }

    // --- Acciones de entrada (lo que hace el usuario en la UI) ---

    fun onNombreNuevaCasaChange(nuevoNombre: String) {
        _uiState.value = _uiState.value.copy(nombreNuevaCasa = nuevoNombre)
    }

    fun onCodigoInvitacionChange(nuevoCodigo: String) {
        _uiState.value = _uiState.value.copy(
            codigoInvitacion = nuevoCodigo.uppercase()
        )
    }

    // --- Operaciones principales ---

    fun crearCasa(usuarioId: String) {
        val estadoActual = _uiState.value
        if (estadoActual.nombreNuevaCasa.isBlank()) {
            _uiState.value = estadoActual.copy(
                error = "Introduce un nombre para la Casa"
            )
            return
        }

        _uiState.value = estadoActual.copy(
            cargando = true,
            error = null,
            operacionExitosa = false
        )

        viewModelScope.launch {
            try {
                val casa = casaRepository.crearCasa(
                    nombre = estadoActual.nombreNuevaCasa,
                    creadorId = usuarioId
                )
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    casaSeleccionada = casa,
                    nombreNuevaCasa = "",
                    operacionExitosa = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al crear la Casa"
                )
            }
        }
    }

    fun unirseACasa(usuarioId: String) {
        val estadoActual = _uiState.value
        if (estadoActual.codigoInvitacion.isBlank()) {
            _uiState.value = estadoActual.copy(
                error = "Introduce un código de invitación"
            )
            return
        }

        _uiState.value = estadoActual.copy(
            cargando = true,
            error = null,
            operacionExitosa = false
        )

        viewModelScope.launch {
            try {
                val casa = casaRepository.unirseACasa(
                    codigoInvitacion = estadoActual.codigoInvitacion,
                    usuarioId = usuarioId
                )
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    casaSeleccionada = casa,
                    codigoInvitacion = "",
                    operacionExitosa = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al unirse a la Casa"
                )
            }
        }
    }

    /**
     * Marca como activa la Casa indicada para el usuario.
     * Se llama al pulsar una Casa de la lista "Mis Casas".
     */
    fun seleccionarCasa(usuarioId: String, casaId: String) {
        viewModelScope.launch {
            try {
                casaRepository.cambiarCasaActiva(usuarioId, casaId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al cambiar de Casa"
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