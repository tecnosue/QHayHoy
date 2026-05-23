package com.tecnosue.qhayhoy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema visual de QHayHoy.
 *
 * Importante: definimos TODOS los roles de surface (surface, surfaceVariant,
 * surfaceContainer, surfaceContainerLow/High/Highest) para evitar que Material 3
 * genere automáticamente los tonos lavanda por defecto en Cards y Sheets.
 *
 * No se soporta modo oscuro: la aplicación se ha diseñado únicamente para
 * el modo claro al considerarse coherente con la estética cálida y doméstica
 * del producto.
 */

private val LightColors = lightColorScheme(
    // ─── PRIMARIO: verde oscuro ───
    primary             = VerdeOliva,
    onPrimary           = TextoSobreOscuro,
    primaryContainer    = VerdeClaro,
    onPrimaryContainer  = VerdeOscuro,

    // ─── SECUNDARIO: naranja fuerte ───
    secondary           = NaranjaFuerte,
    onSecondary         = TextoSobreOscuro,
    secondaryContainer  = NaranjaBadgeClaro,
    onSecondaryContainer = TextoPrincipal,

    // ─── TERCIARIO: variante naranja apagada (para acentos suaves) ───
    tertiary            = NaranjaApagado,
    onTertiary          = TextoSobreOscuro,
    tertiaryContainer   = PastelMelocoton,      // distinto del secondaryContainer
    onTertiaryContainer = TextoPrincipal,

    // ─── FONDOS Y SUPERFICIES ───
    background                  = CremaFondo,
    onBackground                = TextoPrincipal,

    surface                     = Blanco,           // Cards principales
    onSurface                   = TextoPrincipal,

    surfaceVariant              = CremaInput,       // Inputs, chips
    onSurfaceVariant            = TextoSecundario,

    // Jerarquía de containers de menor a mayor "elevación" visual
    surfaceContainerLowest      = Blanco,
    surfaceContainerLow         = CremaFondo,
    surfaceContainer            = CremaFondoAlt,
    surfaceContainerHigh        = CremaInput,
    surfaceContainerHighest     = BordeSuave,

    // ─── BORDES ───
    outline                     = BordeSuave,
    outlineVariant              = BordeSuave.copy(alpha = 0.5f),

    // ─── ERROR (rojo estándar de Material) ───
    error                       = Color(0xFFB3261E),
    onError                     = TextoSobreOscuro,
    errorContainer              = Color(0xFFF9DEDC),
    onErrorContainer            = Color(0xFF410E0B),
)

/**
 * Tema principal de la aplicación. Solo soporta modo claro.
 */
@Composable
fun QHayHoyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = QHayHoyTypography,
        content     = content
    )
}