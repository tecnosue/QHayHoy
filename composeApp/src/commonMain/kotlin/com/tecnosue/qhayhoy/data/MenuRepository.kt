package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.domain.MenuSemanal
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.tecnosue.qhayhoy.domain.ComidaDia

/**
 * Repositorio encargado de gestionar las operaciones sobre el menú semanal
 * de una Casa (RF4 y RF5).
 *
 * Trabaja sobre la subcolección:
 * casas/{casaId}/menuSemanal/{semanaId}
 */
class MenuRepository {

    private val firestore = Firebase.firestore

    /**
     * Referencia a la subcolección de menús de una Casa.
     */
    private fun menuRef(casaId: String) =
        firestore.collection("casas").document(casaId).collection("menuSemanal")

    /**
     * Observa el menú de una semana concreta en tiempo real.
     * Retorna null si la Casa aún no ha generado el menú para esa semana.
     */
    fun observarMenuDeLaSemana(casaId: String, semanaId: String): Flow<MenuSemanal?> {
        return menuRef(casaId).document(semanaId)
            .snapshots
            .map { snapshot ->
                if (snapshot.exists) {
                    snapshot.data(MenuSemanal.serializer())
                } else {
                    null
                }
            }
    }

    /**
     * Guarda (o sobrescribe) el menú semanal completo.
     * Se usará cuando se genere el menú automáticamente por primera vez.
     */
    suspend fun guardarMenu(casaId: String, menu: MenuSemanal) {
        if (menu.id.isBlank()) {
            throw Exception("El menú debe tener un semanaId válido (ej. YYYY-MM-DD)")
        }
        menuRef(casaId).document(menu.id).set(menu)
    }

    /**
     * Actualiza el mapa de asistencias para una comida concreta.
     * Esto disparará automáticamente los listeners reactivos de Firestore.
     */
    suspend fun actualizarAsistencia(
        casaId: String,
        semanaId: String,
        menuActualizado: MenuSemanal
    ) {
        // En Firestore NoSQL, sobrescribimos el documento con el nuevo estado de asistencias.
        // Al estar observando con .snapshots, la UI reaccionará sola.
        menuRef(casaId).document(semanaId).set(menuActualizado)
    }

    /**
     * Sustituye un plato concreto (comida o cena de un día) por una receta distinta.
     * Lee el menú actual, modifica solo el día/tipo afectado y guarda.
     *
     * @param tipo "COMIDA" o "CENA"
     */
    suspend fun sustituirPlato(
        casaId: String,
        semanaId: String,
        dia: String,
        tipo: String,
        nuevaRecetaId: String
    ) {
        val snapshot = menuRef(casaId).document(semanaId).get()
        if (!snapshot.exists) {
            throw Exception("No existe menú para esta semana")
        }
        val menuActual = snapshot.data(MenuSemanal.serializer())

        val comidaDiaActual = menuActual.dias[dia] ?: com.tecnosue.qhayhoy.domain.ComidaDia()
        val comidaDiaNuevo = when (tipo) {
            "COMIDA" -> comidaDiaActual.copy(comidaRecetaId = nuevaRecetaId)
            "CENA"   -> comidaDiaActual.copy(cenaRecetaId = nuevaRecetaId)
            else     -> throw Exception("Tipo de comida no válido: $tipo")
        }

        val diasActualizados = menuActual.dias.toMutableMap()
        diasActualizados[dia] = comidaDiaNuevo

        val menuActualizado = menuActual.copy(dias = diasActualizados)
        menuRef(casaId).document(semanaId).set(menuActualizado)
    }
}