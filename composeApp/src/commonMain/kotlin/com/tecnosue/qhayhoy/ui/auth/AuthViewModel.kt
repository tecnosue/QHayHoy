package com.tecnosue.qhayhoy.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecnosue.qhayhoy.data.UsuarioRepository
import com.tecnosue.qhayhoy.domain.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Representa el estado de las pantallas de autenticación (Login y Registro).
 *
 * Un solo objeto UiState agrupa todo lo que la pantalla necesita saber:
 * qué ha escrito el usuario, si está cargando, si hay error, y si hay
 * sesión iniciada.
 */
data class AuthUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val usuarioActual: Usuario? = null
)

/**
 * ViewModel que gestiona las operaciones de autenticación (RF1):
 * registro, inicio de sesión y cierre de sesión.
 *
 * Expone el estado observable mediante StateFlow<AuthUiState> y las acciones
 * del usuario como funciones públicas.
 */
class AuthViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        usuarioRepository.observarUsuarioActual()
            .onEach { usuario ->
                _uiState.value = _uiState.value.copy(usuarioActual = usuario)
            }
            .launchIn(viewModelScope)
    }

    // --- Acciones de entrada (lo que hace el usuario en la UI) ---

    fun onNombreChange(nuevoNombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nuevoNombre)
    }

    fun onEmailChange(nuevoEmail: String) {
        _uiState.value = _uiState.value.copy(email = nuevoEmail)
    }

    fun onPasswordChange(nuevaPassword: String) {
        _uiState.value = _uiState.value.copy(password = nuevaPassword)
    }

    // --- Operaciones principales ---

    fun registrar() {
        val estadoActual = _uiState.value
        _uiState.value = estadoActual.copy(cargando = true, error = null)

        viewModelScope.launch {
            try {
                usuarioRepository.registrar(
                    nombre = estadoActual.nombre,
                    email = estadoActual.email,
                    password = estadoActual.password
                )
                // Éxito: limpiamos el estado de carga. El usuarioActual lo
                // actualizará automáticamente el observador del init {}.
                _uiState.value = _uiState.value.copy(cargando = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al registrar"
                )
            }
        }
    }

    fun iniciarSesion() {
        val estadoActual = _uiState.value
        _uiState.value = estadoActual.copy(cargando = true, error = null)

        viewModelScope.launch {
            try {
                usuarioRepository.iniciarSesion(
                    email = estadoActual.email,
                    password = estadoActual.password
                )
                _uiState.value = _uiState.value.copy(cargando = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            usuarioRepository.cerrarSesion()
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}