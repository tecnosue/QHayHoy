package com.tecnosue.qhayhoy.data.mealdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta genérica de TheMealDB para cualquier endpoint que devuelve recetas.
 *
 * El campo "meals" puede ser:
 *  - null si no hay resultados (en algunos endpoints)
 *  - lista vacía
 *  - lista con uno o varios MealDto
 */
@Serializable
data class MealsResponseDto(
    val meals: List<MealDto>? = null
)

/**
 * DTO que representa una receta tal y como la devuelve TheMealDB.
 *
 * Los campos siguen la nomenclatura original de la API (strMeal, strIngredient1...)
 * y nunca se exponen fuera del paquete data/mealdb.
 *
 * Algunos endpoints (como filter.php) solo rellenan id, nombre y thumbnail;
 * el resto de campos vienen null. Por eso prácticamente todos son nullable.
 */
@Serializable
data class MealDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String? = null,
    val strCategory: String? = null,
    val strArea: String? = null,
    val strInstructions: String? = null,
    val strTags: String? = null,
    val strYoutube: String? = null,

    // TheMealDB devuelve los ingredientes como 20 campos planos.
    // Los modelamos uno a uno y los unificaremos en el mapper.
    @SerialName("strIngredient1") val ingrediente1: String? = null,
    @SerialName("strIngredient2") val ingrediente2: String? = null,
    @SerialName("strIngredient3") val ingrediente3: String? = null,
    @SerialName("strIngredient4") val ingrediente4: String? = null,
    @SerialName("strIngredient5") val ingrediente5: String? = null,
    @SerialName("strIngredient6") val ingrediente6: String? = null,
    @SerialName("strIngredient7") val ingrediente7: String? = null,
    @SerialName("strIngredient8") val ingrediente8: String? = null,
    @SerialName("strIngredient9") val ingrediente9: String? = null,
    @SerialName("strIngredient10") val ingrediente10: String? = null,
    @SerialName("strIngredient11") val ingrediente11: String? = null,
    @SerialName("strIngredient12") val ingrediente12: String? = null,
    @SerialName("strIngredient13") val ingrediente13: String? = null,
    @SerialName("strIngredient14") val ingrediente14: String? = null,
    @SerialName("strIngredient15") val ingrediente15: String? = null,
    @SerialName("strIngredient16") val ingrediente16: String? = null,
    @SerialName("strIngredient17") val ingrediente17: String? = null,
    @SerialName("strIngredient18") val ingrediente18: String? = null,
    @SerialName("strIngredient19") val ingrediente19: String? = null,
    @SerialName("strIngredient20") val ingrediente20: String? = null,

    @SerialName("strMeasure1") val medida1: String? = null,
    @SerialName("strMeasure2") val medida2: String? = null,
    @SerialName("strMeasure3") val medida3: String? = null,
    @SerialName("strMeasure4") val medida4: String? = null,
    @SerialName("strMeasure5") val medida5: String? = null,
    @SerialName("strMeasure6") val medida6: String? = null,
    @SerialName("strMeasure7") val medida7: String? = null,
    @SerialName("strMeasure8") val medida8: String? = null,
    @SerialName("strMeasure9") val medida9: String? = null,
    @SerialName("strMeasure10") val medida10: String? = null,
    @SerialName("strMeasure11") val medida11: String? = null,
    @SerialName("strMeasure12") val medida12: String? = null,
    @SerialName("strMeasure13") val medida13: String? = null,
    @SerialName("strMeasure14") val medida14: String? = null,
    @SerialName("strMeasure15") val medida15: String? = null,
    @SerialName("strMeasure16") val medida16: String? = null,
    @SerialName("strMeasure17") val medida17: String? = null,
    @SerialName("strMeasure18") val medida18: String? = null,
    @SerialName("strMeasure19") val medida19: String? = null,
    @SerialName("strMeasure20") val medida20: String? = null,
) {

    /**
     * Devuelve los pares (ingrediente, medida) no vacíos.
     *
     * Recorre los 20 campos planos y filtra los que tienen nombre de ingrediente
     * (o sea, no son null/vacíos/blancos). El propio TheMealDB usa cadena vacía
     * para los huecos no usados, no null.
     */
    fun ingredientesEmparejados(): List<Pair<String, String>> {
        val ingredientes = listOf(
            ingrediente1, ingrediente2, ingrediente3, ingrediente4, ingrediente5,
            ingrediente6, ingrediente7, ingrediente8, ingrediente9, ingrediente10,
            ingrediente11, ingrediente12, ingrediente13, ingrediente14, ingrediente15,
            ingrediente16, ingrediente17, ingrediente18, ingrediente19, ingrediente20
        )
        val medidas = listOf(
            medida1, medida2, medida3, medida4, medida5,
            medida6, medida7, medida8, medida9, medida10,
            medida11, medida12, medida13, medida14, medida15,
            medida16, medida17, medida18, medida19, medida20
        )

        //zip es una función estándar de Kotlin que combina dos listas en pares por posición.
        // Si tienes [a, b, c] y [1, 2, 3],
        // devuelve [(a,1), (b,2), (c,3)].
        return ingredientes.zip(medidas)
            .mapNotNull { (ingr, medida) ->
                val nombre = ingr?.trim().orEmpty()
                if (nombre.isBlank()) null
                else nombre to (medida?.trim().orEmpty())
            }
    }
}