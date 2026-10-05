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
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberflora.frontend.model.GrowingAreaResponse
import com.cyberflora.frontend.theme.GraySecondaryText
import com.cyberflora.frontend.theme.GreenPrimary
import com.cyberflora.frontend.theme.NeonPrimary
import com.cyberflora.frontend.theme.SurfaceSecondary
import com.cyberflora.frontend.theme.TextPrimary
import com.cyberflora.frontend.viewmodel.GrowingAreaViewModel

@Composable
fun GrowingAreaListScreen(viewModel: GrowingAreaViewModel, onAddNew: () -> Unit, onEditItem: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        CrudScreenHeader(
            icon = Icons.Default.Yard,
            title = "Áreas de Cultivo",
            actionLabel = "Nova Área",
            onAction = onAddNew
        )
        Spacer(Modifier.height(20.dp))

        when {
            viewModel.isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonPrimary)
            }
            viewModel.areas.isEmpty() -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Nenhuma área de cultivo cadastrada ainda.", color = GraySecondaryText)
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(viewModel.areas, key = { it.id }) { area ->
                    GrowingAreaCard(area = area, onEdit = { onEditItem(area.id) }, onDelete = { viewModel.delete(area.id) })
                }
            }
        }
    }
}

@Composable
private fun GrowingAreaCard(area: GrowingAreaResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (area.irrigationAutomatic) GreenPrimary else GraySecondaryText)
            )
            Row(
                modifier = Modifier.weight(1f).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(area.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (area.irrigationAutomatic) GreenPrimary else GraySecondaryText)
                        )
                    }
                    Text(
                        area.description.take(60) + if (area.description.length > 60) "..." else "",
                        color = GraySecondaryText, fontSize = 13.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${area.capacityM2} m² · ${if (area.irrigationAutomatic) "Irrigação automática" else "Irrigação manual"}",
                        color = NeonPrimary, fontSize = 12.sp)
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
                        containerColor = com.cyberflora.frontend.theme.ErrorColor.copy(alpha = 0.15f),
                        contentColor = com.cyberflora.frontend.theme.ErrorColor
                    )
                ) { Icon(Icons.Default.Delete, contentDescription = "Apagar") }
            }
        }
    }
}