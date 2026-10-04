package com.cyberflora.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberflora.frontend.network.AuthRepository
import com.cyberflora.frontend.network.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.login(email, password)
                .onSuccess {
                    SessionManager.save(it.token, it.name)
                    onSuccess()
                }
                .onFailure {
                    errorMessage = "E-mail ou senha inválidos"
                }
            isLoading = false
        }
    }
}