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

private const val WIDE_SCREEN_BREAKPOINT_DP = 600

@Composable
fun HomeNavigation() {
    var currentSection by remember { mutableStateOf<CrudSection>(CrudSection.Plants) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isWideScreen = maxWidth.value > WIDE_SCREEN_BREAKPOINT_DP

        if (isWideScreen) {
            // Desktop/tablet: Sidebar lateral
            Row(modifier = Modifier.fillMaxSize()) {
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
                        colors = railColors()
                    )
                    Spacer(Modifier.height(4.dp))
                    NavigationRailItem(
                        selected = currentSection == CrudSection.GrowingAreas,
                        onClick = { currentSection = CrudSection.GrowingAreas },
                        icon = { Icon(Icons.Default.Yard, contentDescription = "Áreas de Cultivo") },
                        label = { Text("Áreas") },
                        colors = railColors()
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    CrudContent(currentSection)
                }
            }
        } else {
            // Mobile: Bottom Navigation
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    CrudContent(currentSection)
                }
                NavigationBar(containerColor = SurfaceSecondary) {
                    NavigationBarItem(
                        selected = currentSection == CrudSection.Plants,
                        onClick = { currentSection = CrudSection.Plants },
                        icon = { Icon(Icons.Default.LocalFlorist, contentDescription = "Plantas") },
                        label = { Text("Plantas") },
                        colors = barColors()
                    )
                    NavigationBarItem(
                        selected = currentSection == CrudSection.GrowingAreas,
                        onClick = { currentSection = CrudSection.GrowingAreas },
                        icon = { Icon(Icons.Default.Yard, contentDescription = "Áreas de Cultivo") },
                        label = { Text("Áreas") },
                        colors = barColors()
                    )
                }
            }
        }
    }
}

@Composable
private fun CrudContent(section: CrudSection) {
    when (section) {
        CrudSection.Plants -> PlantNavigation()
        CrudSection.GrowingAreas -> GrowingAreaNavigation()
    }
}

@Composable
private fun railColors() = NavigationRailItemDefaults.colors(
    selectedIconColor = SurfaceSecondary,
    selectedTextColor = NeonPrimary,
    indicatorColor = GreenPrimary,
    unselectedIconColor = GraySecondaryText,
    unselectedTextColor = GraySecondaryText
)

@Composable
private fun barColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = SurfaceSecondary,
    selectedTextColor = NeonPrimary,
    indicatorColor = GreenPrimary,
    unselectedIconColor = GraySecondaryText,
    unselectedTextColor = GraySecondaryText
)