package com.tecnosue.qhayhoy.ui.casa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Key
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.tecnosue.qhayhoy.domain.Casa
import com.tecnosue.qhayhoy.ui.auth.AuthViewModel

/**
 * Pantalla de gestión de Casa (RF2).
 *
 * Organización de la pantalla:
 *  1. Saludo al usuario + botón Cerrar sesión.
 *  2. Sección "Tus Casas": tarjetas con las casas a las que ya pertenece.
 *     Si pulsa una, se selecciona como activa y navega a Principal.
 *  3. Sección "Añadir otra Casa" (o "Empieza creando o uniéndote" si no
 *     pertenece a ninguna todavía): dos tarjetas con las opciones de
 *     crear una Casa nueva o unirse a una existente con código.
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

    LaunchedEffect(usuarioId) {
        if (usuarioId != null) {
            casaViewModel.observarCasas(usuarioId)
        }
    }

    LaunchedEffect(casaState.operacionExitosa) {
        if (casaState.operacionExitosa) {
            casaViewModel.limpiarOperacionExitosa()
            onCasaSeleccionada()
        }
    }

    val tieneCasas = casaState.casas.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // --- Cabecera: saludo + cerrar sesión ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¡Hola, ${authState.usuarioActual?.nombre ?: ""}!",
                style = MaterialTheme.typography.headlineSmall
            )
            IconButton(onClick = {
                authViewModel.cerrarSesion()
                onCerrarSesion()
            }) {
                Icon(
                    imageVector = Lucide.LogOut,
                    contentDescription = "Cerrar sesión",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Sección: Tus Casas (si tiene alguna) ---
        if (tieneCasas) {
            Text(
                text = "Tus Casas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecciona una para entrar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            casaState.casas.forEach { casa ->
                TarjetaCasaExistente(
                    casa = casa,
                    onSeleccionar = {
                        if (usuarioId != null) {
                            casaViewModel.seleccionarCasa(usuarioId, casa.id)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Sección: añadir otra Casa o empezar ---
        Text(
            text = if (tieneCasas) "Añadir otra Casa" else "Empieza tu Casa",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (tieneCasas) {
                "Puedes crear una nueva o unirte a otra con un código"
            } else {
                "Elige cómo empezar"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Tarjeta: Crear Casa nueva ---
        TarjetaCrearCasa(
            nombre = casaState.nombreNuevaCasa,
            cargando = casaState.cargando,
            onNombreChange = { casaViewModel.onNombreNuevaCasaChange(it) },
            onCrear = {
                if (usuarioId != null) casaViewModel.crearCasa(usuarioId)
            },
            habilitado = usuarioId != null
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Separador visual "o"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "  o  ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Tarjeta: Unirse a Casa ---
        TarjetaUnirseCasa(
            codigo = casaState.codigoInvitacion,
            cargando = casaState.cargando,
            onCodigoChange = { casaViewModel.onCodigoInvitacionChange(it) },
            onUnirse = {
                if (usuarioId != null) casaViewModel.unirseACasa(usuarioId)
            },
            habilitado = usuarioId != null
        )

        // --- Mensaje de error ---
        if (casaState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = casaState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Tarjeta de una Casa a la que ya pertenece el usuario.
 * Estilo: blanca con icono verde de casa, bordes redondeados.
 */
@Composable
private fun TarjetaCasaExistente(
    casa: Casa,
    onSeleccionar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onSeleccionar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.House,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = casa.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Código: ${casa.codigoInvitacion}  ·  ${casa.miembrosIds.size} miembro(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Tarjeta para crear una Casa nueva. Estilo: blanca con icono y borde verde.
 */
@Composable
private fun TarjetaCrearCasa(
    nombre: String,
    cargando: Boolean,
    onNombreChange: (String) -> Unit,
    onCrear: () -> Unit,
    habilitado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.House,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
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
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre de la Casa") },
                singleLine = true,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCrear,
                enabled = !cargando && habilitado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Casa")
                }
            }
        }
    }
}

/**
 * Tarjeta para unirse a una Casa existente. Estilo: blanca con icono y borde naranja.
 */
@Composable
private fun TarjetaUnirseCasa(
    codigo: String,
    cargando: Boolean,
    onCodigoChange: (String) -> Unit,
    onUnirse: () -> Unit,
    habilitado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
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
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = codigo,
                onValueChange = onCodigoChange,
                label = { Text("Código (ej: ABC123)") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                singleLine = true,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onUnirse,
                enabled = !cargando && habilitado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                } else {
                    Text("Unirme")
                }
            }
        }
    }
}