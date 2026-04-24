package com.tecnosue.qhayhoy.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Pantalla de registro de nuevo usuario (RF1.1).
 *
 * Solicita nombre, email y contraseña. Al completar el registro
 * con éxito, navega automáticamente a la pantalla principal.
 */
@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onIrALogin: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navegar fuera cuando el registro haya creado la sesión
    LaunchedEffect(uiState.usuarioActual) {
        if (uiState.usuarioActual != null) {
            onRegistroExitoso()
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
            text = "Crear cuenta",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo nombre
        OutlinedTextField(
            value = uiState.nombre,
            onValueChange = { viewModel.onNombreChange(it) },
            label = { Text("Nombre") },
            singleLine = true,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo email
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo contraseña
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        )

        // Mensaje de error
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Registrarse
        Button(
            onClick = { viewModel.registrar() },
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Registrarme")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enlace a login
        TextButton(
            onClick = onIrALogin,
            enabled = !uiState.cargando
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}