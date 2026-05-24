package com.tecnosue.qhayhoy.data.translator

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP para el servicio gratuito de traducción MyMemory.
 *
 * Endpoint: https://api.mymemory.translated.net/get
 *
 * Limitaciones del plan gratuito:
 *  - 5000 palabras/día sin email registrado.
 *  - 50000 palabras/día con email asociado a la petición (parámetro 'de').
 *  - Máximo 500 caracteres por petición (textos largos se parten).
 *
 * No requiere API key ni registro: solo se asocia un email a las
 * peticiones para acceder al límite ampliado.
 */
class MyMemoryClient(
    private val emailContacto: String? = null
) {

    private val baseUrl = "https://api.mymemory.translated.net"

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Traduce un texto de un idioma a otro.
     *
     * @param texto Texto a traducir (máximo 500 caracteres por petición).
     * @param idiomaOrigen Código ISO 639-1 (ej: "en").
     * @param idiomaDestino Código ISO 639-1 (ej: "es").
     * @return Texto traducido, o el original si la API falla.
     */
    suspend fun traducir(
        texto: String,
        idiomaOrigen: String = "en",
        idiomaDestino: String = "es"
    ): String {
        if (texto.isBlank()) return texto

        return try {
            val response = httpClient.get("$baseUrl/get") {
                parameter("q", texto.take(500))
                parameter("langpair", "$idiomaOrigen|$idiomaDestino")
                emailContacto?.let { parameter("de", it) }
            }.body<MyMemoryResponse>()

            response.responseData?.translatedText ?: texto
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla, devolvemos el original sin romper la importación
            texto
        }
    }
}

@Serializable
private data class MyMemoryResponse(
    val responseData: ResponseData? = null,
    val responseStatus: Int = 0
)

@Serializable
private data class ResponseData(
    val translatedText: String? = null,
    val match: Double = 0.0
)