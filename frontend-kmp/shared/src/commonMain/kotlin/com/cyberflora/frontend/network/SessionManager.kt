package com.cyberflora.frontend.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SessionManager {
    var token: String? by mutableStateOf(null)
        private set
    var userName: String? by mutableStateOf(null)
        private set

    fun save(token: String, userName: String) {
        this.token = token
        this.userName = userName
    }

    fun clear() {
        token = null
        userName = null
    }

    val isLoggedIn: Boolean get() = token != null
}