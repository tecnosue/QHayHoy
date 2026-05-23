package com.tecnosue.qhayhoy.domain

import kotlin.math.ceil
import kotlin.math.round

/**
 * Calcula la lista de la compra agregada a partir de un menú semanal,
 * las recetas disponibles y las asistencias confirmadas (RF6.1 y RF6.2).
 *
 * Decisiones de diseño:
 *  - Agrupación: por (nombre normalizado + unidad). Evita falsos positivos
 *    de la normalización lingüística (plurales, tildes) y mantiene una
 *    nomenclatura consistente con el catálogo curado.
 *  - Escalado por asistencia: factor = asistentes / raciones de la receta.
 *  - Escalado por unidad:
 *      - Unidades de peso/volumen → escalado lineal.
 *      - Unidades de conteo (ud, diente, hoja, sobre...) → ceil (no se
 *        pueden comprar 0,75 huevos).
 *      - Unidades cualitativas (pizca, al gusto) → no se escalan.
 *
 * Es una función pura: no toca Firestore ni el estado del ViewModel.
 */
object CalculadorListaCompra {

    private val UNIDADES_CONTABLES = setOf(
        "ud", "uds", "unidad", "unidades",
        "diente", "dientes",
        "hoja", "hojas",
        "sobre", "sobres",
        "lata", "latas",
        "rama", "ramas"
    )

    private val UNIDADES_CUALITATIVAS = setOf(
        "pizca", "al gusto", "c/s", "cantidad suficiente"
    )

    /**
     * @param menu Menú semanal con sus días y asistencias confirmadas.
     * @param recetas Recetas disponibles en la Casa (para resolver IDs).
     * @param itemsComprados Claves de items marcados como comprados (RF6.3).
     */
    fun calcular(
        menu: MenuSemanal,
        recetas: List<Receta>,
        itemsComprados: List<String>
    ): List<ItemCompra> {
        val recetasPorId = recetas.associateBy { it.id }

        // Acumulador: clave -> (nombre legible, unidad, cantidad acumulada)
        val acumulador = mutableMapOf<String, ItemEnConstruccion>()

        DiaSemana.entries.forEach { dia ->
            val comidaDia = menu.dias[dia.name] ?: return@forEach

            procesarComida(
                recetaId = comidaDia.comidaRecetaId,
                asistentes = menu.asistencias["${dia.name}_COMIDA"]?.size ?: 0,
                recetasPorId = recetasPorId,
                acumulador = acumulador
            )
            procesarComida(
                recetaId = comidaDia.cenaRecetaId,
                asistentes = menu.asistencias["${dia.name}_CENA"]?.size ?: 0,
                recetasPorId = recetasPorId,
                acumulador = acumulador
            )
        }

        return acumulador.values
            .map { it.toItemCompra(comprado = it.clave in itemsComprados) }
            .sortedBy { it.nombre }
    }

    private fun procesarComida(
        recetaId: String,
        asistentes: Int,
        recetasPorId: Map<String, Receta>,
        acumulador: MutableMap<String, ItemEnConstruccion>
    ) {
        if (recetaId.isBlank() || asistentes <= 0) return
        val receta = recetasPorId[recetaId] ?: return
        val factor = asistentes.toDouble() / receta.raciones.coerceAtLeast(1)

        receta.ingredientes.forEach { ingrediente ->
            val clave = clave(ingrediente.nombre, ingrediente.unidad)
            val cantidadEscalada = escalar(
                cantidadBase = ingrediente.cantidad,
                factor = factor,
                unidad = ingrediente.unidad
            )

            val existente = acumulador[clave]
            if (existente == null) {
                acumulador[clave] = ItemEnConstruccion(
                    clave = clave,
                    nombre = normalizarNombre(ingrediente.nombre),
                    unidad = normalizarUnidad(ingrediente.unidad),
                    cantidad = cantidadEscalada
                )
            } else {
                acumulador[clave] = existente.copy(
                    cantidad = existente.cantidad + cantidadEscalada
                )
            }
        }
    }

    private fun escalar(cantidadBase: Double, factor: Double, unidad: String): Double {
        val u = unidad.trim().lowercase()
        return when {
            u in UNIDADES_CUALITATIVAS -> cantidadBase  // sin escalar
            u in UNIDADES_CONTABLES    -> ceil(cantidadBase * factor)
            else                       -> redondear(cantidadBase * factor)
        }
    }

    private fun redondear(valor: Double): Double {
        // Redondeo a 2 decimales para evitar 374.99999...
        return round(valor * 100) / 100
    }

    /**
     * Equivalencias culinarias: variantes de un mismo ingrediente que en la
     * práctica doméstica se compran como uno solo.
     *
     * El mapeo es: forma normalizada (ya en singular, sin tildes) → forma canónica.
     * La forma canónica también debe estar normalizada.
     *
     * Mantener esta lista corta y centrada en casos reales del catálogo.
     */
    private val EQUIVALENCIAS = mapOf(
        // Aceites
        "aceite" to "aceite de oliva",
        "aceite de oliva virgen" to "aceite de oliva",
        "aceite de oliva virgen extra" to "aceite de oliva",
        "aove" to "aceite de oliva",

        // Sal y pimienta
        "sal fina" to "sal",
        "sal gruesa" to "sal",
        "sal marina" to "sal"

    )

    private fun normalizarNombre(nombre: String): String {
        val base = nombre.trim().lowercase()
            // Quita tildes básicas
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ü", "u")

        // Paso 1: singularización morfológica por reglas del español
        val singular = when {
            base.length <= 3 -> base
            base.endsWith("ces") -> base.dropLast(3) + "z"
            base.endsWith("nes") -> base.dropLast(2)
            base.endsWith("res") -> base.dropLast(2)
            base.endsWith("les") -> base.dropLast(2)
            base.endsWith("ses") -> base.dropLast(2)
            base.endsWith("des") -> base.dropLast(2)
            base.endsWith("es") && base.length > 4 -> base.dropLast(2)
            base.endsWith("s") && base.length > 3 -> base.dropLast(1)
            else -> base
        }

        // Paso 2: aplicar equivalencias culinarias (forma canónica)
        return EQUIVALENCIAS[singular] ?: singular
    }
    private fun normalizarUnidad(unidad: String): String {
        // Igualamos abreviaturas comunes: "gr" y "gramos" y "g" son lo mismo.
        val u = unidad.trim().lowercase()
        return when (u) {
            "gr", "gramos", "gramo" -> "g"
            "kgs", "kilos", "kilo", "kilogramo", "kilogramos" -> "kg"
            "litros", "litro" -> "l"
            "mililitros", "mililitro" -> "ml"
            "uds", "unidad", "unidades", "u" -> "ud"
            "cucharadas" -> "cucharada"
            "cucharaditas" -> "cucharadita"
            "dientes" -> "diente"
            "hojas" -> "hoja"
            "sobres" -> "sobre"
            "latas" -> "lata"
            "ramas" -> "rama"
            else -> u
        }
    }
    /**
     * Clave de agrupación: nombre base + unidad limpia.
     * Insensible a mayúsculas y espacios sobrantes.
     */
    fun clave(nombre: String, unidad: String): String {
        return "${normalizarNombre(nombre)}_${normalizarUnidad(unidad)}"
    }


    private data class ItemEnConstruccion(
        val clave: String,
        val nombre: String,
        val unidad: String,
        val cantidad: Double
    ) {
        fun toItemCompra(comprado: Boolean) = ItemCompra(
            clave = clave,
            nombre = nombre,
            cantidad = cantidad,
            unidad = unidad,
            comprado = comprado
        )
    }
}

