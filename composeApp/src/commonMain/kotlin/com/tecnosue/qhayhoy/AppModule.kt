package com.tecnosue.qhayhoy

import com.tecnosue.qhayhoy.data.CasaRepository
import com.tecnosue.qhayhoy.data.CatalogoLoader
import com.tecnosue.qhayhoy.data.ListaCompraRepository
import com.tecnosue.qhayhoy.data.MenuRepository
import com.tecnosue.qhayhoy.data.RecetaExternaRepository
import com.tecnosue.qhayhoy.data.RecetaRepository
import com.tecnosue.qhayhoy.data.UsuarioRepository
import com.tecnosue.qhayhoy.data.mealdb.MealDbApiClient
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel
import com.tecnosue.qhayhoy.ui.casa.CasaViewModel
import com.tecnosue.qhayhoy.ui.listacompra.ListaCompraViewModel
import com.tecnosue.qhayhoy.ui.menu.MenuSemanalViewModel
import com.tecnosue.qhayhoy.ui.receta.RecetaViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.tecnosue.qhayhoy.data.translator.MyMemoryClient
import com.tecnosue.qhayhoy.data.translator.TraductorRepository

/**
 * Módulo principal de Koin.
 *
 * Declara todas las dependencias de la aplicación y el scope en el
 * que se gestionan. Centraliza la configuración de inyección de
 * dependencias para facilitar el testing y la evolución del proyecto.
 */
val appModule = module {

    // --- Cliente HTTP de TheMealDB ---
    single { MealDbApiClient() }

    // --- Repositorios (capa de datos) ---
    // single { ... } → una única instancia compartida durante toda la app.
    // Ideal para repositorios: son stateless y pesa poco mantenerlos vivos.
    single { UsuarioRepository() }
    single { RecetaRepository() }
    single { CatalogoLoader(get()) }
    single { CasaRepository(get()) }
    single { MenuRepository() }
    single { RecetaExternaRepository(get(), get()) }
    single { ListaCompraRepository() }
    single { MyMemoryClient(emailContacto = "qhayhoymenusparanopensar@gmail.com") }
    single { TraductorRepository(get()) }




    // --- ViewModels ---
    // viewModelOf(::Clase) → crea una nueva instancia por cada pantalla
    // que lo solicite, respetando el ciclo de vida del ViewModel.
    // Koin resuelve automáticamente los parámetros del constructor.
    viewModelOf(::AuthViewModel)
    viewModelOf(::CasaViewModel)
    viewModelOf(::RecetaViewModel)
    viewModelOf(::MenuSemanalViewModel)
    viewModelOf(::ListaCompraViewModel)


}

