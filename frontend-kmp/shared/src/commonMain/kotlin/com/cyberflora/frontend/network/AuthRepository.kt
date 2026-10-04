package com.cyberflora.frontend.network

import com.cyberflora.frontend.model.AuthResponse
import com.cyberflora.frontend.model.LoginRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response: AuthResponse = httpClient.post("$BASE_URL/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}