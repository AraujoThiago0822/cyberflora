package com.cyberflora.frontend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberflora.frontend.theme.ErrorColor
import com.cyberflora.frontend.theme.GreenPrimary
import com.cyberflora.frontend.theme.TextPrimary
import com.cyberflora.frontend.viewmodel.PlantViewModel

@Composable
fun PlantFormScreen(viewModel: PlantViewModel, onSaved: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            if (viewModel.editingId == null) "Nova Planta" else "Editar Planta",
            color = TextPrimary, fontSize = 22.sp
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(viewModel.name, { viewModel.name = it }, label = { Text("Nome") }, colors = cyberFloraFieldColors(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(viewModel.species, { viewModel.species = it }, label = { Text("Espécie") }, colors = cyberFloraFieldColors(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(viewModel.plantingDate, { viewModel.plantingDate = it }, label = { Text("Data de plantio (AAAA-MM-DD)") }, colors = cyberFloraFieldColors(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(viewModel.minHumidity, { viewModel.minHumidity = it }, label = { Text("Umid. mín (%)") }, colors = cyberFloraFieldColors(), modifier = Modifier.weight(1f))
            OutlinedTextField(viewModel.maxHumidity, { viewModel.maxHumidity = it }, label = { Text("Umid. máx (%)") }, colors = cyberFloraFieldColors(), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = viewModel.active, onCheckedChange = { viewModel.active = it }, colors = SwitchDefaults.colors(checkedTrackColor = GreenPrimary))
            Spacer(Modifier.width(8.dp))
            Text("Ativa", color = TextPrimary)
        }

        viewModel.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = ErrorColor)
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { viewModel.clearForm() }, modifier = Modifier.weight(1f)) { Text("Limpar") }
            Button(
                onClick = { viewModel.save(onSaved = onSaved) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                modifier = Modifier.weight(1f)
            ) { Text("Gravar") }
        }
    }
}