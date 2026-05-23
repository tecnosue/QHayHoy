package com.tecnosue.qhayhoy.ui.receta

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import com.tecnosue.qhayhoy.domain.OrigenReceta
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.background

/**
 * Pantalla de detalle de una receta.
 *
 * Muestra toda la información de una receta en modo lectura.
 * Ofrece dos acciones desde la TopAppBar:
 *  - Editar (lápiz): abre el editor con la receta cargada
 *  - Eliminar (papelera): muestra diálogo de confirmación y elimina
 *
 * Si la receta no existe (ya fue eliminada o ID inválido),
 * vuelve automáticamente a la lista.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRecetaScreen(
    authViewModel: AuthViewModel,
    recetaViewModel: RecetaViewModel,
    recetaId: String,
    onVolver: () -> Unit,
    onEditar: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val recetaState by recetaViewModel.uiState.collectAsState()

    val casaActivaId = authState.usuarioActual?.casaActivaId
    val receta = recetaState.recetas.firstOrNull { it.id == recetaId }

    var mostrarDialogoBorrar by remember { mutableStateOf(false) }

    // Si la receta no existe (p.ej. ya borrada), volvemos atrás
    LaunchedEffect(receta, recetaState.operacionExitosa) {
        if (recetaState.operacionExitosa) {
            recetaViewModel.limpiarOperacionExitosa()
            onVolver()
        }
    }

    if (receta == null) {
        // Aún cargando o la receta no existe
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cargando receta...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditar) {
                        Icon(
                            imageVector = Lucide.Pencil,
                            contentDescription = "Editar"
                        )
                    }
                    IconButton(onClick = { mostrarDialogoBorrar = true }) {
                        Icon(
                            imageVector = Lucide.Trash2,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .safeContentPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Imagen de cabecera ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!receta.imagenUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = receta.imagenUrl,
                        contentDescription = receta.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback: inicial grande de la receta
                    Text(
                        text = receta.nombre.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Cabecera con nombre y metadatos
            Text(
                text = receta.nombre,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = etiquetaOrigenLarga(receta.origen),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${receta.raciones} raciones",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (receta.tiempoPreparacionMin != null) {
                    Text(
                        text = "${receta.tiempoPreparacionMin} min",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // --- Ingredientes ---
            Text(
                text = "Ingredientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (receta.ingredientes.isEmpty()) {
                Text(
                    text = "No se han añadido ingredientes",
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
                        receta.ingredientes.forEachIndexed { indice, ing ->
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
                                        text = formatearCantidad(ing.cantidad, ing.unidad),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (indice < receta.ingredientes.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // --- Pasos ---
            Text(
                text = "Pasos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (receta.pasos.isEmpty()) {
                Text(
                    text = "No se han añadido pasos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                receta.pasos.forEachIndexed { indice, paso ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${indice + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = paso,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Mostrar error si ocurrió al borrar
            if (recetaState.error != null) {
                Text(
                    text = recetaState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo de confirmación para borrar
    if (mostrarDialogoBorrar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrar = false },
            title = { Text("Eliminar receta") },
            text = { Text("¿Seguro que quieres eliminar la receta \"${receta.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoBorrar = false
                        if (casaActivaId != null) {
                            recetaViewModel.eliminarReceta(casaActivaId, receta.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Texto largo y descriptivo para cada origen de receta.
 */
private fun etiquetaOrigenLarga(origen: OrigenReceta): String {
    return when (origen) {
        OrigenReceta.PROPIA -> "Receta propia"
        OrigenReceta.CATALOGO -> "Del catálogo"
        OrigenReceta.EXTERNA -> "Importada"
    }
}

/**
 * Da formato amigable a la cantidad y unidad de un ingrediente.
 * Ejemplos: "300 g", "1 unidad", "2.5 l".
 */
private fun formatearCantidad(cantidad: Double, unidad: String): String {
    val cantidadStr = if (cantidad % 1.0 == 0.0) {
        cantidad.toInt().toString()
    } else {
        cantidad.toString()
    }
    return if (unidad.isBlank()) cantidadStr else "$cantidadStr $unidad"
}
