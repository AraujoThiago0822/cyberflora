package com.cyberflora.frontend.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val email: String, val name: String)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)