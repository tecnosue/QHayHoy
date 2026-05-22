package com.tecnosue.qhayhoy.domain

import kotlinx.serialization.Serializable

/**
 * Representa los días de la semana para mapearlos en el menú.
 */
@Serializable
enum class DiaSemana {
    LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
}

/**
 * Representa los IDs de las recetas asignadas a un día concreto.
 * Usamos String vacío o null para indicar que aún no hay receta asignada.
 */
@Serializable
data class ComidaDia(
    val comidaRecetaId: String = "",
    val cenaRecetaId: String = ""
)

/**
 * Entidad principal del menú semanal (RF4).
 * En Firestore se guardará en: casas/{casaId}/menuSemanal/{semanaId}
 * * @param id El identificador será la fecha del lunes (ej. "2026-04-27")
 * @param asistencias Mapa donde la clave es "DIA_TIPO" (ej. "LUNES_COMIDA")
 * y el valor es la lista de UIDs confirmados.
 */
@Serializable
data class MenuSemanal(
    val id: String = "",
    val fechaInicioMillis: Long = 0L, // Usamos Long (epoch) para facilitar la serialización multiplataforma
    val dias: Map<String, ComidaDia> = emptyMap(),
    val asistencias: Map<String, List<String>> = emptyMap()
)