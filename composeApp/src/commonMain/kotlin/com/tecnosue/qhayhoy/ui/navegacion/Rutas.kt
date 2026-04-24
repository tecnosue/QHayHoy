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
}