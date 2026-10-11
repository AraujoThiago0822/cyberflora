package com.cyberflora.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberflora.frontend.model.PlantRequest
import com.cyberflora.frontend.model.PlantResponse
import com.cyberflora.frontend.network.PlantRepository
import com.cyberflora.frontend.network.userMessage
import kotlinx.coroutines.launch

/** "05042025" (DDMMAAAA) -> "2025-04-05". Retorna null se a data for inválida. */
private fun digitsToIso(digits: String): String? {
    if (digits.length != 8) return null
    val day = digits.substring(0, 2).toInt()
    val month = digits.substring(2, 4).toInt()
    val year = digits.substring(4, 8).toInt()
    if (year < 1900 || month !in 1..12 || day < 1) return null
    val maxDay = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    if (day > maxDay) return null
    return "$year-${digits.substring(2, 4)}-${digits.substring(0, 2)}"
}

/** "2025-04-05" -> "05042025" */
private fun isoToDigits(iso: String): String {
    val parts = iso.split("-")
    return if (parts.size == 3) parts[2] + parts[1] + parts[0] else ""
}

class PlantViewModel : ViewModel() {
    private val repository = PlantRepository()

    var plants by mutableStateOf<List<PlantResponse>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var editingId by mutableStateOf<Long?>(null)
        private set
    var name by mutableStateOf("")
    var species by mutableStateOf("")
    var plantingDate by mutableStateOf("")   // só dígitos DDMMAAAA
    var minHumidity by mutableStateOf("")    // só dígitos
    var maxHumidity by mutableStateOf("")    // só dígitos
    var active by mutableStateOf(true)

    fun loadAll() {
        viewModelScope.launch {
            isLoading = true
            repository.listAll()
                .onSuccess { plants = it }
                .onFailure { errorMessage = userMessage(it) }
            isLoading = false
        }
    }

    fun loadForEdit(id: Long) {
        viewModelScope.launch {
            repository.findById(id).onSuccess { p ->
                editingId = p.id
                name = p.name
                species = p.species
                plantingDate = p.plantingDate?.let { isoToDigits(it) } ?: ""
                minHumidity = p.minHumidity.toInt().toString()
                maxHumidity = p.maxHumidity.toInt().toString()
                active = p.active
                errorMessage = null
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
        val min = minHumidity.toIntOrNull()
        val max = maxHumidity.toIntOrNull()
        if (name.isBlank() || species.isBlank()) {
            errorMessage = "Preencha o nome e a espécie"
            return
        }
        if (min == null || max == null || min !in 1..100 || max !in 1..100) {
            errorMessage = "A umidade deve estar entre 1% e 100%"
            return
        }
        if (min >= max) {
            errorMessage = "A umidade mínima deve ser menor que a máxima"
            return
        }
        val iso = if (plantingDate.isBlank()) null else digitsToIso(plantingDate)
        if (plantingDate.isNotBlank() && iso == null) {
            errorMessage = "Data inválida (use DD/MM/AAAA)"
            return
        }
        errorMessage = null

        viewModelScope.launch {
            val request = PlantRequest(
                name = name,
                species = species,
                plantingDate = iso,
                minHumidity = min.toDouble(),
                maxHumidity = max.toDouble(),
                active = active
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