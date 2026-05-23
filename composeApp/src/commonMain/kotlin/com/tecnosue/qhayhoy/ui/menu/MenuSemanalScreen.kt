package com.tecnosue.qhayhoy.ui.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.tecnosue.qhayhoy.domain.ComidaDia
import com.tecnosue.qhayhoy.domain.DiaSemana
import com.tecnosue.qhayhoy.domain.Receta
import androidx.compose.material3.Checkbox
import com.composables.icons.lucide.ShoppingCart


/**
 * COMPONENTE STATEFUL
 * Se encarga de conectarse con el ViewModel, recolectar el estado
 * y pasar los datos limpios al componente visual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSemanalScreen(
    viewModel: MenuSemanalViewModel,
    casaId: String,
    semanaId: String,
    miembrosIds: List<String>,
    usuarioActualId: String,
    onVolver: () -> Unit,
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit,
    onIrAListaCompra: () -> Unit

) {
    val uiState by viewModel.uiState.collectAsState()

    var platoASustituir by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(semanaId) {
        viewModel.observarDatos(casaId, semanaId)
    }

    MenuSemanalContent(
        uiState = uiState,
        recetas = uiState.recetas,
        usuarioActualId = usuarioActualId,
        totalMiembros = miembrosIds.size,
        onGenerarMenuClick = {
            viewModel.generarMenuAutomatico(casaId, semanaId, miembrosIds)
        },
        onVolver = onVolver,
        onPlatoClick = onPlatoClick,
        onSustituirClick = { dia, tipo -> platoASustituir = dia to tipo },
        onCambiarMiAsistencia = { dia, tipo, asistira ->
            viewModel.cambiarAsistencia(casaId, semanaId, dia, tipo, usuarioActualId, asistira)
        },
        onIrAListaCompra = onIrAListaCompra


    )

    platoASustituir?.let { (dia, tipo) ->
        SustituirPlatoBottomSheet(
            dia = dia,
            tipo = tipo,
            recetas = uiState.recetas,
            onCerrar = { platoASustituir = null },
            onRecetaSeleccionada = { recetaId ->
                viewModel.sustituirPlato(casaId, semanaId, dia, tipo, recetaId)
                platoASustituir = null
            }
        )
    }
}

/**
 * COMPONENTE STATELESS
 * Puramente visual. No sabe qué es un ViewModel ni Firebase.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSemanalContent(
    uiState: MenuSemanalUiState,
    recetas: List<Receta>,
    usuarioActualId: String,
    totalMiembros: Int,
    onGenerarMenuClick: () -> Unit,
    onVolver: () -> Unit,
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit,
    onSustituirClick: (dia: String, tipo: String) -> Unit,
    onCambiarMiAsistencia: (dia: String, tipo: String, asistira: Boolean) -> Unit,
    onIrAListaCompra: () -> Unit

) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú semanal") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onIrAListaCompra) {
                        Icon(
                            imageVector = Lucide.ShoppingCart,
                            contentDescription = "Lista de la compra"
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Semana actual",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGenerarMenuClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.cargando
            ) {
                Text(
                    text = if (uiState.cargando) "Generando..." else "✨ Generar menú",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.cargando && uiState.menu == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.menu == null && !uiState.cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Aún no hay menú para esta semana.\n¡Pulsa generar!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (uiState.menu != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(DiaSemana.entries) { dia ->
                        val comidaDia = uiState.menu.dias[dia.name] ?: ComidaDia()
                        DiaMenuCard(
                            dia = dia,
                            comidaDia = comidaDia,
                            asistencias = uiState.menu.asistencias,
                            recetas = recetas,
                            usuarioActualId = usuarioActualId,
                            totalMiembros = totalMiembros,
                            onPlatoClick = onPlatoClick,
                            onSustituirClick = onSustituirClick,
                            onCambiarMiAsistencia = onCambiarMiAsistencia
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiaMenuCard(
    dia: DiaSemana,
    comidaDia: ComidaDia,
    asistencias: Map<String, List<String>>,
    recetas: List<Receta>,
    usuarioActualId: String,
    totalMiembros: Int,
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit,
    onSustituirClick: (dia: String, tipo: String) -> Unit,
    onCambiarMiAsistencia: (dia: String, tipo: String, asistira: Boolean) -> Unit
) {
    val resolverNombre = { id: String ->
        recetas.find { it.id == id }?.nombre ?: "Plato sin asignar"
    }

    val asistentesComida = asistencias["${dia.name}_COMIDA"] ?: emptyList()
    val asistentesCena = asistencias["${dia.name}_CENA"] ?: emptyList()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dia.name.lowercase().replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            PlatoRow(
                tipo = "COMIDA",
                nombrePlato = resolverNombre(comidaDia.comidaRecetaId),
                asistentes = asistentesComida.size,
                totalMiembros = totalMiembros,
                yoAsisto = usuarioActualId in asistentesComida,
                onClick = { onPlatoClick(dia.name, "COMIDA", comidaDia.comidaRecetaId) },
                onSustituirClick = { onSustituirClick(dia.name, "COMIDA") },
                onToggleMiAsistencia = { asistira -> onCambiarMiAsistencia(dia.name, "COMIDA", asistira) }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            PlatoRow(
                tipo = "CENA",
                nombrePlato = resolverNombre(comidaDia.cenaRecetaId),
                asistentes = asistentesCena.size,
                totalMiembros = totalMiembros,
                yoAsisto = usuarioActualId in asistentesCena,
                onClick = { onPlatoClick(dia.name, "CENA", comidaDia.cenaRecetaId) },
                onSustituirClick = { onSustituirClick(dia.name, "CENA") },
                onToggleMiAsistencia = { asistira -> onCambiarMiAsistencia(dia.name, "CENA", asistira) }
            )
        }
    }
}

@Composable
fun PlatoRow(
    tipo: String,
    nombrePlato: String,
    asistentes: Int,
    totalMiembros: Int,
    yoAsisto: Boolean,
    onClick: () -> Unit,
    onSustituirClick: () -> Unit,
    onToggleMiAsistencia: (asistira: Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tipo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = nombrePlato,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Indicador informativo: 3/4 comensales
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "👥", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$asistentes/$totalMiembros",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botón sustituir
        IconButton(onClick = onSustituirClick) {
            Icon(
                imageVector = Lucide.RefreshCw,
                contentDescription = "Sustituir plato",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Check de mi asistencia
        Checkbox(
            checked = yoAsisto,
            onCheckedChange = { nuevoValor -> onToggleMiAsistencia(nuevoValor) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SustituirPlatoBottomSheet(
    dia: String,
    tipo: String,
    recetas: List<Receta>,
    onCerrar: () -> Unit,
    onRecetaSeleccionada: (recetaId: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Sustituir plato",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${dia.lowercase().replaceFirstChar { it.uppercase() }} · ${tipo.lowercase().replaceFirstChar { it.uppercase() }}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (recetas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay recetas disponibles",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recetas, key = { it.id }) { receta ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRecetaSeleccionada(receta.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = receta.nombre.ifBlank { "(Sin nombre)" },
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (receta.tiempoPreparacionMin != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${receta.tiempoPreparacionMin} min · ${receta.raciones} raciones",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

