package com.tecnosue.qhayhoy.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/**
 * Devuelve el ID de la semana actual (formato YYYY-MM-DD del lunes).
 * Se usa como identificador del documento del menú/lista semanal.
 */
fun obtenerIdSemanaActual(): String {
    val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val diasARestar = hoy.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber
    val lunes = hoy.minus(DatePeriod(days = diasARestar))
    return lunes.toString()
}
