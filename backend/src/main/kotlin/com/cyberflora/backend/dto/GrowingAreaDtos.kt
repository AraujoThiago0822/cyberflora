package com.cyberflora.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class GrowingAreaRequest(
    @field:NotBlank(message = "O nome da área é obrigatório")
    val name: String,

    val description: String,

    @field:Positive(message = "A capacidade deve ser maior que zero")
    val capacityM2: Double,

    val irrigationAutomatic: Boolean
)

data class GrowingAreaResponse(
    val id: Long,
    val name: String,
    val description: String,
    val capacityM2: Double,
    val irrigationAutomatic: Boolean,
    val createdAt: LocalDateTime
)