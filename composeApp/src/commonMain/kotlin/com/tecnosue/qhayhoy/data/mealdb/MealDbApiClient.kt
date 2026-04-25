package com.tecnosue.qhayhoy.data.mealdb

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP para la API pública TheMealDB.
 *
 * Encapsula la configuración del HttpClient y expone métodos de alto nivel
 * por cada endpoint que la app utiliza. Devuelve siempre listas de DTOs
 * (List<MealDto>) para que el repositorio se encargue del mapping al modelo.
 *
 * Se utiliza la API key de prueba "1" durante el desarrollo, conforme a la
 * licencia de TheMealDB. Para producción se requeriría una clave premium.
 */
class MealDbApiClient {

    private val baseUrl = "https://www.themealdb.com/api/json/v1/1"

    /**
     * Cliente HTTP configurado con:
     *  - Negociación de contenido JSON (kotlinx.serialization)
     *  - ignoreUnknownKeys: la API añade campos nuevos sin romper la app
     *  - Logging básico para depuración (visible en Logcat)
     */
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true //Si TheMealDB añade un campo nuevo que no tenemos declarado lo ignora y no rompe la app
                isLenient = true
            })
        }
        install(Logging) {
            // para ver los log. En producción se pondría LogLevel.NONE.
            level = LogLevel.INFO
        }
    }

    /**
     * Devuelve la lista de recetas de una categoría (preview ligero).
     *
     * Endpoint: filter.php?c={categoria}
     * Solo rellena: idMeal, strMeal, strMealThumb.
     * Para detalle completo hay que llamar a [obtenerDetalleReceta].
     *
     * Categorías típicas: "Vegetarian", "Vegan", "Beef", "Chicken", "Seafood"...
     */
    suspend fun listarPorCategoria(categoria: String): List<MealDto> {
        val respuesta: MealsResponseDto = httpClient
            .get("$baseUrl/filter.php") {
                parameter("c", categoria)
            }
            .body()
        return respuesta.meals.orEmpty()
    }

    /**
     * Devuelve el detalle completo de una receta por su ID.
     *
     * Endpoint: lookup.php?i={id}
     * Rellena todos los campos: ingredientes, medidas, instrucciones, etc.
     *
     * @return el MealDto si existe, null si TheMealDB no encuentra nada.
     */
    suspend fun obtenerDetalleReceta(idMeal: String): MealDto? {
        val respuesta: MealsResponseDto = httpClient
            .get("$baseUrl/lookup.php") {
                parameter("i", idMeal)
            }
            .body()
        return respuesta.meals?.firstOrNull()
    }

    /**
     * Busca recetas por nombre (búsqueda libre).
     *
     * Endpoint: search.php?s={query}
     * Devuelve recetas con detalle completo (no solo preview).
     * Si la query está vacía o no encuentra nada, devuelve lista vacía.
     */
    suspend fun buscarPorNombre(query: String): List<MealDto> {
        if (query.isBlank()) return emptyList()
        val respuesta: MealsResponseDto = httpClient
            .get("$baseUrl/search.php") {
                parameter("s", query.trim())
            }
            .body()
        return respuesta.meals.orEmpty()
    }
}