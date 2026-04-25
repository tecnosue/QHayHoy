package com.tecnosue.qhayhoy.ui.casa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel

/**
 * Pantalla principal tras seleccionar/crear/unirse a una Casa.
 *
 * En esta Iteración 1 es un placeholder mínimo que confirma que el
 * flujo completo ha funcionado. En iteraciones posteriores aquí se
 * mostrará el menú semanal (Iteración 3).
 */
@Composable
fun PrincipalScreen(
    authViewModel: AuthViewModel,
    casaViewModel: CasaViewModel,
    onCerrarSesion: () -> Unit,
    onIrARecetas: () -> Unit,
    onCambiarDeCasa: () -> Unit

) {
    val authState by authViewModel.uiState.collectAsState()
    val casaState by casaViewModel.uiState.collectAsState()

    // Si se pierde la sesión por cualquier motivo, salimos al Login
    LaunchedEffect(authState.usuarioActual) {
        if (authState.usuarioActual == null) {
            onCerrarSesion()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido a QHayHoy!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hola, ${authState.usuarioActual?.nombre ?: ""}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tarjeta con información de la Casa activa
        casaState.casaSeleccionada?.let { casa ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Text(
                        text = "Código de invitación:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = casa.codigoInvitacion,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Comparte este código para que otros miembros se unan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${casa.miembrosIds.size} miembro(s)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "En próximas iteraciones:\n• Gestión de recetas\n• Menú semanal\n• Lista de la compra",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onIrARecetas,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mis recetas")
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCambiarDeCasa,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cambiar de Casa")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authViewModel.cerrarSesion()
            },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}