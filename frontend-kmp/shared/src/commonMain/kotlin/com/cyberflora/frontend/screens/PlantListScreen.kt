package com.cyberflora.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        CrudScreenHeader(
            icon = Icons.Default.LocalFlorist,
            title = "Minhas Plantas",
            actionLabel = "Nova Planta",
            onAction = onAddNew
        )
        Spacer(Modifier.height(20.dp))

        when {
            viewModel.isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonPrimary)
            }
            viewModel.plants.isEmpty() -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Nenhuma planta cadastrada ainda.", color = GraySecondaryText)
            }
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
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (plant.active) GreenPrimary else GraySecondaryText)
            )
            Row(
                modifier = Modifier.weight(1f).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(plant.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (plant.active) GreenPrimary else GraySecondaryText)
                        )
                    }
                    Text(plant.species, color = GraySecondaryText, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = NeonPrimary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${plant.minHumidity.toInt()}%–${plant.maxHumidity.toInt()}%", color = NeonPrimary, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = NeonPrimary.copy(alpha = 0.15f),
                        contentColor = NeonPrimary
                    )
                ) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                Spacer(Modifier.width(6.dp))
                FilledTonalIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = ErrorColor.copy(alpha = 0.15f),
                        contentColor = ErrorColor
                    )
                ) { Icon(Icons.Default.Delete, contentDescription = "Apagar") }
            }
        }
    }
}