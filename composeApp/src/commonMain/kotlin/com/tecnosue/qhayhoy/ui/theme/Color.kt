package com.tecnosue.qhayhoy.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════
// Paleta QHayHoy — extraída del mockup (mayo 2026)
// ═══════════════════════════════════════════════════════════════

// ─── Verdes (acción primaria) ───
val VerdeOscuro       = Color(0xFF4E670E)  // Botón "Iniciar sesión", texto "QHayHoy"
val VerdeOliva        = Color(0xFF7A9E3F)  // Acentos secundarios verdes
val VerdeClaro        = Color(0xFFEDF0E6)  // Fondos tenues / badge claro

// ─── Naranjas (acción secundaria) ───
val NaranjaFuerte     = Color(0xFFE8833A)  // Botón "Generar menú", "Unirme"
val NaranjaApagado    = Color(0xFFD99D6B)  // Acento terciario (variante suave)
val NaranjaBadgeClaro = Color(0xFFEAB899)  // Badge tenue del icono llave

// ─── Fondos crema ───
val CremaFondo        = Color(0xFFFAFAEE)  // Background general
val CremaFondoAlt     = Color(0xFFF8F9EE)  // Variante crema más cálida
val CremaInput        = Color(0xFFEEEEE2)  // Inputs y cards alternativas
val Blanco            = Color(0xFFFFFFFF)  // Surface de cards principales

// ─── Texto ───
val TextoPrincipal    = Color(0xFF1F1F1F)  // Negro suave para títulos/cuerpo
val TextoSecundario   = Color(0xFF6B6B6B)  // Gris medio para subtítulos
val TextoSobreOscuro  = Color(0xFFFFFFFF)  // Texto blanco sobre primarios

// ─── Bordes ───
val BordeSuave        = Color(0xFFE5E2D7)  // Bordes de inputs / outlines

// ─── Pasteles de categorías de recetas ───
val PastelVerdeMenta  = Color(0xFFE8F2E7)
val PastelMelocoton   = Color(0xFFFDEADC)
val PastelAmarillo    = Color(0xFFFFF4CD)
val PastelLila        = Color(0xFFF4E7F8)
val PastelBeis        = Color(0xFFE9E4E0)
val PastelMentaClara  = Color(0xFFEBF5EC)

// Lista útil para asignar color por receta de forma rotativa
val PastelesCategorias = listOf(
    PastelVerdeMenta,
    PastelMelocoton,
    PastelAmarillo,
    PastelLila,
    PastelBeis,
    PastelMentaClara
)