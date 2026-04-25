package com.tecnosue.qhayhoy.ui.receta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Compass
import com.tecnosue.qhayhoy.domain.OrigenReceta
import com.tecnosue.qhayhoy.domain.Receta
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel

/**
 * Pantalla de listado de recetas de la Casa activa.
 *
 * Muestra las recetas en formato tarjeta y permite:
 *  - Ver el detalle de una receta (pulsando sobre ella)
 *  - Crear una receta nueva (botón flotante)
 *
 * Satisface los requisitos de consulta y navegación de recetas (RF3.1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaRecetasScreen(
    authViewModel: AuthViewModel,
    recetaViewModel: RecetaViewModel,
    onVolver: () -> Unit,
    onNuevaReceta: () -> Unit,
    onRecetaSeleccionada: (String) -> Unit,
    onIrADescubrir: () -> Unit

) {
    val authState by authViewModel.uiState.collectAsState()
    val recetaState by recetaViewModel.uiState.collectAsState()

    val casaActivaId = authState.usuarioActual?.casaActivaId

    // Cuando tenemos la Casa activa, empezamos a observar sus recetas
    LaunchedEffect(casaActivaId) {
        if (casaActivaId != null) {
            recetaViewModel.observarRecetas(casaActivaId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis recetas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onIrADescubrir) {
                        Icon(
                            imageVector = Lucide.Compass,
                            contentDescription = "Descubrir recetas"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val usuarioId = authState.usuarioActual?.id ?: return@ExtendedFloatingActionButton
                    recetaViewModel.iniciarNuevaReceta(usuarioId)
                    onNuevaReceta()
                },
                icon = { Icon(imageVector = Lucide.Plus, contentDescription = null) },
                text = { Text("Nueva receta") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .safeContentPadding()
                .padding(horizontal = 16.dp)
        ) {
            if (casaActivaId == null) {
                // No hay Casa activa (caso raro, pero lo cubrimos)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecciona o crea una Casa primero",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                return@Column
            }

            if (recetaState.recetas.isEmpty()) {
                // Lista vacía: mensaje amigable
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Aún no hay recetas",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pulsa \"+ Nueva receta\" para añadir la primera",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Lista con recetas
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
                ) {
                    items(recetaState.recetas, key = { it.id }) { receta ->
                        TarjetaReceta(
                            receta = receta,
                            onClick = { onRecetaSeleccionada(receta.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta que representa visualmente una receta en la lista.
 */
@Composable
private fun TarjetaReceta(
    receta: Receta,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = receta.nombre.ifBlank { "(Sin nombre)" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = etiquetaOrigen(receta.origen),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (receta.tiempoPreparacionMin != null) {
                    Text(
                        text = "  •  ${receta.tiempoPreparacionMin} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "  •  ${receta.raciones} raciones",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (receta.ingredientes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${receta.ingredientes.size} ingrediente(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Texto visible para cada tipo de origen de receta.
 */
private fun etiquetaOrigen(origen: OrigenReceta): String {
    return when (origen) {
        OrigenReceta.PROPIA -> "Propia"
        OrigenReceta.CATALOGO -> "Catálogo"
        OrigenReceta.EXTERNA -> "Importada"
    }
}