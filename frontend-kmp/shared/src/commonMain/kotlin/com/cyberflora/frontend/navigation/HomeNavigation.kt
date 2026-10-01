package com.cyberflora.frontend.navigation

import androidx.compose.foundation.layout.* import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun HomeNavigation() {
    var currentSection by remember { mutableStateOf<CrudSection>(CrudSection.Plants) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra simples de navegação entre CRUDs (Nível 1) — evolui pra
        // Bottom Navigation (mobile) / Sidebar (desktop) numa parte futura.
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = { currentSection = CrudSection.Plants }) {
                Text("🌱 Plantas")
            }
            Button(onClick = { currentSection = CrudSection.GrowingAreas }) {
                Text("🪴 Áreas de Cultivo")
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentSection) {
                CrudSection.Plants -> CrudNavigation(title = "Plantas")
                CrudSection.GrowingAreas -> CrudNavigation(title = "Áreas de Cultivo")
            }
        }
    }
}