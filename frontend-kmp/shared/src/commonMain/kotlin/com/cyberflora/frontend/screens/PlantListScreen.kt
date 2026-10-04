package com.cyberflora.frontend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberflora.frontend.model.PlantResponse
import com.cyberflora.frontend.theme.ErrorColor
import com.cyberflora.frontend.theme.GraySecondaryText
import com.cyberflora.frontend.theme.GreenPrimary
import com.cyberflora.frontend.theme.NeonPrimary
import com.cyberflora.frontend.theme.SurfaceSecondary
import com.cyberflora.frontend.theme.TextPrimary
import com.cyberflora.frontend.viewmodel.PlantViewModel

@Composable
fun PlantListScreen(viewModel: PlantViewModel, onAddNew: () -> Unit, onEditItem: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Minhas Plantas", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Nova Planta")
            }
        }
        Spacer(Modifier.height(16.dp))

        when {
            viewModel.isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonPrimary)
            }
            viewModel.plants.isEmpty() -> Text("Nenhuma planta cadastrada ainda.", color = GraySecondaryText)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(viewModel.plants, key = { it.id }) { plant ->
                    PlantCard(plant = plant, onEdit = { onEditItem(plant.id) }, onDelete = { viewModel.delete(plant.id) })
                }
            }
        }
    }
}

@Composable
private fun PlantCard(plant: PlantResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(plant.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(plant.species, color = GraySecondaryText, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text("Umidade ideal: ${plant.minHumidity.toInt()}%–${plant.maxHumidity.toInt()}%", color = NeonPrimary, fontSize = 12.sp)
                Text(
                    if (plant.active) "Ativa" else "Inativa",
                    color = if (plant.active) GreenPrimary else ErrorColor,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = NeonPrimary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = ErrorColor) }
        }
    }
}