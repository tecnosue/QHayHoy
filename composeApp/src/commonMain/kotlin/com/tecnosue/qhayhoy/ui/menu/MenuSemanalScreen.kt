package com.tecnosue.qhayhoy.ui.menu

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
    onVolver: () -> Unit,
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estado del bottom sheet: par (dia, tipo) cuando está abierto, null cuando cerrado
    var platoASustituir by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(semanaId) {
        viewModel.observarDatos(casaId, semanaId)
    }

    MenuSemanalContent(
        uiState = uiState,
        recetas = uiState.recetas,
        onGenerarMenuClick = {
            viewModel.generarMenuAutomatico(casaId, semanaId, miembrosIds)
        },
        onVolver = onVolver,
        onPlatoClick = onPlatoClick,
        onSustituirClick = { dia, tipo ->
            platoASustituir = dia to tipo
        }
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
    onGenerarMenuClick: () -> Unit,
    onVolver: () -> Unit,
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit,
    onSustituirClick: (dia: String, tipo: String) -> Unit
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
                            onPlatoClick = onPlatoClick,
                            onSustituirClick = onSustituirClick
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
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit,
    onSustituirClick: (dia: String, tipo: String) -> Unit
) {
    val resolverNombre = { id: String ->
        recetas.find { it.id == id }?.nombre ?: "Plato sin asignar"
    }

    val asistenciaComida = asistencias["${dia.name}_COMIDA"]?.size ?: 0
    val asistenciaCena = asistencias["${dia.name}_CENA"]?.size ?: 0

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                asistentes = asistenciaComida,
                onClick = { onPlatoClick(dia.name, "COMIDA", comidaDia.comidaRecetaId) },
                onSustituirClick = { onSustituirClick(dia.name, "COMIDA") }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            PlatoRow(
                tipo = "CENA",
                nombrePlato = resolverNombre(comidaDia.cenaRecetaId),
                asistentes = asistenciaCena,
                onClick = { onPlatoClick(dia.name, "CENA", comidaDia.cenaRecetaId) },
                onSustituirClick = { onSustituirClick(dia.name, "CENA") }
            )
        }
    }
}

@Composable
fun PlatoRow(
    tipo: String,
    nombrePlato: String,
    asistentes: Int,
    onClick: () -> Unit,
    onSustituirClick: () -> Unit
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

        IconButton(onClick = onSustituirClick) {
            Icon(
                imageVector = Lucide.RefreshCw,
                contentDescription = "Sustituir plato",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "👥", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$asistentes",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
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