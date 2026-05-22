package com.tecnosue.qhayhoy.ui.menu

import androidx.compose.foundation.background
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
import com.tecnosue.qhayhoy.domain.ComidaDia
import com.tecnosue.qhayhoy.domain.DiaSemana
import com.tecnosue.qhayhoy.domain.MenuSemanal
import com.tecnosue.qhayhoy.domain.Receta
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

/**
 * COMPONENTE STATEFUL
 * Se encarga de conectarse con el ViewModel, recolectar el estado
 * y pasar los datos limpios al componente visual.
 */

@Composable
fun MenuSemanalScreen(
    viewModel: MenuSemanalViewModel,
    casaId: String,
    semanaId: String,
    miembrosIds: List<String>,
    onVolver: () -> Unit,
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit // Navegación al detalle
) {
    // Recolectamos el estado de forma reactiva
    val uiState by viewModel.uiState.collectAsState()

    // Observamos el menú al entrar en la pantalla
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
        onPlatoClick = onPlatoClick
    )
}

/**
 * COMPONENTE STATELESS
 * Puramente visual. No sabe qué es un ViewModel ni Firebase.
 * Ideal para renderizar Previews fácilmente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSemanalContent(
    uiState: MenuSemanalUiState,
    recetas: List<Receta>,
    onGenerarMenuClick: () -> Unit,
    onVolver: () -> Unit,
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit
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
                color = Color.Gray,
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
                    Text("Aún no hay menú para esta semana.\n¡Pulsa generar!", color = Color.Gray)
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
                            onPlatoClick = onPlatoClick
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
    onPlatoClick: (dia: String, tipo: String, recetaId: String) -> Unit
) {
    val resolverNombre = { id: String ->
        recetas.find { it.id == id }?.nombre ?: "Plato sin asignar"
    }

    val asistenciaComida = asistencias["${dia.name}_COMIDA"]?.size ?: 0
    val asistenciaCena = asistencias["${dia.name}_CENA"]?.size ?: 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                onClick = { onPlatoClick(dia.name, "COMIDA", comidaDia.comidaRecetaId) }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            PlatoRow(
                tipo = "CENA",
                nombrePlato = resolverNombre(comidaDia.cenaRecetaId),
                asistentes = asistenciaCena,
                onClick = { onPlatoClick(dia.name, "CENA", comidaDia.cenaRecetaId) }
            )
        }
    }
}

@Composable
fun PlatoRow(
    tipo: String,
    nombrePlato: String,
    asistentes: Int,
    onClick: () -> Unit
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
                color = Color.Gray
            )
            Text(
                text = nombrePlato,
                fontSize = 15.sp,
                color = Color.DarkGray
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "👥", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$asistentes",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
