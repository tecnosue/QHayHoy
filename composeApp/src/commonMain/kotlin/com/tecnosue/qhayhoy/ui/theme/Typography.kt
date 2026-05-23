package com.tecnosue.qhayhoy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografía personalizada de QHayHoy.
 *
 * Sobreescribe los estilos de Material 3 que se usan para títulos importantes,
 * aplicándoles directamente el color VerdeOscuro. Esto permite que cualquier
 * Text de la app que use estos estilos lleve el color enfático sin tener que
 * pasarlo manualmente.
 *
 * Estilos enfatizados (verde oscuro):
 *  - headlineLarge, headlineMedium, headlineSmall → saludos, títulos de pantalla.
 *  - titleLarge → nombres de receta, encabezados de sección destacados.
 *
 * El resto de estilos (titleMedium, body*, label*) mantienen los valores
 * por defecto de Material 3 y usan onSurface / onSurfaceVariant.
 */
val QHayHoyTypography = Typography().run {
    copy(
        headlineLarge  = headlineLarge.copy(color = VerdeOscuro, fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(color = VerdeOscuro, fontWeight = FontWeight.Bold),
        headlineSmall  = headlineSmall.copy(color = VerdeOscuro, fontWeight = FontWeight.Bold),
        titleLarge     = titleLarge.copy(color = VerdeOscuro, fontWeight = FontWeight.Bold)
    )
}
