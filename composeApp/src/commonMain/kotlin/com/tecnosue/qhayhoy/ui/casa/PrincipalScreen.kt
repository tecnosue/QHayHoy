package com.tecnosue.qhayhoy.ui.casa

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShoppingCart
import com.composables.icons.lucide.UtensilsCrossed
import com.tecnosue.qhayhoy.domain.obtenerIdSemanaActual
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.IconButton
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Sparkles

/**
 * Pantalla principal tras seleccionar/crear/unirse a una Casa.
 *
 * Actúa como dashboard del hogar: muestra la información de la Casa activa
 * y un grid 2x2 con accesos directos a las funcionalidades principales
 * (menú semanal, lista de la compra, recetas propias y descubrir).
 */
@Composable
fun PrincipalScreen(
    authViewModel: AuthViewModel,
    casaViewModel: CasaViewModel,
    onCerrarSesion: () -> Unit,
    onIrARecetas: () -> Unit,
    onIrADescubrir: () -> Unit,
    onIrAListaCompra: (semanaId: String, casaId: String) -> Unit,
    onCambiarDeCasa: () -> Unit,
    onIrAMenu: (semanaId: String, casaId: String, miembrosIds: List<String>) -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val casaState by casaViewModel.uiState.collectAsState()

    LaunchedEffect(authState.usuarioActual) {
        if (authState.usuarioActual == null) {
            onCerrarSesion()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Saludo ---
        Text(
            text = "¡Hola, ${authState.usuarioActual?.nombre ?: ""}!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // --- Tarjeta de Casa activa ---
        casaState.casaSeleccionada?.let { casa ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Casa activa",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = casa.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Código: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = casa.codigoInvitacion,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "·  ${casa.miembrosIds.size} miembro(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Grid 2x2 de accesos rápidos ---
            val acciones = listOf(
                AccionRapida(
                    titulo = "Menú\nsemanal",
                    icono = Lucide.Calendar,
                    color = MaterialTheme.colorScheme.primary,        // verde oliva
                    onClick = {
                        onIrAMenu(obtenerIdSemanaActual(), casa.id, casa.miembrosIds)
                    }
                ),
                AccionRapida(
                    titulo = "Lista de\nla compra",
                    icono = Lucide.ShoppingCart,
                    color = MaterialTheme.colorScheme.secondary,      // naranja
                    onClick = {
                        onIrAListaCompra(obtenerIdSemanaActual(), casa.id)
                    }
                ),
                AccionRapida(
                    titulo = "Mis\nrecetas",
                    icono = Lucide.UtensilsCrossed,
                    color = MaterialTheme.colorScheme.tertiary,       // naranja apagado
                    onClick = onIrARecetas
                ),
                AccionRapida(
                    titulo = "Descubrir\nrecetas",
                    icono = Lucide.Sparkles,
                    color = MaterialTheme.colorScheme.primary,        // verde otra vez
                    onClick = onIrADescubrir
                )
            )

            // Grid 2x2 manual con dos filas
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaAccion(
                        accion = acciones[0],
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaAccion(
                        accion = acciones[1],
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaAccion(
                        accion = acciones[2],
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaAccion(
                        accion = acciones[3],
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

// --- Acciones secundarias ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCambiarDeCasa,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cambiar de Casa")
            }

            IconButton(
                onClick = { authViewModel.cerrarSesion() }
            ) {
                Icon(
                    imageVector = Lucide.LogOut,
                    contentDescription = "Cerrar sesión",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Datos de una tarjeta de acción rápida del dashboard.
 */
private data class AccionRapida(
    val titulo: String,
    val icono: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

/**
 * Tarjeta cuadrada con icono grande, título y acción al pulsar.
 */
@Composable
private fun TarjetaAccion(
    accion: AccionRapida,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, accion.color.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = accion.onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icono dentro de cuadrado redondeado con fondo tenue del color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = accion.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = accion.icono,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = accion.color
                )
            }

            Text(
                text = accion.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}