package com.cyberflora.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberflora.frontend.model.SensorReadingResponse
import com.cyberflora.frontend.network.SensorReadingRepository
import com.cyberflora.frontend.network.userMessage
import kotlinx.coroutines.launch

class SensorHistoryViewModel(private val areaId: Long) : ViewModel() {
    private val repository = SensorReadingRepository()

    var hours by mutableStateOf(24)
    var type by mutableStateOf("soil_moisture")

    var history by mutableStateOf<List<SensorReadingResponse>>(emptyList())
        private set
    var latest by mutableStateOf<List<SensorReadingResponse>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun refresh() {
        val tipoPedido = type
        val horasPedidas = hours
        viewModelScope.launch {
            isLoading = true
            repository.history(areaId, horasPedidas, tipoPedido)
                .onSuccess {
                    // descarta a resposta se o usuário já trocou o filtro enquanto ela vinha
                    if (tipoPedido == type && horasPedidas == hours) {
                        history = it
                        errorMessage = null
                    }
                }
                .onFailure { errorMessage = userMessage(it) }
            repository.latest(areaId).onSuccess { latest = it }
            isLoading = false
        }
    }
}