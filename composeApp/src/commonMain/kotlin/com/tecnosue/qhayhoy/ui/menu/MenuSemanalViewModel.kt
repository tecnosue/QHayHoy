package com.tecnosue.qhayhoy.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecnosue.qhayhoy.data.MenuRepository
import com.tecnosue.qhayhoy.data.RecetaRepository
import com.tecnosue.qhayhoy.domain.ComidaDia
import com.tecnosue.qhayhoy.domain.DiaSemana
import com.tecnosue.qhayhoy.domain.MenuSemanal
import com.tecnosue.qhayhoy.domain.Receta
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class MenuSemanalUiState(
    val menu: MenuSemanal? = null,
    val recetas: List<Receta> = emptyList(), // <-- AÑADIDO
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoGeneracion: Boolean = false
)

class MenuSemanalViewModel(
    private val menuRepository: MenuRepository,
    private val recetaRepository: RecetaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuSemanalUiState())
    val uiState: StateFlow<MenuSemanalUiState> = _uiState.asStateFlow()

    private var jobObservacion: Job? = null

    // NUEVO: Observa tanto el menú como las recetas de la casa
    fun observarDatos(casaId: String, semanaId: String) {
        jobObservacion?.cancel()
        _uiState.value = _uiState.value.copy(cargando = true, error = null)

        jobObservacion = menuRepository.observarMenuDeLaSemana(casaId, semanaId)
            .onEach { menuObtenido ->
                _uiState.value = _uiState.value.copy(menu = menuObtenido, cargando = false)
            }
            .launchIn(viewModelScope)

        recetaRepository.observarRecetasDeCasa(casaId)
            .onEach { listaRecetas ->
                _uiState.value = _uiState.value.copy(recetas = listaRecetas)
            }
            .launchIn(viewModelScope)
    }

    // Le quitamos la lista de recetas de los parámetros, las coge de su propio estado
    fun generarMenuAutomatico(casaId: String, semanaId: String, miembrosIds: List<String>) {
        _uiState.value = _uiState.value.copy(cargando = true, error = null, exitoGeneracion = false)

        viewModelScope.launch {
            try {
                val recetasDisponibles = _uiState.value.recetas

                if (recetasDisponibles.isEmpty()) {
                    throw Exception("No hay recetas suficientes en la Casa para generar un menú.")
                }

                val recetasMezcladas = recetasDisponibles.shuffled()
                val diasGenerados = mutableMapOf<String, ComidaDia>()
                var indexReceta = 0

                DiaSemana.entries.forEach { dia ->
                    val comidaId = recetasMezcladas[indexReceta % recetasMezcladas.size].id
                    indexReceta++
                    val cenaId = recetasMezcladas[indexReceta % recetasMezcladas.size].id
                    indexReceta++

                    // Clave como String para que Firestore no falle
                    diasGenerados[dia.name] = ComidaDia(comidaRecetaId = comidaId, cenaRecetaId = cenaId)
                }

                val clavesAsistencia = DiaSemana.entries.flatMap { dia ->
                    listOf("${dia.name}_COMIDA", "${dia.name}_CENA")
                }
                val asistenciasPorDefecto = clavesAsistencia.associateWith { miembrosIds }

                val nuevoMenu = MenuSemanal(
                    id = semanaId,
                    fechaInicioMillis = Clock.System.now().toEpochMilliseconds(),
                    dias = diasGenerados,
                    asistencias = asistenciasPorDefecto
                )

                menuRepository.guardarMenu(casaId, nuevoMenu)

                _uiState.value = _uiState.value.copy(cargando = false, exitoGeneracion = true)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = e.message ?: "Error al generar el menú"
                )
            }
        }
    }

    fun limpiarEstadoExito() {
        _uiState.value = _uiState.value.copy(exitoGeneracion = false)
    }

    fun sustituirPlato(
        casaId: String,
        semanaId: String,
        dia: String,
        tipo: String,
        nuevaRecetaId: String
    ) {
        viewModelScope.launch {
            try {
                menuRepository.sustituirPlato(casaId, semanaId, dia, tipo, nuevaRecetaId)
                // El listener reactivo de observarMenuDeLaSemana refresca la UI sola.
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al sustituir el plato"
                )
            }
        }
    }
}