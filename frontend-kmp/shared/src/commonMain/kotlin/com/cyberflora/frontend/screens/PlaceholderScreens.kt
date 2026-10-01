package com.cyberflora.frontend.screens

import androidx.compose.foundation.layout.* import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoginPlaceholder(onLoginSuccess: () -> Unit, onGoToRegister: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TELA DE LOGIN (placeholder)", color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLoginSuccess) { Text("Simular login OK") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onGoToRegister) { Text("Ir para Registro") }
    }
}

@Composable
fun RegisterPlaceholder(onBackToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TELA DE REGISTRO (placeholder)", color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBackToLogin) { Text("Voltar para Login") }
    }
}

@Composable
fun ListPlaceholder(title: String, onAddNew: () -> Unit, onEditItem: (Long) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("LISTAGEM — $title (placeholder)", color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddNew) { Text("+ Novo") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onEditItem(1L) }) { Text("Simular editar item id=1") }
    }
}

@Composable
fun FormPlaceholder(title: String, id: Long?, onSave: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val modo = if (id == null) "CRIAÇÃO" else "EDIÇÃO (id=$id)"
        Text("FORMULÁRIO — $title — $modo (placeholder)", color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSave) { Text("Gravar (volta pra Listagem)") }
    }
}