package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.domain.ListaCompraSemana
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para la lista de la compra (RF6).
 *
 * Persiste únicamente el estado 'comprado' de cada item en:
 *   casas/{casaId}/listaCompra/{semanaId}
 *
 * El resto de la lista (ingredientes y cantidades) se calcula al vuelo
 * mediante CalculadorListaCompra.
 */
class ListaCompraRepository {

    private val firestore = Firebase.firestore

    private fun listaRef(casaId: String) =
        firestore.collection("casas").document(casaId).collection("listaCompra")

    /**
     * Observa qué items están marcados como comprados en una semana.
     * Si el documento no existe aún, devuelve lista vacía.
     */
    fun observarComprados(casaId: String, semanaId: String): Flow<List<String>> {
        return listaRef(casaId).document(semanaId).snapshots.map { snapshot ->
            if (snapshot.exists) {
                snapshot.data(ListaCompraSemana.serializer()).itemsComprados
            } else {
                emptyList()
            }
        }
    }

    /**
     * Marca o desmarca un item como comprado. Crea el documento si no existe.
     */
    suspend fun cambiarEstadoComprado(
        casaId: String,
        semanaId: String,
        clave: String,
        comprado: Boolean
    ) {
        val doc = listaRef(casaId).document(semanaId)
        val snapshot = doc.get()

        val estadoActual = if (snapshot.exists) {
            snapshot.data(ListaCompraSemana.serializer())
        } else {
            ListaCompraSemana(semanaId = semanaId, itemsComprados = emptyList())
        }

        val listaNueva = if (comprado) {
            if (clave in estadoActual.itemsComprados) estadoActual.itemsComprados
            else estadoActual.itemsComprados + clave
        } else {
            estadoActual.itemsComprados - clave
        }

        doc.set(estadoActual.copy(itemsComprados = listaNueva))
    }
}