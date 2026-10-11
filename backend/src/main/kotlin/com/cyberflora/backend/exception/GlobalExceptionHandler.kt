package com.cyberflora.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun notFound(e: NoSuchElementException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (e.message ?: "Não encontrado")))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun invalid(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val message = e.bindingResult.fieldErrors.joinToString("; ") { it.defaultMessage ?: "Valor inválido" }
        return ResponseEntity.badRequest().body(mapOf("error" to message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun unreadable(e: HttpMessageNotReadableException): ResponseEntity<Map<String, String>> {
        val cause = e.mostSpecificCause.message?.lineSequence()?.firstOrNull()?.take(200) ?: "sem detalhe"
        println("Corpo da requisição inválido: $cause")
        return ResponseEntity.badRequest().body(mapOf("error" to "Corpo da requisição inválido: $cause"))
    }
}