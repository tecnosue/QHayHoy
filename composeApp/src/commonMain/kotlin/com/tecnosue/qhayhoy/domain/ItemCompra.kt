package com.tecnosue.qhayhoy.domain

import kotlinx.serialization.Serializable

/**
 * Representa un ingrediente agregado en la lista de la compra (RF6).
 *
 * Esta clase se calcula al vuelo a partir del menú semanal, las recetas
 * y las asistencias confirmadas. Solo el campo 'comprado' se persiste en
 * Firestore (en casas/{casaId}/listaCompra/{semanaId}.itemsComprados).
 */
@Serializable
data class ItemCompra(
    val clave: String = "",       // Clave única de agrupación: "tomate_g"
    val nombre: String = "",
    val cantidad: Double = 0.0,
    val unidad: String = "",
    val comprado: Boolean = false
)