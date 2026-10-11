package com.cyberflora.frontend.network

import com.cyberflora.frontend.model.GrowingAreaRequest
import com.cyberflora.frontend.model.GrowingAreaResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class GrowingAreaRepository {

    private fun HttpRequestBuilder.withAuth() {
        header("Authorization", "Bearer ${SessionManager.token}")
    }

    suspend fun listAll(): Result<List<GrowingAreaResponse>> = try {
        val response: List<GrowingAreaResponse> = httpClient.get("$BASE_URL/growing-areas") { withAuth() }.ensureSuccess().body()
        Result.success(response)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun findById(id: Long): Result<GrowingAreaResponse> = try {
        val response: GrowingAreaResponse = httpClient.get("$BASE_URL/growing-areas/$id") { withAuth() }.ensureSuccess().body()
        Result.success(response)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun create(request: GrowingAreaRequest): Result<GrowingAreaResponse> = try {
        val response: GrowingAreaResponse = httpClient.post("$BASE_URL/growing-areas") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.ensureSuccess().body()
        Result.success(response)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun update(id: Long, request: GrowingAreaRequest): Result<GrowingAreaResponse> = try {
        val response: GrowingAreaResponse = httpClient.put("$BASE_URL/growing-areas/$id") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.ensureSuccess().body()
        Result.success(response)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun delete(id: Long): Result<Unit> = try {
        httpClient.delete("$BASE_URL/growing-areas/$id") { withAuth() }.ensureSuccess()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}