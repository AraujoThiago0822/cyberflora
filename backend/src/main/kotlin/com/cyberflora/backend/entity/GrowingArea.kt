package com.cyberflora.backend.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

@Entity
@Table(name = "growing_areas")
data class GrowingArea(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "O nome da área é obrigatório")
    @Column(nullable = false)
    val name: String = "",

    @Column(length = 500)
    val description: String = "",

    @field:Positive(message = "A capacidade deve ser maior que zero")
    @Column(name = "capacity_m2")
    val capacityM2: Double = 0.0,

    @Column(name = "irrigation_automatic")
    val irrigationAutomatic: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)