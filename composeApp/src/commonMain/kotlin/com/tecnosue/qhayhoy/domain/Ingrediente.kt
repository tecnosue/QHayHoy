package com.tecnosue.qhayhoy.domain

import kotlinx.serialization.Serializable

/**
 * Representa un ingrediente dentro de una receta.
 *
 * Es una clase dependiente: no vive de forma autónoma en Firestore,
 * sino embebida dentro del array 'ingredientes' del documento Receta.
 *
 * Esta desnormalización es habitual en Firestore:
 * - Mejora el rendimiento de lectura (1 sola consulta trae toda la receta)
 * - Coherente con el dominio (un ingrediente solo existe en su receta)
 */
@Serializable
data class Ingrediente(
    val nombre: String = "",
    val cantidad: Double = 0.0,
    val unidad: String = ""
)