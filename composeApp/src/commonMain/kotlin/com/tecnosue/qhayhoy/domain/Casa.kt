package com.tecnosue.qhayhoy.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Representa un hogar compartido (Casa) dentro de la aplicación.
 *
 * Una Casa es el contenedor de toda la información operativa:
 * recetas, menús semanales y listas de la compra.
 * Todos los miembros de la Casa tienen los mismos permisos
 * para facilitar la colaboración (RF2).
 *
 * Mapea con la colección Firestore: casas/{casaId}
 */
@Serializable
data class Casa(
    val id: String = "",
    val nombre: String = "",
    val codigoInvitacion: String = "",
    val fechaCreacion: Instant? = null,
    val miembrosIds: List<String> = emptyList()
)