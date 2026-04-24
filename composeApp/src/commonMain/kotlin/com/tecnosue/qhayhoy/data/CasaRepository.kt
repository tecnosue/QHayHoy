package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.domain.Casa
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Repositorio encargado de gestionar todas las operaciones relacionadas
 * con las Casas (hogares compartidos).
 *
 * Centraliza la comunicación con la colección 'casas' de Cloud Firestore
 * y mantiene la relación N:M con los usuarios mediante los arrays
 * miembrosIds (en Casa) y casasIds (en Usuario).
 */
class CasaRepository {

    private val firestore = Firebase.firestore

    /**
     * Crea una nueva Casa en Firestore.
     * Genera un código de invitación único de 6 caracteres alfanuméricos.
     * Añade al creador como primer miembro, actualiza su lista de casas
     * y la marca como Casa activa.
     */
    suspend fun crearCasa(nombre: String, creadorId: String): Casa {
        val casaId = firestore.collection("casas").document.id
        val codigoInvitacion = generarCodigoInvitacion()

        val casa = Casa(
            id = casaId,
            nombre = nombre,
            codigoInvitacion = codigoInvitacion,
            fechaCreacion = Clock.System.now(),
            miembrosIds = listOf(creadorId)
        )

        // Guardar el documento de la Casa
        firestore.collection("casas")
            .document(casaId)
            .set(casa)

        // Añadir la Casa al usuario (sin sobrescribir) y marcarla como activa
        firestore.collection("usuarios")
            .document(creadorId)
            .update(
                "casasIds" to FieldValue.arrayUnion(casaId),
                "casaActivaId" to casaId
            )

        return casa
    }

    /**
     * Une al usuario a una Casa existente mediante su código de invitación.
     * Actualiza miembrosIds de la Casa y casasIds del usuario,
     * y marca la Casa como activa.
     */
    suspend fun unirseACasa(codigoInvitacion: String, usuarioId: String): Casa {
        val query = firestore.collection("casas")
            .where { "codigoInvitacion" equalTo codigoInvitacion }
            .get()

        val documento = query.documents.firstOrNull()
            ?: throw Exception("No existe ninguna Casa con ese código")

        val casa = documento.data(Casa.serializer())

        if (usuarioId in casa.miembrosIds) {
            throw Exception("Ya eres miembro de esta Casa")
        }

        // Añadir al usuario a la Casa (atómico, sin race conditions)
        firestore.collection("casas")
            .document(casa.id)
            .update("miembrosIds" to FieldValue.arrayUnion(usuarioId))

        // Añadir la Casa al usuario y marcarla activa
        firestore.collection("usuarios")
            .document(usuarioId)
            .update(
                "casasIds" to FieldValue.arrayUnion(casa.id),
                "casaActivaId" to casa.id
            )

        return casa.copy(miembrosIds = casa.miembrosIds + usuarioId)
    }

    /**
     * Cambia la Casa activa del usuario a otra de las que ya pertenece.
     */
    suspend fun cambiarCasaActiva(usuarioId: String, casaId: String) {
        firestore.collection("usuarios")
            .document(usuarioId)
            .update("casaActivaId" to casaId)
    }

    /**
     * Observa las Casas a las que pertenece un usuario en tiempo real.
     */
    fun observarCasasDelUsuario(usuarioId: String): Flow<List<Casa>> {
        return firestore.collection("casas")
            .where { "miembrosIds" contains usuarioId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data(Casa.serializer()) }
            }
    }

    /**
     * Genera un código de invitación aleatorio de 6 caracteres
     * alfanuméricos (letras mayúsculas y dígitos).
     */
    private fun generarCodigoInvitacion(): String {
        val caracteres = ('A'..'Z') + ('0'..'9')
        return (1..6).map { caracteres.random() }.joinToString("")
    }
}