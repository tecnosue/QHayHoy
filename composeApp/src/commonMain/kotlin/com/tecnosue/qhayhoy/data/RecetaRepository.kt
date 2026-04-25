package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.domain.OrigenReceta
import com.tecnosue.qhayhoy.domain.Receta
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Repositorio encargado de gestionar las operaciones CRUD sobre recetas
 * pertenecientes a una Casa (RF3.1).
 *
 * Todas las operaciones trabajan sobre la subcolección:
 *   casas/{casaId}/recetas/{recetaId}
 *
 * De esta forma, las recetas siempre están asociadas a una Casa concreta
 * y las reglas de seguridad se aplican de forma propagada desde el
 * documento padre (RNF3).
 */
class RecetaRepository {

    private val firestore = Firebase.firestore

    /**
     * Referencia a la subcolección de recetas de una Casa concreta.
     * Centralizar esta referencia evita repetir la ruta en cada método
     * y facilita cualquier cambio futuro de estructura.
     */
    private fun recetasRef(casaId: String) =
        firestore.collection("casas").document(casaId).collection("recetas")

    /**
     * Observa todas las recetas de una Casa en tiempo real.
     * Cualquier cambio (nueva receta, edición, borrado) se propaga
     * automáticamente a los dispositivos de todos los miembros (RF7.1).
     */
    fun observarRecetasDeCasa(casaId: String): Flow<List<Receta>> {
        return recetasRef(casaId)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data(Receta.serializer()) }
            }
    }

    /**
     * Crea una nueva receta en la Casa indicada.
     * El ID lo genera Firestore automáticamente.
     */
    suspend fun crearReceta(casaId: String, receta: Receta): Receta {
        val ref = recetasRef(casaId).document
        val recetaConId = receta.copy(
            id = ref.id,
            fechaCreacion = Clock.System.now()
        )
        ref.set(recetaConId)
        return recetaConId
    }

    /**
     * Actualiza una receta existente.
     * Solo tiene sentido para recetas propias (no para las precargadas
     * del catálogo, que son de solo lectura).
     */
    suspend fun actualizarReceta(casaId: String, receta: Receta) {
        if (receta.id.isBlank()) {
            throw Exception("No se puede actualizar una receta sin ID")
        }
        recetasRef(casaId).document(receta.id).set(receta)
    }

    /**
     * Elimina una receta de la Casa.
     */
    suspend fun eliminarReceta(casaId: String, recetaId: String) {
        recetasRef(casaId).document(recetaId).delete()
    }

    /**
     * Obtiene una receta concreta por su ID.
     * Útil cuando la pantalla de detalle se abre desde una referencia
     * (por ejemplo, desde el menú semanal).
     */
    suspend fun obtenerReceta(casaId: String, recetaId: String): Receta? {
        val snapshot = recetasRef(casaId).document(recetaId).get()
        return if (snapshot.exists) {
            snapshot.data(Receta.serializer())
        } else {
            null
        }
    }

    /**
     * Importa una receta externa (desde TheMealDB u otra fuente) a la Casa.
     * Crea un nuevo documento en la Casa con origen EXTERNA y
     * conservando el idExterno original para trazabilidad.
     *
     * El usuario puede haber editado la receta antes de guardarla.
     */
    suspend fun importarRecetaExterna(
        casaId: String,
        receta: Receta,
        idExterno: String
    ): Receta {
        val recetaExterna = receta.copy(
            origen = OrigenReceta.EXTERNA,
            idExterno = idExterno
        )
        return crearReceta(casaId, recetaExterna)
    }
}