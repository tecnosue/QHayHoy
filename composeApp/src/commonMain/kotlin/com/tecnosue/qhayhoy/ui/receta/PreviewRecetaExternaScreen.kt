package com.tecnosue.qhayhoy.ui.receta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel

/**
 * Pantalla de previsualización de una receta externa antes de importarla
 * a la Casa activa (RF3.3).
 *
 * Muestra la receta completa (imagen, ingredientes, etiquetas) y ofrece
 * un botón principal "Añadir a mi Casa". Al confirmar, la receta se guarda
 * en la subcolección de recetas con origen EXTERNA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewRecetaExternaScreen(
    authViewModel: AuthViewModel,
    recetaViewModel: RecetaViewModel,
    idExterno: String,
    onVolver: () -> Unit,
    onImportada: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val recetaState by recetaViewModel.uiState.collectAsState()

    val casaActivaId = authState.usuarioActual?.casaActivaId
    val usuarioId = authState.usuarioActual?.id

    // Cargar el detalle al entrar
    LaunchedEffect(idExterno) {
        recetaViewModel.cargarDetalleReceptaExterna(idExterno)
    }

    // Volver al listado al importar con éxito
    LaunchedEffect(recetaState.importacionExitosa) {
        if (recetaState.importacionExitosa) {
            recetaViewModel.limpiarImportacionExitosa()
            onImportada()
        }
    }

    val receta = recetaState.recetaExternaSeleccionada

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vista previa") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when {
            recetaState.cargandoDetalleExterno -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            receta == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = recetaState.errorExternas ?: "No se pudo cargar la receta",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .safeContentPadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Imagen grande arriba
                    if (!receta.imagenUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.4f)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = receta.imagenUrl,
                                contentDescription = receta.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Nombre
                        Text(
                            text = receta.nombre,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Etiquetas y meta
                        if (receta.etiquetasDieta.isNotEmpty()) {
                            Text(
                                text = receta.etiquetasDieta.joinToString(" • ") {
                                    it.replaceFirstChar { c -> c.uppercase() }
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "Receta externa importable a tu Casa",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        // Ingredientes
                        Text(
                            text = "Ingredientes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (receta.ingredientes.isEmpty()) {
                            Text(
                                text = "Esta receta no detalla ingredientes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    receta.ingredientes.forEachIndexed { i, ing ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "•  ${ing.nombre}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (ing.cantidad > 0.0 || ing.unidad.isNotBlank()) {
                                                Text(
                                                    text = formatearMedida(ing.cantidad, ing.unidad),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (i < receta.ingredientes.lastIndex) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Aviso de origen
                        Text(
                            text = "Nota: las recetas se importan en su idioma original (inglés) " +
                                    "desde TheMealDB. Una vez importadas, podrás editarlas en tu Casa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (recetaState.errorExternas != null) {
                            Text(
                                text = recetaState.errorExternas ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón importar
                        Button(
                            onClick = {
                                if (casaActivaId != null && usuarioId != null) {
                                    recetaViewModel.importarRecetaExterna(casaActivaId, usuarioId)
                                }
                            },
                            enabled = !recetaState.importandoReceta &&
                                    casaActivaId != null &&
                                    usuarioId != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (recetaState.importandoReceta) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Añadir a mi Casa")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Formatea cantidad+unidad de forma similar al detalle de receta propia.
 */
private fun formatearMedida(cantidad: Double, unidad: String): String {
    val cantidadStr = if (cantidad % 1.0 == 0.0) {
        cantidad.toInt().toString()
    } else {
        cantidad.toString()
    }
    return if (unidad.isBlank()) cantidadStr else "$cantidadStr $unidad"
}