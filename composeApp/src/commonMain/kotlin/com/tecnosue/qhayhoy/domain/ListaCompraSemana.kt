package com.tecnosue.qhayhoy.domain

import kotlinx.serialization.Serializable

/**
 * Documento persistente en Firestore que guarda el estado de la lista
 * de la compra para una semana concreta de una Casa (RF6.3).
 *
 * Se guarda en: casas/{casaId}/listaCompra/{semanaId}
 *
 * Solo guardamos qué items se han marcado como comprados (por su clave).
 * El resto de la lista se calcula al vuelo.
 */
@Serializable
data class ListaCompraSemana(
    val semanaId: String = "",
    val itemsComprados: List<String> = emptyList()  // claves de items marcados
)
