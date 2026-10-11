package com.cyberflora.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberflora.frontend.model.GrowingAreaRequest
import com.cyberflora.frontend.model.GrowingAreaResponse
import com.cyberflora.frontend.network.GrowingAreaRepository
import kotlinx.coroutines.launch

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

    fun loadAll() {
        viewModelScope.launch {
            isLoading = true
            repository.listAll()
                .onSuccess { areas = it }
                .onFailure { errorMessage = "Erro ao carregar áreas de cultivo" }
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
            }
        }
    }

    fun clearForm() {
        editingId = null
        name = ""
        description = ""
        capacityM2 = ""
        irrigationAutomatic = true
        errorMessage = null
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val request = GrowingAreaRequest(
                name = name,
                description = description,
                capacityM2 = capacityM2.toDoubleOrNull() ?: 0.0,
                irrigationAutomatic = irrigationAutomatic
            )
            val result = if (editingId == null) repository.create(request) else repository.update(editingId!!, request)
            result
                .onSuccess {
                    clearForm()
                    loadAll()
                    onSaved()
                }
                .onFailure { errorMessage = com.cyberflora.frontend.network.userMessage(it) }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id).onSuccess { loadAll() }
        }
    }
}