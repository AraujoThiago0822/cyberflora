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
import com.cyberflora.frontend.viewmodel.GrowingAreaViewModel

@Composable
fun GrowingAreaFormScreen(viewModel: GrowingAreaViewModel, onSaved: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            if (viewModel.editingId == null) "Nova Área de Cultivo" else "Editar Área de Cultivo",
            color = TextPrimary, fontSize = 22.sp
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(viewModel.name, { viewModel.name = it }, label = { Text("Nome") }, colors = cyberFloraFieldColors(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            viewModel.description, { viewModel.description = it },
            label = { Text("Descrição") }, colors = cyberFloraFieldColors(),
            minLines = 2, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            viewModel.capacityM2, { viewModel.capacityM2 = it },
            label = { Text("Capacidade (m²)") }, colors = cyberFloraFieldColors(),
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
            OutlinedButton(onClick = { viewModel.clearForm() }, modifier = Modifier.weight(1f)) { Text("Limpar") }
            Button(
                onClick = { viewModel.save(onSaved = onSaved) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                modifier = Modifier.weight(1f)
            ) { Text("Gravar") }
        }
    }
}