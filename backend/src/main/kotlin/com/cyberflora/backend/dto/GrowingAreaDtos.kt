package com.cyberflora.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class GrowingAreaRequest(
    @field:NotBlank(message = "O nome da área é obrigatório")
    @field:Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    val name: String,

    @field:Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    val description: String,

    @field:Positive(message = "A capacidade deve ser maior que zero")
    val capacityM2: Double,

    val irrigationAutomatic: Boolean,

    @field:Pattern(
        regexp = "^[A-Za-z0-9_-]{0,64}$",
        message = "O ID do dispositivo deve ter até 64 caracteres (letras, números, - e _)"
    )
    val deviceId: String? = null
)

data class GrowingAreaResponse(
    val id: Long,
    val name: String,
    val description: String,
    val capacityM2: Double,
    val irrigationAutomatic: Boolean,
    val createdAt: LocalDateTime,
    val deviceId: String? = null
)