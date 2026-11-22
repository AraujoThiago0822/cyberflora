package com.cyberflora.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import jakarta.validation.constraints.Size

data class PlantRequest(
    @field:NotBlank(message = "O nome da planta é obrigatório")
    @field:Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    val name: String,

    @field:NotBlank(message = "A espécie é obrigatória")
    @field:Size(max = 150, message = "A espécie deve ter no máximo 150 caracteres")
    val species: String,

    val plantingDate: LocalDate?,

    @field:Positive(message = "A umidade mínima deve ser maior que zero")
    val minHumidity: Double,

    @field:Positive(message = "A umidade máxima deve ser maior que zero")
    val maxHumidity: Double,

    val active: Boolean
)

data class PlantResponse(
    val id: Long,
    val name: String,
    val species: String,
    val plantingDate: LocalDate?,
    val minHumidity: Double,
    val maxHumidity: Double,
    val active: Boolean
)
