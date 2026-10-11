package com.cyberflora.backend.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDate

@Entity
@Table(name = "plants")
data class Plant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:NotBlank(message = "O nome da planta é obrigatório")
    @Column(nullable = false, length = 100)
    val name: String = "",

    @field:NotBlank(message = "A espécie é obrigatória")
    @Column(nullable = false, length = 150)
    val species: String = "",

    @Column(name = "planting_date")
    val plantingDate: LocalDate? = null,

    @field:Positive(message = "A umidade mínima deve ser maior que zero")
    @Column(name = "min_humidity")
    val minHumidity: Double = 0.0,

    @field:Positive(message = "A umidade máxima deve ser maior que zero")
    @Column(name = "max_humidity")
    val maxHumidity: Double = 0.0,

    @Column(nullable = false)
    val active: Boolean = true,

    @ManyToOne
    @JoinColumn(name = "owner_id")
    val owner: User? = null
)