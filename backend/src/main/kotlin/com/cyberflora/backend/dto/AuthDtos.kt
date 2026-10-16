package com.cyberflora.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class RegisterRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    val name: String,

    @field:NotBlank(message = "O e-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    val email: String,

    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,72}$",
        message = "A senha deve ter de 8 a 72 caracteres, com 1 maiúscula, 1 número e 1 caractere especial"
    )
    val password: String,

    @field:NotBlank(message = "A confirmação de senha é obrigatória")
    val confirmPassword: String
)

data class LoginRequest(
    @field:NotBlank(message = "O e-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    val email: String,

    @field:NotBlank(message = "A senha é obrigatória")
    val password: String
)

data class AuthResponse(
    val token: String,
    val email: String,
    val name: String
)