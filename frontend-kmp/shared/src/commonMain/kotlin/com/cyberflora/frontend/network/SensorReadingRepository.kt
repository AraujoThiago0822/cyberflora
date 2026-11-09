package com.cyberflora.frontend.network

import com.cyberflora.frontend.model.SensorReadingResponse
import io.ktor.client.call.*
import io.ktor.client.request.*

class SensorReadingRepository {

    private fun HttpRequestBuilder.withAuth() {
        header("Authorization", "Bearer ${SessionManager.token}")
    }

    suspend fun history(areaId: Long, hours: Int, type: String): Result<List<SensorReadingResponse>> = try {
        val resposta: List<SensorReadingResponse> = httpClient.get("$BASE_URL/growing-areas/$areaId/readings") {
            withAuth()
            parameter("hours", hours)
            parameter("type", type)
        }.ensureSuccess().body()
        Result.success(resposta)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun latest(areaId: Long): Result<List<SensorReadingResponse>> = try {
        val resposta: List<SensorReadingResponse> = httpClient.get("$BASE_URL/growing-areas/$areaId/readings/latest") { withAuth() }
            .ensureSuccess().body()
        Result.success(resposta)
    } catch (e: Exception) { Result.failure(e) }
}