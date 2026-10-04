package com.cyberflora.frontend.network

import com.cyberflora.frontend.model.PlantRequest
import com.cyberflora.frontend.model.PlantResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class PlantRepository {

    private fun HttpRequestBuilder.withAuth() {
        header("Authorization", "Bearer ${SessionManager.token}")
    }

    suspend fun listAll(): Result<List<PlantResponse>> = try {
        val response: List<PlantResponse> = httpClient.get("$BASE_URL/plants") { withAuth() }.body()
        Result.success(response)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun findById(id: Long): Result<PlantResponse> = try {
        val response: PlantResponse = httpClient.get("$BASE_URL/plants/$id") { withAuth() }.body()
        Result.success(response)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun create(request: PlantRequest): Result<PlantResponse> = try {
        val response: PlantResponse = httpClient.post("$BASE_URL/plants") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        Result.success(response)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(id: Long, request: PlantRequest): Result<PlantResponse> = try {
        val response: PlantResponse = httpClient.put("$BASE_URL/plants/$id") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        Result.success(response)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: Long): Result<Unit> = try {
        httpClient.delete("$BASE_URL/plants/$id") { withAuth() }
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}