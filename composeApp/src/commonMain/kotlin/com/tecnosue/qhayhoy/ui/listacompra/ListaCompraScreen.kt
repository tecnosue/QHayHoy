package com.tecnosue.qhayhoy.ui.listacompra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.tecnosue.qhayhoy.domain.ItemCompra

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaCompraScreen(
    viewModel: ListaCompraViewModel,
    casaId: String,
    semanaId: String,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(semanaId) {
        viewModel.observarLista(casaId, semanaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de la compra") },
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                !uiState.menuExiste -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aún no hay menú para esta semana.\nGenera el menú primero.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                uiState.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay ingredientes que comprar.\nVerifica las asistencias del menú.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    val pendientes = uiState.items.count { !it.comprado }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$pendientes pendiente(s) de ${uiState.items.size}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(uiState.items, key = { it.clave }) { item ->
                            ItemCompraRow(
                                item = item,
                                onToggle = { nuevoEstado ->
                                    viewModel.cambiarEstadoComprado(
                                        casaId = casaId,
                                        semanaId = semanaId,
                                        clave = item.clave,
                                        comprado = nuevoEstado
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCompraRow(
    item: ItemCompra,
    onToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.comprado,
                onCheckedChange = { onToggle(it) }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Medium,
                    color = if (item.comprado) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (item.comprado) TextDecoration.LineThrough else null
                )
                Text(
                    text = formatearCantidad(item.cantidad, item.unidad),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Muestra "350 g", "2 ud", "0.5 l", etc. Quita el ".0" para enteros.
 */
private fun formatearCantidad(cantidad: Double, unidad: String): String {
    val cantidadStr = if (cantidad % 1.0 == 0.0) {
        cantidad.toInt().toString()
    } else {
        // Redondeo visual a 1 decimal
        ((cantidad * 10).toInt() / 10.0).toString()
    }
    return if (unidad.isBlank()) cantidadStr else "$cantidadStr $unidad"
}
