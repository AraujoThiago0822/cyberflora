package com.cyberflora.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberflora.frontend.network.AuthRepository
import com.cyberflora.frontend.network.SessionManager
import kotlinx.coroutines.launch

private val STRONG_PASSWORD_REGEX =
    Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>_\\-+=]).{8,}$")

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository()

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun register(onSuccess: () -> Unit) {
        if (!STRONG_PASSWORD_REGEX.matches(password)) {
            errorMessage = "Senha fraca: use 8+ caracteres, 1 maiúscula, 1 número e 1 caractere especial"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "As senhas não coincidem"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.register(name, email, password, confirmPassword)
                .onSuccess {
                    SessionManager.save(it.token, it.name)
                    onSuccess()
                }
                .onFailure {
                    errorMessage = "Não foi possível registrar (e-mail já em uso?)"
                }
            isLoading = false
        }
    }
}