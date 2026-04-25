package com.tecnosue.qhayhoy.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Indica el origen de una receta.
 *
 * - PROPIA: creada manualmente por un miembro de la Casa.
 * - CATALOGO: precargada en la aplicación desde el catálogo curado.
 * - EXTERNA: importada desde la API externa (TheMealDB) por un
 *   miembro de la Casa. Al importarla, el usuario puede editarla
 *   antes de guardarla en la Casa.
 */
@Serializable
enum class OrigenReceta {
    PROPIA,
    CATALOGO,
    EXTERNA
}

/**
 * Representa una receta perteneciente a una Casa.
 *
 * Las recetas viven en la subcolección:
 *   casas/{casaId}/recetas/{recetaId}
 *
 * Los ingredientes van embebidos como array dentro del documento,
 * siguiendo la desnormalización habitual en Firestore (RF3).
 */
@Serializable
data class Receta(
    val id: String = "",
    val nombre: String = "",
    val origen: OrigenReceta = OrigenReceta.PROPIA,
    val idExterno: String? = null,
    val ingredientes: List<Ingrediente> = emptyList(),
    val pasos: List<String> = emptyList(),
    val imagenUrl: String? = null,
    val tiempoPreparacionMin: Int? = null,
    val raciones: Int = 4,
    val etiquetasDieta: List<String> = emptyList(),
    val creadaPor: String = "",
    val fechaCreacion: Instant? = null
)