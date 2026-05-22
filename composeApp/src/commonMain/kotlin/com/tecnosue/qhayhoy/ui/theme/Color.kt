package com.tecnosue.qhayhoy.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════
// Paleta QHayHoy — extraída del mockup (mayo 2026)
// ═══════════════════════════════════════════════════════════════

// ─── Verdes oliva (acción primaria) ───
val OlivaFuerte       = Color(0xFF4E660D)  // Botón "Iniciar sesión", texto "QHayHoy"
val OlivaMedio        = Color(0xFF7F9A50)  // Botón "Crear Casa", avatar
val OlivaTitulares    = Color(0xFF466225)  // "Lunes", "Martes"... en Menú semanal
val OlivaBadgeClaro   = Color(0xFFEDF0E9)  // Fondo del icono cubiertos / casita

// ─── Naranjas (acción secundaria) ───
val NaranjaFuerte     = Color(0xFFFC9B54)  // Botón "Generar menú"
val NaranjaFAB        = Color(0xFFE6853F)  // FAB "+" en Recetas
val NaranjaApagado    = Color(0xFFD99D6B)  // Botón "Unirme"
val NaranjaBadgeClaro = Color(0xFFEAB899)  // Badge del icono llave

// ─── Fondos crema ───
val CremaFondo        = Color(0xFFFAF7F0)  // Background general
val CremaFondoAlt     = Color(0xFFF8F9EE)  // Hueco entre cards (matiz oliva)
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