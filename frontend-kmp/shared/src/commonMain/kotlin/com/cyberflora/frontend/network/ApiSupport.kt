package com.cyberflora.frontend.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ApiException(val statusCode: Int, message: String) : Exception(message)

suspend fun HttpResponse.ensureSuccess(): HttpResponse {
    if (status.isSuccess()) return this
    val code = status.value
    val detail = extractErrorMessage(bodyAsText())
    val message = when {
        code == 401 || code == 403 -> "Sessão expirada ou sem permissão. Clique em Sair e entre novamente."
        code in 400..499 && detail != null -> detail
        else -> "Erro $code no servidor" + (detail?.let { ": $it" } ?: "")
    }
    throw ApiException(code, message)
}

private fun extractErrorMessage(body: String): String? = try {
    Json.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.content
} catch (e: Exception) {
    null
}

fun userMessage(e: Throwable): String =
    if (e is ApiException) (e.message ?: "Erro desconhecido") else "Falha de conexão com o servidor"