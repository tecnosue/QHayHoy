package com.tecnosue.qhayhoy.ui.casa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel

/**
 * Pantalla de gestión de Casa (RF2).
 *
 * Ofrece dos opciones al usuario autenticado que aún no tiene Casa activa:
 *   - Crear una nueva Casa e invitar a otros mediante código.
 *   - Unirse a una Casa existente introduciendo su código de invitación.
 *
 * Si el usuario ya pertenece a alguna Casa, puede seleccionarla de la lista.
 */
@Composable
fun GestionCasaScreen(
    authViewModel: AuthViewModel,
    casaViewModel: CasaViewModel,
    onCasaSeleccionada: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val casaState by casaViewModel.uiState.collectAsState()

    val usuarioId = authState.usuarioActual?.id

    // Cuando tenemos el usuario autenticado, empezamos a observar sus Casas
    LaunchedEffect(usuarioId) {
        if (usuarioId != null) {
            casaViewModel.observarCasas(usuarioId)
        }
    }

    // Si la operación de crear/unirse ha sido exitosa, navegamos
    LaunchedEffect(casaState.operacionExitosa) {
        if (casaState.operacionExitosa) {
            casaViewModel.limpiarOperacionExitosa()
            onCasaSeleccionada()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Saludo al usuario
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¡Hola, ${authState.usuarioActual?.nombre ?: ""}!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = {
                authViewModel.cerrarSesion()
                onCerrarSesion()
            }) {
                Text("Cerrar sesión")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu Casa",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Elige cómo empezar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Tarjeta: Crear Casa nueva ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Crear una Casa nueva",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Genera un código para que los tuyos se unan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = casaState.nombreNuevaCasa,
                    onValueChange = { casaViewModel.onNombreNuevaCasaChange(it) },
                    label = { Text("Nombre de la Casa") },
                    singleLine = true,
                    enabled = !casaState.cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (usuarioId != null) casaViewModel.crearCasa(usuarioId)
                    },
                    enabled = !casaState.cargando && usuarioId != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (casaState.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Crear Casa")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Separador visual "o"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  o  ",
                style = MaterialTheme.typography.bodyMedium
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Tarjeta: Unirse a Casa ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Unirse a una Casa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Introduce el código de invitación que te han enviado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = casaState.codigoInvitacion,
                    onValueChange = { casaViewModel.onCodigoInvitacionChange(it) },
                    label = { Text("Código (ej: ABC123)") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    singleLine = true,
                    enabled = !casaState.cargando,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (usuarioId != null) casaViewModel.unirseACasa(usuarioId)
                    },
                    enabled = !casaState.cargando && usuarioId != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (casaState.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Unirme")
                    }
                }
            }
        }

        // Mensaje de error
        if (casaState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = casaState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Si ya tiene casas, mostramos una lista debajo
        if (casaState.casas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Mis Casas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            casaState.casas.forEach { casa ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = onCasaSeleccionada
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = casa.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Código: ${casa.codigoInvitacion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${casa.miembrosIds.size} miembro(s)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}