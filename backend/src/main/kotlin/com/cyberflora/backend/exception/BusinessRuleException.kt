package com.cyberflora.backend.exception

/** Violação de uma regra de negócio do CyberFlora (tratada pelo GlobalExceptionHandler como HTTP 400). */
class BusinessRuleException(message: String) : RuntimeException(message)