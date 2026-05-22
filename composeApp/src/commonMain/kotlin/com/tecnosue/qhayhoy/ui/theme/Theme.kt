package com.tecnosue.qhayhoy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema visual de QHayHoy.
 *
 * Importante: definimos TODOS los roles de surface (surface, surfaceVariant,
 * surfaceContainer, surfaceContainerLow/High/Highest) para evitar que Material 3
 * genere automáticamente los tonos lavanda por defecto en Cards y Sheets.
 */

private val LightColors = lightColorScheme(
    // ─── PRIMARIO: verde oliva ───
    primary             = OlivaFuerte,
    onPrimary           = TextoSobreOscuro,
    primaryContainer    = OlivaBadgeClaro,
    onPrimaryContainer  = OlivaFuerte,

    // ─── SECUNDARIO: naranja ───
    secondary           = NaranjaFuerte,
    onSecondary         = TextoSobreOscuro,
    secondaryContainer  = NaranjaBadgeClaro,
    onSecondaryContainer= TextoPrincipal,

    // ─── TERCIARIO: variante naranja apagada (para acentos suaves) ───
    tertiary            = NaranjaApagado,
    onTertiary          = TextoSobreOscuro,
    tertiaryContainer   = NaranjaBadgeClaro,
    onTertiaryContainer = TextoPrincipal,

    // ─── FONDOS Y SUPERFICIES (lo crítico para quitar el morado de las Cards) ───
    background                  = CremaFondo,
    onBackground                = TextoPrincipal,

    surface                     = Blanco,           // Cards principales
    onSurface                   = TextoPrincipal,

    surfaceVariant              = CremaInput,       // Inputs, chips
    onSurfaceVariant            = TextoSecundario,

    // Los containers son los que Material 3 inventa con tinte primario si no los defines
    surfaceContainerLowest      = Blanco,
    surfaceContainerLow         = CremaFondo,
    surfaceContainer            = CremaFondoAlt,
    surfaceContainerHigh        = CremaInput,
    surfaceContainerHighest     = CremaInput,

    // ─── BORDES ───
    outline                     = BordeSuave,
    outlineVariant              = BordeSuave.copy(alpha = 0.5f),

    // ─── ERROR (mantenemos el rojo estándar de Material) ───
    error                       = Color(0xFFB3261E),
    onError                     = TextoSobreOscuro,
    errorContainer              = Color(0xFFF9DEDC),
    onErrorContainer            = Color(0xFF410E0B),
)

// Si decides soportar modo oscuro más adelante, deja al menos esta variante mínima
// para evitar crashes. De momento la mantenemos cercana al light.
private val DarkColors = darkColorScheme(
    primary             = OlivaMedio,
    onPrimary           = TextoSobreOscuro,
    secondary           = NaranjaFuerte,
    onSecondary         = TextoSobreOscuro,
    background          = Color(0xFF1F1F1B),
    onBackground        = Color(0xFFE8E5D8),
    surface             = Color(0xFF272722),
    onSurface           = Color(0xFFE8E5D8),
    surfaceVariant      = Color(0xFF333330),
    onSurfaceVariant    = Color(0xFFC8C5BA),
)

@Composable
fun QHayHoyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography  = MaterialTheme.typography,  // sustituye por tu Typography si la tienes
        content     = content
    )
}