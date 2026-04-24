package com.tecnosue.qhayhoy.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


/**
 * Representa un usuario registrado en la aplicación.
 *
 * Un usuario puede pertenecer a varias Casas simultáneamente
 * (por ejemplo, su piso habitual y la casa familiar de fin de semana).
 *
 * Mapea con la colección Firestore: usuarios/{usuarioId}
 */
@Serializable
data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val fechaRegistro: Instant? = null,
    val casasIds: List<String> = emptyList()
)