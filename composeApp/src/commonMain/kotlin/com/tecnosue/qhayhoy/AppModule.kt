package com.tecnosue.qhayhoy

import com.tecnosue.qhayhoy.data.CasaRepository
import com.tecnosue.qhayhoy.data.UsuarioRepository
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel
import com.tecnosue.qhayhoy.ui.casa.CasaViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Módulo principal de Koin.
 *
 * Declara todas las dependencias de la aplicación y el scope en el
 * que se gestionan. Centraliza la configuración de inyección de
 * dependencias para facilitar el testing y la evolución del proyecto.
 */
val appModule = module {

    // --- Repositorios (capa de datos) ---
    // single { ... } → una única instancia compartida durante toda la app.
    // Ideal para repositorios: son stateless y pesa poco mantenerlos vivos.
    single { UsuarioRepository() }
    single { CasaRepository() }

    // --- ViewModels ---
    // viewModelOf(::Clase) → crea una nueva instancia por cada pantalla
    // que lo solicite, respetando el ciclo de vida del ViewModel.
    // Koin resuelve automáticamente los parámetros del constructor.
    viewModelOf(::AuthViewModel)
    viewModelOf(::CasaViewModel)
}