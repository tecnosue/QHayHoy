package com.tecnosue.qhayhoy.data.mealdb

import com.tecnosue.qhayhoy.domain.Ingrediente
import com.tecnosue.qhayhoy.domain.OrigenReceta
import com.tecnosue.qhayhoy.domain.Receta

/**
 * Convierte los DTOs de TheMealDB al modelo de dominio Receta.
 *
 * Esta capa de traducción es la única dependencia que tiene el resto
 * de la app respecto al formato externo. Si TheMealDB cambia su estructura,
 * solo este archivo necesita modificarse.
 */
object MealDbMapper {

    /**
     * Convierte un MealDto a una Receta lista para mostrar/importar.
     *
     * Si la receta no se va a guardar todavía (solo se muestra en la pantalla
     * "Descubrir"), se deja id en blanco. Al importarla a la Casa, el
     * RecetaRepository generará un id propio.
     *
     * @param creadaPor uid del usuario que está navegando.
     *                  Solo importa si finalmente se importa.
     */
    fun aReceta(dto: MealDto, creadaPor: String = ""): Receta {
        return Receta(
            id = "",  // se genera al guardar en Firestore
            nombre = dto.strMeal,
            origen = OrigenReceta.EXTERNA,
            idExterno = dto.idMeal,
            ingredientes = extraerIngredientes(dto),
            pasos = extraerPasos(dto.strInstructions),
            imagenUrl = dto.strMealThumb,
            tiempoPreparacionMin = null,  // TheMealDB no lo proporciona
            raciones = 4,                  // valor razonable por defecto
            etiquetasDieta = extraerEtiquetas(dto),
            creadaPor = creadaPor,
            fechaCreacion = null
        )
    }

    /**
     * Extrae los ingredientes a partir de los 20 campos planos del DTO.
     *
     * Aprovecha el método [MealDto.ingredientesEmparejados], que ya filtra
     * los huecos vacíos. Aquí solo separamos cantidad numérica de unidad.
     */
    private fun extraerIngredientes(dto: MealDto): List<Ingrediente> {
        return dto.ingredientesEmparejados().map { (nombre, medida) ->
            val (cantidad, unidad) = parsearMedida(medida)
            Ingrediente(
                nombre = nombre,
                cantidad = cantidad,
                unidad = unidad
            )
        }
    }

    /**
     * Intenta separar una medida de TheMealDB (ej: "3/4 cup", "1 tsp", "200g")
     * en (cantidad: Double, unidad: String).
     *
     * TheMealDB no es consistente: a veces escribe "1/2", "1 1/2", "2 cups",
     * "to taste", etc. Aplicamos un parseo "best-effort":
     *  - Buscamos un número al inicio (entero, decimal o fracción simple)
     *  - El resto se considera la unidad
     *  - Si no hay número, dejamos cantidad = 0 y unidad = la cadena entera
     */
    private fun parsearMedida(medida: String): Pair<Double, String> {
        val texto = medida.trim()
        if (texto.isEmpty()) return 0.0 to ""

        // Caso "1 1/2 cups" — número entero seguido de fracción
        val regexMixto = Regex("""^(\d+)\s+(\d+)/(\d+)\s*(.*)$""")
        regexMixto.matchEntire(texto)?.let { m ->
            val entero = m.groupValues[1].toDouble()
            val numerador = m.groupValues[2].toDouble()
            val denominador = m.groupValues[3].toDouble()
            val unidad = m.groupValues[4].trim()
            return (entero + numerador / denominador) to unidad
        }

        // Caso "3/4 cup"
        val regexFraccion = Regex("""^(\d+)/(\d+)\s*(.*)$""")
        regexFraccion.matchEntire(texto)?.let { m ->
            val numerador = m.groupValues[1].toDouble()
            val denominador = m.groupValues[2].toDouble()
            val unidad = m.groupValues[3].trim()
            return (numerador / denominador) to unidad
        }

        // Caso "200g" o "200 g" o "1.5 cups"
        val regexNumerico = Regex("""^(\d+(?:\.\d+)?)\s*(.*)$""")
        regexNumerico.matchEntire(texto)?.let { m ->
            val cantidad = m.groupValues[1].toDouble()
            val unidad = m.groupValues[2].trim()
            return cantidad to unidad
        }

        // No hay número (ej: "to taste", "pinch")
        return 0.0 to texto
    }

    /**
     * Convierte la cadena de instrucciones (un único bloque de texto)
     * en una lista de pasos.
     *
     * TheMealDB devuelve todas las instrucciones en un solo string,
     * con saltos de línea separando los pasos. A veces hay líneas vacías.
     */
    private fun extraerPasos(instrucciones: String?): List<String> {
        if (instrucciones.isNullOrBlank()) return emptyList()
        return instrucciones
            .split("\r\n", "\n", "\r")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    /**
     * Genera etiquetas de dieta sencillas a partir de la categoría.
     * Es una heurística — TheMealDB no tiene metadatos formales de dieta.
     */
    private fun extraerEtiquetas(dto: MealDto): List<String> {
        val etiquetas = mutableListOf<String>()
        when (dto.strCategory?.lowercase()) {
            "vegetarian" -> etiquetas.add("vegetariana")
            "vegan" -> {
                etiquetas.add("vegana")
                etiquetas.add("vegetariana")
            }
        }
        return etiquetas
    }
}