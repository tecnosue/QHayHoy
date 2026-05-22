package com.tecnosue.qhayhoy

import androidx.compose.runtime.Composable
import com.tecnosue.qhayhoy.ui.navegacion.AppNavegacion
import org.koin.compose.KoinApplication
import com.tecnosue.qhayhoy.ui.theme.QHayHoyTheme


/**
 * Entry point de la aplicación (compartido entre Android e iOS).
 *
 * Inicializa Koin con el módulo principal y renderiza el grafo de
 * navegación de la app.
 */
@Composable
fun App() {
    KoinApplication(
        application = {
            modules(appModule)
        }
    ) {
        QHayHoyTheme {
            AppNavegacion()
        }
    }
}