package com.cyberflora.frontend.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberflora.frontend.theme.GraySecondaryText
import com.cyberflora.frontend.theme.NeonPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000) // tempo total da splash: ~2 segundos de exibição + transição
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "CYBERFLORA",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = NeonPrimary
            )
            Text(
                text = "SMART GROWING SYSTEM",
                fontSize = 14.sp,
                color = GraySecondaryText
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = NeonPrimary)
        }
    }
}