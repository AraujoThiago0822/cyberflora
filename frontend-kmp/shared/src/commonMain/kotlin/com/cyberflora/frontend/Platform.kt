package com.cyberflora.frontend

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform