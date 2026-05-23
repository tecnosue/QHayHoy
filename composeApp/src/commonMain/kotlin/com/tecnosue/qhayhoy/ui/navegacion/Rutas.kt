package com.tecnosue.qhayhoy.ui.navegacion

import kotlinx.serialization.Serializable

/**
 * Rutas de navegación de la aplicación.
 *
 * Cada objeto/data class representa una pantalla del flujo de navegación.
 * Navigation Compose usa serialización para gestionar las rutas de forma
 * type-safe (sin cadenas de texto propensas a errores).
 */
object Rutas {

    @Serializable
    data object Login

    @Serializable
    data object Registro

    @Serializable
    data object GestionCasa

    @Serializable
    data object Principal

    @Serializable
    data object ListaRecetas

    /**
     * Ruta que acepta un ID opcional de receta.
     *  - Si recetaId es null → crear nueva receta
     *  - Si tiene valor → editar esa receta
     */
    @Serializable
    data class EditorReceta(val recetaId: String? = null)

    @Serializable
    data class DetalleReceta(val recetaId: String)

    // --- Descubrir recetas externas (RF3.2 / RF3.3) ---

    @Serializable
    data object DescubrirRecetas

    @Serializable
    data class PreviewRecetaExterna(val idExterno: String)

    // --- Menú Semanal (RF4) ---

    @Serializable
    data class MenuSemanal(
        val semanaId: String,
        val casaId: String,
        val miembrosIds: List<String>  // si Serializable lo permite; si no, pásalo como string separado por comas
    )
    @Serializable
    data class ListaCompra(
        val casaId: String,
        val semanaId: String
    )
}