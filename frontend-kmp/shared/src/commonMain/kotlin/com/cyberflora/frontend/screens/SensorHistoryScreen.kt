package com.cyberflora.frontend.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberflora.frontend.model.SensorReadingResponse
import com.cyberflora.frontend.theme.BackgroundPrimary
import com.cyberflora.frontend.theme.ErrorColor
import com.cyberflora.frontend.theme.GraySecondaryText
import com.cyberflora.frontend.theme.GreenDark
import com.cyberflora.frontend.theme.GreenPrimary
import com.cyberflora.frontend.theme.NeonPrimary
import com.cyberflora.frontend.theme.SurfaceSecondary
import com.cyberflora.frontend.theme.TextPrimary
import com.cyberflora.frontend.viewmodel.SensorHistoryViewModel
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.roundToLong

private val TIPOS = listOf(
    "soil_moisture" to "Umidade do solo",
    "air_humidity" to "Umidade do ar",
    "temperature" to "Temperatura",
    "luminosity" to "Luminosidade"
)
private val PERIODOS = listOf(1 to "1 h", 6 to "6 h", 24 to "24 h", 72 to "72 h")
private val ISO = Regex("""(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(\.\d+)?""")

@Composable
fun SensorHistoryScreen(areaName: String, deviceId: String?, viewModel: SensorHistoryViewModel, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = NeonPrimary)
            }
            Column {
                Text("Histórico de sensores", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("$areaName · ${deviceId ?: "sem dispositivo"}", color = GraySecondaryText, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))

        if (deviceId.isNullOrBlank()) {
            Text(
                "Esta área não tem dispositivo vinculado. Edite a área e informe o ID do dispositivo (por exemplo, esp32-001).",
                color = GraySecondaryText
            )
            return@Column
        }

        // Atualiza ao abrir, ao trocar o filtro e a cada 10 s enquanto a tela estiver aberta
        LaunchedEffect(viewModel.hours, viewModel.type) {
            while (true) {
                viewModel.refresh()
                delay(10_000)
            }
        }

        val ultimas = viewModel.latest.associateBy { it.sensorType }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            LatestCard("Umidade do solo", ultimas["soil_moisture"], Modifier.weight(1f))
            LatestCard("Umidade do ar", ultimas["air_humidity"], Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            LatestCard("Temperatura", ultimas["temperature"], Modifier.weight(1f))
            LatestCard("Luminosidade", ultimas["luminosity"], Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TIPOS.forEach { (codigo, rotulo) -> Opcao(rotulo, viewModel.type == codigo) { viewModel.type = codigo } }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PERIODOS.forEach { (horas, rotulo) -> Opcao(rotulo, viewModel.hours == horas) { viewModel.hours = horas } }
        }
        Spacer(Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SurfaceSecondary), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                val nomeDoTipo = TIPOS.firstOrNull { it.first == viewModel.type }?.second ?: ""
                Text("$nomeDoTipo · últimas ${viewModel.hours} h", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))
                LineChart(viewModel.history, viewModel.type)
                if (viewModel.isLoading && viewModel.history.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(color = NeonPrimary, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        viewModel.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = ErrorColor, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text("Atualiza sozinho a cada 10 segundos.", color = GraySecondaryText, fontSize = 11.sp)
    }
}

@Composable
private fun Opcao(texto: String, selecionada: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selecionada) GreenPrimary else SurfaceSecondary)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            texto,
            color = if (selecionada) BackgroundPrimary else GraySecondaryText,
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LatestCard(titulo: String, leitura: SensorReadingResponse?, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceSecondary), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, color = GraySecondaryText, fontSize = 11.sp)
            Text(
                if (leitura == null) "—" else "${formatar(leitura.value)} ${leitura.unit}",
                color = NeonPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
            Text(if (leitura == null) "sem leitura" else "às ${horaDe(leitura.readingAt)}", color = GraySecondaryText, fontSize = 10.sp)
        }
    }
}

@Composable
private fun LineChart(pontos: List<SensorReadingResponse>, tipo: String) {
    val dados = pontos.mapNotNull { p -> epochSeconds(p.readingAt)?.let { it to p.value } }.sortedBy { it.first }
    if (dados.size < 2) {
        Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("Aguardando leituras (mínimo de 2 pontos)...", color = GraySecondaryText, fontSize = 12.sp)
        }
        return
    }
    val valores = dados.map { it.second }
    val faixa: Pair<Double, Double> =
        if (tipo == "soil_moisture" || tipo == "air_humidity") 0.0 to 100.0
        else {
            val mn = valores.min()
            val mx = valores.max()
            val folga = max((mx - mn) * 0.15, 1.0)
            (mn - folga) to (mx + folga)
        }
    val yMin = faixa.first
    val yMax = faixa.second
    val t0 = dados.first().first
    val dt = max(dados.last().first - t0, 1.0)

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.height(180.dp).padding(end = 6.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(formatar(yMax), color = GraySecondaryText, fontSize = 10.sp)
            Text(formatar((yMax + yMin) / 2), color = GraySecondaryText, fontSize = 10.sp)
            Text(formatar(yMin), color = GraySecondaryText, fontSize = 10.sp)
        }
        Canvas(modifier = Modifier.weight(1f).height(180.dp)) {
            val w = size.width
            val h = size.height
            for (i in 0..2) {
                val y = h * i / 2f
                drawLine(GreenDark.copy(alpha = 0.5f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
            val caminho = Path()
            var ultimoX = 0f
            var ultimoY = 0f
            dados.forEachIndexed { i, (t, v) ->
                val x = ((t - t0) / dt * w).toFloat()
                val y = (h - ((v - yMin) / (yMax - yMin) * h)).toFloat()
                if (i == 0) caminho.moveTo(x, y) else caminho.lineTo(x, y)
                ultimoX = x
                ultimoY = y
            }
            drawPath(caminho, color = NeonPrimary, style = Stroke(width = 4f))
            drawCircle(NeonPrimary, radius = 7f, center = Offset(ultimoX, ultimoY))
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(start = 34.dp, top = 4.dp)) {
        Text(horaDe(pontos.first().readingAt), color = GraySecondaryText, fontSize = 10.sp)
        Spacer(Modifier.weight(1f))
        Text(horaDe(pontos.last().readingAt), color = GraySecondaryText, fontSize = 10.sp)
    }
}

private fun formatar(valor: Double): String = ((valor * 10).roundToLong() / 10.0).toString()

private fun horaDe(iso: String): String = if (iso.length >= 16) iso.substring(11, 16) else iso

/** Converte "2026-10-20T19:30:12.345" em segundos desde 1970 (sem biblioteca de datas). */
private fun epochSeconds(iso: String): Double? {
    val g = ISO.find(iso)?.groupValues ?: return null
    val y = g[1].toLong()
    val mo = g[2].toLong()
    val d = g[3].toLong()
    val h = g[4].toLong()
    val mi = g[5].toLong()
    val s = g[6].toLong()
    val fracao = if (g[7].isNotEmpty()) g[7].toDouble() else 0.0
    val ano = if (mo <= 2) y - 1 else y
    val era = (if (ano >= 0) ano else ano - 399) / 400
    val yoe = ano - era * 400
    val doy = (153 * (mo + (if (mo > 2) -3 else 9)) + 2) / 5 + d - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    val dias = era * 146097 + doe - 719468
    return (dias * 86400 + h * 3600 + mi * 60 + s).toDouble() + fracao
}