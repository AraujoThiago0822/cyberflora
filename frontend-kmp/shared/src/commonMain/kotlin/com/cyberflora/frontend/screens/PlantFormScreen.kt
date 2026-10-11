package com.cyberflora.frontend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberflora.frontend.theme.ErrorColor
import com.cyberflora.frontend.theme.GraySecondaryText
import com.cyberflora.frontend.theme.GreenPrimary
import com.cyberflora.frontend.theme.TextPrimary
import com.cyberflora.frontend.viewmodel.PlantViewModel

@Composable
fun PlantFormScreen(viewModel: PlantViewModel, onSaved: () -> Unit) {
    val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(
            if (viewModel.editingId == null) "Nova Planta" else "Editar Planta",
            color = TextPrimary, fontSize = 22.sp
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Nome") },
            singleLine = true,
            colors = cyberFloraFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = viewModel.species,
            onValueChange = { viewModel.species = it },
            label = { Text("Espécie") },
            singleLine = true,
            colors = cyberFloraFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = viewModel.plantingDate,
            onValueChange = { viewModel.plantingDate = it.filter { c -> c.isDigit() }.take(8) },
            label = { Text("Data de plantio") },
            placeholder = { Text("DD/MM/AAAA", color = GraySecondaryText) },
            visualTransformation = DateVisualTransformation(),
            keyboardOptions = numberKeyboard,
            singleLine = true,
            colors = cyberFloraFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = viewModel.minHumidity,
                onValueChange = { viewModel.minHumidity = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("Umid. mín") },
                suffix = { Text("%", color = GraySecondaryText) },
                keyboardOptions = numberKeyboard,
                singleLine = true,
                colors = cyberFloraFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = viewModel.maxHumidity,
                onValueChange = { viewModel.maxHumidity = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("Umid. máx") },
                suffix = { Text("%", color = GraySecondaryText) },
                keyboardOptions = numberKeyboard,
                singleLine = true,
                colors = cyberFloraFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = viewModel.active,
                onCheckedChange = { viewModel.active = it },
                colors = SwitchDefaults.colors(checkedTrackColor = GreenPrimary)
            )
            Spacer(Modifier.width(8.dp))
            Text("Ativa", color = TextPrimary)
        }

        viewModel.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = ErrorColor)
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { viewModel.clearForm() }, modifier = Modifier.weight(1f)) { Text("Limpar Campos") }
            Button(
                onClick = { viewModel.save(onSaved = onSaved) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                modifier = Modifier.weight(1f)
            ) { Text("Gravar") }
        }
    }
}