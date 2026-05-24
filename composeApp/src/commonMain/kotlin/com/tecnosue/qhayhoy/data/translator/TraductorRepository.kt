package com.tecnosue.qhayhoy.data.translator

import com.tecnosue.qhayhoy.domain.Receta
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Servicio de traducción de recetas externas (RF3).
 *
 * Recibe una receta importada de TheMealDB en inglés y la devuelve
 * traducida al español. Si la traducción falla en algún campo,
 * mantiene el original sin bloquear la importación.
 *
 * Optimización: traduce los ingredientes y pasos en paralelo mediante
 * corrutinas, reduciendo el tiempo total de importación.
 */
class TraductorRepository(
    private val myMemoryClient: MyMemoryClient
) {

    /**
     * Traduce los campos textuales de una receta al español.
     * Si algún campo falla, se conserva en su idioma original.
     */
    suspend fun traducirRecetaAEspanol(receta: Receta): Receta = coroutineScope {
        // Lanzamos todas las traducciones en paralelo
        val nombreDeferred = async { myMemoryClient.traducir(receta.nombre) }

        val ingredientesDeferred = receta.ingredientes.map { ing ->
            async {
                val nombreIng = myMemoryClient.traducir(ing.nombre)
                val unidadIng = if (ing.unidad.isNotBlank()) {
                    myMemoryClient.traducir(ing.unidad)
                } else {
                    ing.unidad
                }
                ing.copy(nombre = nombreIng, unidad = unidadIng)
            }
        }

        val pasosDeferred = receta.pasos.map { paso ->
            async { traducirTextoLargo(paso) }
        }

        // Esperamos todos los resultados
        receta.copy(
            nombre = nombreDeferred.await(),
            ingredientes = ingredientesDeferred.map { it.await() },
            pasos = pasosDeferred.map { it.await() }
        )
    }

    /**
     * Traduce un texto que puede superar el límite de 500 caracteres
     * de MyMemory, partiéndolo por frases.
     */
    private suspend fun traducirTextoLargo(texto: String): String {
        if (texto.length <= 500) {
            return myMemoryClient.traducir(texto)
        }

        // Partimos por puntos para mantener coherencia
        val frases = texto.split(". ")
        val bloques = mutableListOf<String>()
        val acumulador = StringBuilder()

        for (frase in frases) {
            if (acumulador.length + frase.length > 480) {
                if (acumulador.isNotBlank()) {
                    bloques.add(acumulador.toString())
                    acumulador.clear()
                }
            }
            if (acumulador.isNotEmpty()) acumulador.append(". ")
            acumulador.append(frase)
        }

        if (acumulador.isNotBlank()) {
            bloques.add(acumulador.toString())
        }

        // Traducimos cada bloque en paralelo
        return coroutineScope {
            bloques.map { bloque ->
                async { myMemoryClient.traducir(bloque) }
            }.map { it.await() }.joinToString(". ")
        }
    }
}