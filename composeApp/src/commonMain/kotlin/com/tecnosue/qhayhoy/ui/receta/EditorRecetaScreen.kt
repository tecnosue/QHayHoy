package com.tecnosue.qhayhoy.ui.receta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.tecnosue.qhayhoy.domain.Ingrediente
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel

/**
 * Pantalla para crear o editar una receta.
 *
 * - Si se invoca con recetaId null, se abre en modo "nueva receta" usando
 *   la receta en blanco que el ViewModel ha inicializado previamente.
 * - Si se invoca con un recetaId válido, carga esa receta al editor
 *   (siempre que exista en la lista de recetas observadas).
 *
 * Satisface los requisitos de creación y edición de recetas (RF3.1).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorRecetaScreen(
    authViewModel: AuthViewModel,
    recetaViewModel: RecetaViewModel,
    recetaId: String?,
    onVolver: () -> Unit,
    onGuardado: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val recetaState by recetaViewModel.uiState.collectAsState()

    val casaActivaId = authState.usuarioActual?.casaActivaId

    // Si venimos con un ID concreto, cargamos esa receta al editor
    LaunchedEffect(recetaId) {
        if (recetaId != null) {
            val receta = recetaState.recetas.firstOrNull { it.id == recetaId }
            if (receta != null) {
                recetaViewModel.cargarRecetaParaEditar(receta)
            }
        }
    }

    // Navegar de vuelta al guardar con éxito
    LaunchedEffect(recetaState.operacionExitosa) {
        if (recetaState.operacionExitosa) {
            recetaViewModel.limpiarOperacionExitosa()
            onGuardado()
        }
    }

    val receta = recetaState.recetaEnEdicion
    val esNueva = receta.id.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esNueva) "Nueva receta" else "Editar receta") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Volver"
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
            // --- Datos básicos ---
            OutlinedTextField(
                value = receta.nombre,
                onValueChange = { recetaViewModel.onNombreChange(it) },
                label = { Text("Nombre de la receta") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = if (receta.raciones > 0) receta.raciones.toString() else "",
                    onValueChange = {
                        val num = it.toIntOrNull() ?: 0
                        recetaViewModel.onRacionesChange(num)
                    },
                    label = { Text("Raciones") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = receta.tiempoPreparacionMin?.toString() ?: "",
                    onValueChange = {
                        val num = it.toIntOrNull()
                        recetaViewModel.onTiempoChange(num)
                    },
                    label = { Text("Tiempo (min)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // --- Ingredientes ---
            SeccionIngredientes(
                ingredientes = receta.ingredientes,
                onAgregar = { recetaViewModel.agregarIngrediente() },
                onActualizar = { indice, ing -> recetaViewModel.actualizarIngrediente(indice, ing) },
                onEliminar = { indice -> recetaViewModel.eliminarIngrediente(indice) }
            )

            // --- Pasos ---
            SeccionPasos(
                pasos = receta.pasos,
                onAgregar = { recetaViewModel.agregarPaso() },
                onActualizar = { indice, texto -> recetaViewModel.actualizarPaso(indice, texto) },
                onEliminar = { indice -> recetaViewModel.eliminarPaso(indice) }
            )

            // --- Error ---
            if (recetaState.error != null) {
                Text(
                    text = recetaState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // --- Botón guardar ---
            Button(
                onClick = {
                    if (casaActivaId != null) {
                        recetaViewModel.guardarReceta(casaActivaId)
                    }
                },
                enabled = !recetaState.cargando && casaActivaId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (recetaState.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (esNueva) "Crear receta" else "Guardar cambios")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Sección de ingredientes con lista dinámica editable.
 */
@Composable
private fun SeccionIngredientes(
    ingredientes: List<Ingrediente>,
    onAgregar: () -> Unit,
    onActualizar: (Int, Ingrediente) -> Unit,
    onEliminar: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ingredientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (ingredientes.isEmpty()) {
                Text(
                    text = "No hay ingredientes todavía",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ingredientes.forEachIndexed { indice, ing ->
                    FilaIngrediente(
                        ingrediente = ing,
                        onCambio = { onActualizar(indice, it) },
                        onEliminar = { onEliminar(indice) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            OutlinedButton(
                onClick = onAgregar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Lucide.Plus, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir ingrediente")
            }
        }
    }
}

/**
 * Fila editable de un ingrediente: nombre + cantidad + unidad + botón borrar.
 */
@Composable
private fun FilaIngrediente(
    ingrediente: Ingrediente,
    onCambio: (Ingrediente) -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = ingrediente.nombre,
            onValueChange = { onCambio(ingrediente.copy(nombre = it)) },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.weight(2f)
        )
        OutlinedTextField(
            value = if (ingrediente.cantidad > 0.0) ingrediente.cantidad.toString() else "",
            onValueChange = {
                val num = it.toDoubleOrNull() ?: 0.0
                onCambio(ingrediente.copy(cantidad = num))
            },
            label = { Text("Cant.") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = ingrediente.unidad,
            onValueChange = { onCambio(ingrediente.copy(unidad = it)) },
            label = { Text("Ud.") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onEliminar) {
            Icon(
                imageVector = Lucide.Trash2,
                contentDescription = "Eliminar ingrediente",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Sección de pasos con lista dinámica editable.
 */
@Composable
private fun SeccionPasos(
    pasos: List<String>,
    onAgregar: () -> Unit,
    onActualizar: (Int, String) -> Unit,
    onEliminar: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pasos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (pasos.isEmpty()) {
                Text(
                    text = "No hay pasos todavía",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                pasos.forEachIndexed { indice, texto ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${indice + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, end = 8.dp)
                        )
                        OutlinedTextField(
                            value = texto,
                            onValueChange = { onActualizar(indice, it) },
                            label = { Text("Descripción del paso") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onEliminar(indice) }) {
                            Icon(
                                imageVector = Lucide.Trash2,
                                contentDescription = "Eliminar paso",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            OutlinedButton(
                onClick = onAgregar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Lucide.Plus, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir paso")
            }
        }
    }
}