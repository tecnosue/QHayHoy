package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.data.mealdb.MealDbApiClient
import com.tecnosue.qhayhoy.data.mealdb.MealDbMapper
import com.tecnosue.qhayhoy.domain.Receta

/**
 * Repositorio que centraliza el acceso a recetas externas (RF3.2).
 *
 * Encapsula:
 *  - el cliente HTTP de TheMealDB
 *  - la traducción DTO → modelo de dominio
 *
 * Hacia el resto de la app, expone exclusivamente objetos Receta,
 * de forma que ni los ViewModels ni la UI conocen los detalles
 * del proveedor externo. Si en el futuro se sustituye TheMealDB por
 * otra API, solo este repositorio (y el mapper) cambiarán.
 */
class RecetaExternaRepository(
    private val apiClient: MealDbApiClient
) {

    /**
     * Devuelve un listado ligero de recetas vegetarianas.
     *
     * Internamente se hace una petición a filter.php?c=Vegetarian, que
     * solo devuelve id, nombre y miniatura por receta. Los ingredientes
     * y pasos completos se obtienen al pedir el detalle de una receta
     * concreta con [obtenerDetalle].
     */
    suspend fun listarVegetarianas(): List<Receta> {
        val dtos = apiClient.listarPorCategoria("Vegetarian")
        return dtos.map { MealDbMapper.aReceta(it) }
    }

    /**
     * Devuelve un listado ligero de recetas veganas.
     */
    suspend fun listarVeganas(): List<Receta> {
        val dtos = apiClient.listarPorCategoria("Vegan")
        return dtos.map { MealDbMapper.aReceta(it) }
    }

    /**
     * Busca recetas por texto libre en el nombre.
     * Devuelve recetas con detalle completo (ingredientes, pasos, etc).
     */
    suspend fun buscarPorNombre(query: String): List<Receta> {
        val dtos = apiClient.buscarPorNombre(query)
        return dtos.map { MealDbMapper.aReceta(it) }
    }

    /**
     * Obtiene el detalle completo de una receta a partir de su id externo.
     *
     * Se utiliza al "previsualizar" o "importar" una receta del listado
     * vegetariano/vegano (que solo trae datos básicos).
     *
     * @return la Receta completa, o null si TheMealDB no la encuentra.
     */
    suspend fun obtenerDetalle(idExterno: String): Receta? {
        val dto = apiClient.obtenerDetalleReceta(idExterno) ?: return null
        return MealDbMapper.aReceta(dto)
    }
}