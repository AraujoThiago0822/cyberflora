package com.cyberflora.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberflora.frontend.model.GrowingAreaRequest
import com.cyberflora.frontend.model.GrowingAreaResponse
import com.cyberflora.frontend.network.GrowingAreaRepository
import com.cyberflora.frontend.network.userMessage
import kotlinx.coroutines.launch

private val DEVICE_ID_REGEX = Regex("^[A-Za-z0-9_-]{0,64}$")

class GrowingAreaViewModel : ViewModel() {
    private val repository = GrowingAreaRepository()

    var areas by mutableStateOf<List<GrowingAreaResponse>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var editingId by mutableStateOf<Long?>(null)
        private set
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var capacityM2 by mutableStateOf("")
    var irrigationAutomatic by mutableStateOf(true)
    var deviceId by mutableStateOf("")

    fun loadAll() {
        viewModelScope.launch {
            isLoading = true
            repository.listAll()
                .onSuccess { areas = it }
                .onFailure { errorMessage = userMessage(it) }
            isLoading = false
        }
    }

    fun loadForEdit(id: Long) {
        viewModelScope.launch {
            repository.findById(id).onSuccess { a ->
                editingId = a.id
                name = a.name
                description = a.description
                capacityM2 = a.capacityM2.toString()
                irrigationAutomatic = a.irrigationAutomatic
                deviceId = a.deviceId ?: ""
                errorMessage = null
            }
        }
    }

    fun clearForm() {
        editingId = null
        name = ""
        description = ""
        capacityM2 = ""
        irrigationAutomatic = true
        deviceId = ""
        errorMessage = null
    }

    fun save(onSaved: () -> Unit) {
        val capacidade = capacityM2.replace(',', '.').toDoubleOrNull()
        if (name.isBlank()) {
            errorMessage = "Informe o nome da área"
            return
        }
        if (capacidade == null || capacidade <= 0.0) {
            errorMessage = "A capacidade deve ser maior que zero"
            return
        }
        if (!DEVICE_ID_REGEX.matches(deviceId.trim())) {
            errorMessage = "ID do dispositivo: até 64 caracteres (letras, números, - e _)"
            return
        }
        errorMessage = null

        viewModelScope.launch {
            val request = GrowingAreaRequest(
                name = name,
                description = description,
                capacityM2 = capacidade,
                irrigationAutomatic = irrigationAutomatic,
                deviceId = deviceId.trim()
            )
            val result = if (editingId == null) repository.create(request) else repository.update(editingId!!, request)
            result
                .onSuccess {
                    clearForm()
                    loadAll()
                    onSaved()
                }
                .onFailure { errorMessage = userMessage(it) }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id).onSuccess { loadAll() }
        }
    }
}