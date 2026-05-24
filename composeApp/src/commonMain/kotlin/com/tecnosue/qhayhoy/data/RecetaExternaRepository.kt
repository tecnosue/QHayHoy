package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.data.mealdb.MealDbApiClient
import com.tecnosue.qhayhoy.data.mealdb.MealDbMapper
import com.tecnosue.qhayhoy.data.translator.TraductorRepository
import com.tecnosue.qhayhoy.domain.Receta

/**
 * Repositorio que centraliza el acceso a recetas externas (RF3.2).
 *
 * Encapsula:
 *  - el cliente HTTP de TheMealDB
 *  - la traducción DTO → modelo de dominio
 *  - la traducción de inglés a español al obtener el detalle
 *
 * Decisión de diseño: el listado (vegetarianas/veganas) se mantiene en
 * inglés porque devuelve cientos de recetas y traducir todos los nombres
 * agotaría rápidamente la cuota gratuita del servicio de traducción.
 * En cambio, el detalle (nombre + ingredientes + pasos) sí se traduce,
 * ya que es donde la traducción aporta valor real al usuario.
 */
class RecetaExternaRepository(
    private val apiClient: MealDbApiClient,
    private val traductorRepository: TraductorRepository
) {

    suspend fun listarVegetarianas(): List<Receta> {
        val dtos = apiClient.listarPorCategoria("Vegetarian")
        return dtos.map { MealDbMapper.aReceta(it) }
    }

    suspend fun listarVeganas(): List<Receta> {
        val dtos = apiClient.listarPorCategoria("Vegan")
        return dtos.map { MealDbMapper.aReceta(it) }
    }

    suspend fun buscarPorNombre(query: String): List<Receta> {
        val dtos = apiClient.buscarPorNombre(query)
        return dtos.map { MealDbMapper.aReceta(it) }
    }

    /**
     * Obtiene el detalle completo de una receta y la traduce al español.
     *
     * Si la traducción falla por cualquier motivo, la receta se devuelve
     * en su idioma original (inglés) sin bloquear la importación.
     *
     * @return la Receta completa traducida, o null si TheMealDB no la encuentra.
     */
    suspend fun obtenerDetalle(idExterno: String): Receta? {
        val dto = apiClient.obtenerDetalleReceta(idExterno) ?: return null
        val recetaEnIngles = MealDbMapper.aReceta(dto)
        return traductorRepository.traducirRecetaAEspanol(recetaEnIngles)
    }
}