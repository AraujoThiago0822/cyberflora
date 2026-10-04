package com.cyberflora.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberflora.frontend.model.PlantRequest
import com.cyberflora.frontend.model.PlantResponse
import com.cyberflora.frontend.network.PlantRepository
import kotlinx.coroutines.launch

class PlantViewModel : ViewModel() {
    private val repository = PlantRepository()

    // estado da lista
    var plants by mutableStateOf<List<PlantResponse>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // estado dos campos do formulario (um por campo, como pede o PDF)
    var editingId by mutableStateOf<Long?>(null)
        private set
    var name by mutableStateOf("")
    var species by mutableStateOf("")
    var plantingDate by mutableStateOf("")
    var minHumidity by mutableStateOf("")
    var maxHumidity by mutableStateOf("")
    var active by mutableStateOf(true)

    fun loadAll() {
        viewModelScope.launch {
            isLoading = true
            repository.listAll()
                .onSuccess { plants = it }
                .onFailure { errorMessage = "Erro ao carregar plantas" }
            isLoading = false
        }
    }

    fun loadForEdit(id: Long) {
        viewModelScope.launch {
            repository.findById(id).onSuccess { p ->
                editingId = p.id
                name = p.name
                species = p.species
                plantingDate = p.plantingDate ?: ""
                minHumidity = p.minHumidity.toString()
                maxHumidity = p.maxHumidity.toString()
                active = p.active
            }
        }
    }

    fun clearForm() {
        editingId = null
        name = ""
        species = ""
        plantingDate = ""
        minHumidity = ""
        maxHumidity = ""
        active = true
        errorMessage = null
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val request = PlantRequest(
                name = name,
                species = species,
                plantingDate = plantingDate.ifBlank { null },
                minHumidity = minHumidity.toDoubleOrNull() ?: 0.0,
                maxHumidity = maxHumidity.toDoubleOrNull() ?: 0.0,
                active = active
            )
            val result = if (editingId == null) repository.create(request) else repository.update(editingId!!, request)
            result
                .onSuccess {
                    clearForm()
                    loadAll()
                    onSaved()
                }
                .onFailure { errorMessage = "Erro ao salvar a planta" }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id).onSuccess { loadAll() }
        }
    }
}