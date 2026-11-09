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
import com.cyberflora.frontend.viewmodel.GrowingAreaViewModel

private fun caractereDeDispositivo(c: Char): Boolean =
    c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '-' || c == '_'

@Composable
fun GrowingAreaFormScreen(viewModel: GrowingAreaViewModel, onSaved: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(
            if (viewModel.editingId == null) "Nova Área de Cultivo" else "Editar Área de Cultivo",
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
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = { Text("Descrição") },
            minLines = 2,
            colors = cyberFloraFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = viewModel.capacityM2,
            onValueChange = { viewModel.capacityM2 = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(10) },
            label = { Text("Capacidade (m²)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = cyberFloraFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = viewModel.deviceId,
            onValueChange = { viewModel.deviceId = it.filter { c -> caractereDeDispositivo(c) }.take(64) },
            label = { Text("Dispositivo (ID)") },
            placeholder = { Text("esp32-001", color = GraySecondaryText) },
            supportingText = { Text("Opcional. Cada dispositivo só pode estar em uma área.", color = GraySecondaryText) },
            singleLine = true,
            colors = cyberFloraFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = viewModel.irrigationAutomatic,
                onCheckedChange = { viewModel.irrigationAutomatic = it },
                colors = SwitchDefaults.colors(checkedTrackColor = GreenPrimary)
            )
            Spacer(Modifier.width(8.dp))
            Text("Irrigação automática", color = TextPrimary)
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