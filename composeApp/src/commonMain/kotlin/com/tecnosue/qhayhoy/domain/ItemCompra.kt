package com.tecnosue.qhayhoy.domain

import kotlinx.serialization.Serializable

/**
 * Representa un ingrediente agrupado dentro de la lista de la compra (RF6).
 * En Firestore se guardará embebido en un array dentro del documento de la lista
 * o como subcolección (según lo definamos en el Repositorio).
 */
@Serializable
data class ItemCompra(
    val ingrediente: String = "", // Nombre unificado del ingrediente
    val cantidad: Double = 0.0,   // Cantidad recalculada según comensales
    val unidad: String = "",
    val comprado: Boolean = false,
    val semanaId: String = ""     // Referencia para saber a qué semana pertenece
)
