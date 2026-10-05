package com.cyberflora.frontend.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cyberflora.frontend.theme.GraySecondaryText
import com.cyberflora.frontend.theme.GreenPrimary
import com.cyberflora.frontend.theme.NeonPrimary
import com.cyberflora.frontend.theme.SurfaceSecondary

@Composable
fun HomeNavigation() {
    var currentSection by remember { mutableStateOf<CrudSection>(CrudSection.Plants) }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavigationRail(
            containerColor = SurfaceSecondary,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            NavigationRailItem(
                selected = currentSection == CrudSection.Plants,
                onClick = { currentSection = CrudSection.Plants },
                icon = { Icon(Icons.Default.LocalFlorist, contentDescription = "Plantas") },
                label = { Text("Plantas") },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = SurfaceSecondary,
                    selectedTextColor = NeonPrimary,
                    indicatorColor = GreenPrimary,
                    unselectedIconColor = GraySecondaryText,
                    unselectedTextColor = GraySecondaryText
                )
            )

            Spacer(Modifier.height(4.dp))

            NavigationRailItem(
                selected = currentSection == CrudSection.GrowingAreas,
                onClick = { currentSection = CrudSection.GrowingAreas },
                icon = { Icon(Icons.Default.Yard, contentDescription = "Áreas de Cultivo") },
                label = { Text("Áreas") },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = SurfaceSecondary,
                    selectedTextColor = NeonPrimary,
                    indicatorColor = GreenPrimary,
                    unselectedIconColor = GraySecondaryText,
                    unselectedTextColor = GraySecondaryText
                )
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (currentSection) {
                CrudSection.Plants -> PlantNavigation()
                CrudSection.GrowingAreas -> GrowingAreaNavigation()
            }
        }
    }
}