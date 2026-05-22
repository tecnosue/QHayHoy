package com.tecnosue.qhayhoy.data

import com.tecnosue.qhayhoy.domain.MenuSemanal
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
}