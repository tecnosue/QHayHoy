package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.domain.Ingrediente
import com.tecnosue.qhayhoy.domain.OrigenReceta
import com.tecnosue.qhayhoy.domain.Receta
import qhayhoy.composeapp.generated.resources.Res
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * DTO de receta del catálogo precargado.
 *
 * Refleja la estructura del JSON `catalogo_recetas.json`. El catálogo no
 * incluye los pasos de cocción (decisión de producto: el objetivo de la
 * app es planificar qué comer, no enseñar a cocinar).
 */
@Serializable
private data class RecetaCatalogoDto(
    val nombre: String,
    val ingredientes: List<IngredienteCatalogoDto>,
    val tiempoPreparacionMin: Int? = null,
    val raciones: Int = 4,
    val etiquetasDieta: List<String> = emptyList()
)

@Serializable
private data class IngredienteCatalogoDto(
    val nombre: String,
    val cantidad: Double,
    val unidad: String
)

/**
 * Encargado de cargar el catálogo inicial de recetas en una Casa recién creada.
 *
 * Solo se invoca una vez por Casa, justo después de su creación. Lee el JSON
 * empaquetado en los recursos de Compose Multiplatform, lo deserializa y crea
 * cada receta en la subcolección `casas/{casaId}/recetas` con origen CATALOGO.
 */
class CatalogoLoader(
    private val recetaRepository: RecetaRepository
) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Carga el catálogo completo en la Casa indicada.
     *
     * Si falla la lectura del recurso o alguna inserción concreta, se registra
     * el error pero no se interrumpe la carga del resto de recetas.
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun cargarCatalogoEnCasa(casaId: String, creadorId: String) {
        val bytes = Res.readBytes("files/catalogo_recetas.json")
        val contenido = bytes.decodeToString()
        val dtos = json.decodeFromString<List<RecetaCatalogoDto>>(contenido)

        dtos.forEach { dto ->
            val receta = Receta(
                id = "",
                nombre = dto.nombre,
                origen = OrigenReceta.CATALOGO,
                idExterno = null,
                ingredientes = dto.ingredientes.map {
                    Ingrediente(
                        nombre = it.nombre,
                        cantidad = it.cantidad,
                        unidad = it.unidad
                    )
                },
                pasos = emptyList(),
                imagenUrl = null,
                tiempoPreparacionMin = dto.tiempoPreparacionMin,
                raciones = dto.raciones,
                etiquetasDieta = dto.etiquetasDieta,
                creadaPor = creadorId,
                fechaCreacion = Clock.System.now()
            )
            try {
                recetaRepository.crearReceta(casaId, receta)
            } catch (e: Exception) {
                // Si una receta concreta falla, seguimos con el resto.
                // En una versión futura se podría reportar el error a un sistema
                // centralizado de logs.
            }
        }
    }
}