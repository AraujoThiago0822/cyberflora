package com.cyberflora.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberflora.frontend.theme.GraySecondaryText
import com.cyberflora.frontend.theme.NeonPrimary
import com.cyberflora.frontend.theme.SurfaceSecondary
import com.cyberflora.frontend.theme.TextPrimary

@Composable
fun CyberFloraLogo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceSecondary)
                .border(2.dp, NeonPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Eco, contentDescription = null, tint = NeonPrimary, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("CYBERFLORA", color = NeonPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Text("SMART GROWING SYSTEM", color = GraySecondaryText, fontSize = 11.sp, letterSpacing = 2.sp)
    }
}

@Composable
fun cyberFloraFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonPrimary,
    unfocusedBorderColor = GraySecondaryText,
    focusedLabelColor = NeonPrimary,
    unfocusedLabelColor = GraySecondaryText,
    cursorColor = NeonPrimary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = SurfaceSecondary,
    unfocusedContainerColor = SurfaceSecondary
)