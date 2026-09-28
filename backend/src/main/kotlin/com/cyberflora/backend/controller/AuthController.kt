package com.cyberflora.backend.controller

import com.cyberflora.backend.dto.AuthResponse
import com.cyberflora.backend.dto.LoginRequest
import com.cyberflora.backend.dto.RegisterRequest
import com.cyberflora.backend.entity.User
import com.cyberflora.backend.repository.UserRepository
import com.cyberflora.backend.security.JwtUtil
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<Any> {
        if (request.password != request.confirmPassword) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "As senhas não coincidem"))
        }

        if (userRepository.findByEmail(request.email).isPresent) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to "Este e-mail já está cadastrado"))
        }

        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password)!!
        )
        userRepository.save(user)

        val token = jwtUtil.generateToken(user.email)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse(token = token, email = user.email, name = user.name))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password)
            )

            val user = userRepository.findByEmail(request.email)
                .orElseThrow { IllegalStateException("Usuário não encontrado") }

            val token = jwtUtil.generateToken(user.email)

            ResponseEntity.ok(AuthResponse(token = token, email = user.email, name = user.name))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "E-mail ou senha inválidos"))
        }
    }
}