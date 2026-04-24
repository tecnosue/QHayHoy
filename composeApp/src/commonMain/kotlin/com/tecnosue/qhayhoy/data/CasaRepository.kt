package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.domain.Casa
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.random.Random

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
     * Añade al creador como primer miembro y actualiza su lista de casas.
     */
    suspend fun crearCasa(nombre: String, creadorId: String): Casa {
        // 1. Generar ID y código de invitación
        val casaId = firestore.collection("casas").document.id
        val codigoInvitacion = generarCodigoInvitacion()

        // 2. Construir el objeto Casa
        val casa = Casa(
            id = casaId,
            nombre = nombre,
            codigoInvitacion = codigoInvitacion,
            fechaCreacion = Clock.System.now(),
            miembrosIds = listOf(creadorId)
        )

        // 3. Guardar el documento de la Casa
        firestore.collection("casas")
            .document(casaId)
            .set(casa)

        // 4. Añadir el ID de la Casa al usuario (relación N:M)
        firestore.collection("usuarios")
            .document(creadorId)
            .update("casasIds" to listOf(casaId))
        // NOTA: este update sobrescribe. En la Iteración siguiente
        // lo mejoraremos para hacer append si el usuario ya tiene casas.

        return casa
    }

    /**
     * Une al usuario a una Casa existente mediante su código de invitación.
     * Actualiza tanto miembrosIds de la Casa como casasIds del usuario.
     */
    suspend fun unirseACasa(codigoInvitacion: String, usuarioId: String): Casa {
        // 1. Buscar la Casa por el código
        val query = firestore.collection("casas")
            .where { "codigoInvitacion" equalTo codigoInvitacion }
            .get()

        val documento = query.documents.firstOrNull()
            ?: throw Exception("No existe ninguna Casa con ese código")

        val casa = documento.data(Casa.serializer())

        // 2. Verificar que el usuario no sea ya miembro
        if (usuarioId in casa.miembrosIds) {
            throw Exception("Ya eres miembro de esta Casa")
        }

        // 3. Añadir el usuario a la lista de miembros
        val nuevosMiembros = casa.miembrosIds + usuarioId
        firestore.collection("casas")
            .document(casa.id)
            .update("miembrosIds" to nuevosMiembros)

        // 4. Añadir la Casa a las casasIds del usuario
        firestore.collection("usuarios")
            .document(usuarioId)
            .update("casasIds" to listOf(casa.id))
        // NOTA: igual que arriba, esto sobrescribe — se refinará después.

        return casa.copy(miembrosIds = nuevosMiembros)
    }

    /**
     * Observa las Casas a las que pertenece un usuario en tiempo real.
     * Cualquier cambio en Firestore (nuevo miembro, cambio de nombre...)
     * se propaga automáticamente.
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