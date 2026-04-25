package com.tecnosue.qhayhoy.ui.receta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.tecnosue.qhayhoy.domain.Receta

/**
 * Pantalla "Descubrir recetas" (RF3.2).
 *
 * Muestra recetas externas obtenidas de TheMealDB en una cuadrícula
 * de 2 columnas. Permite filtrar entre vegetariana y vegana.
 * Al pulsar una receta se navega a la pantalla de previsualización
 * para verla con detalle e importarla a la Casa activa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescubrirRecetasScreen(
    recetaViewModel: RecetaViewModel,
    onVolver: () -> Unit,
    onRecetaSeleccionada: (idExterno: String) -> Unit
) {
    val state by recetaViewModel.uiState.collectAsState()

    // Cargar al entrar la primera vez
    LaunchedEffect(Unit) {
        if (state.recetasExternas.isEmpty() && !state.cargandoExternas) {
            recetaViewModel.cargarRecetasExternas()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Descubrir recetas") },
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
        ) {
            // Filtros de dieta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filtroDieta == FiltroDieta.VEGETARIANA,
                    onClick = { recetaViewModel.cambiarFiltroDieta(FiltroDieta.VEGETARIANA) },
                    label = { Text("Vegetariana") }
                )
                FilterChip(
                    selected = state.filtroDieta == FiltroDieta.VEGANA,
                    onClick = { recetaViewModel.cambiarFiltroDieta(FiltroDieta.VEGANA) },
                    label = { Text("Vegana") }
                )
            }

            when {
                state.cargandoExternas -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.errorExternas != null && state.recetasExternas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No se han podido cargar las recetas",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.errorExternas ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                state.recetasExternas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin resultados",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.recetasExternas, key = { it.idExterno ?: it.nombre }) { receta ->
                            TarjetaRecetaExterna(
                                receta = receta,
                                onClick = {
                                    receta.idExterno?.let(onRecetaSeleccionada)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta visual de una receta externa: imagen + nombre.
 */
@Composable
private fun TarjetaRecetaExterna(
    receta: Receta,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!receta.imagenUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = receta.imagenUrl,
                        contentDescription = receta.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Text(
                text = receta.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                maxLines = 2
            )
        }
    }
}