package ru.dimlife.lastshelter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Void = Color(0xFF070B0A)
private val Panel = Color(0xFF101715)
private val Neon = Color(0xFF73FF8A)
private val Muted = Color(0xFF8B9B90)
private val White = Color(0xFFE8F2E9)
private val Alert = Color(0xFFFFC857)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Void) {
                    ShelterScreen()
                }
            }
        }
    }
}

@Composable
private fun ShelterScreen() {
    var power by remember { mutableIntStateOf(72) }
    var water by remember { mutableIntStateOf(64) }
    var oxygen by remember { mutableIntStateOf(88) }
    var supplies by remember { mutableIntStateOf(35) }
    var day by remember { mutableIntStateOf(1) }
    var message by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("DIMLIFE // SURVIVAL SYSTEM", color = Neon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("LAST SHELTER", color = White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            }
            Box(
                modifier = Modifier.border(1.dp, Neon.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("DAY %02d".format(day), color = Neon, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp))
                .border(1.dp, Neon.copy(alpha = 0.28f), RoundedCornerShape(14.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("SECTOR 01  /  COMMAND ROOM", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Убежище функционирует", color = Neon, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier.fillMaxWidth().height(116.dp).background(Color(0xFF0A110D), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("⌁  ⌁  ⌁", color = Neon, fontSize = 28.sp)
                    Text("[ REACTOR ONLINE ]", color = Neon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("MAIN POWER CORE • MK-I", color = Muted, fontSize = 10.sp)
                }
            }
            Text("После катастрофы прошло мало времени. Запасы ограничены. Удерживайте системы в рабочем состоянии.", color = White, fontSize = 13.sp, lineHeight = 19.sp)
        }

        Text("СИСТЕМЫ ЖИЗНЕОБЕСПЕЧЕНИЯ", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            ResourceRow("ЭНЕРГИЯ", power, "⚡")
            ResourceRow("ВОДА", water, "◈")
            ResourceRow("КИСЛОРОД", oxygen, "◎")
            ResourceRow("ПРОВИЗИЯ", supplies, "▦")
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("ЖУРНАЛ СИСТЕМЫ", color = Neon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                when (message) {
                    1 -> "Генератор прошёл проверку. Энергия увеличена на 12 единиц."
                    2 -> "Запущен цикл фильтрации. Запас воды увеличен на 10 единиц."
                    3 -> "Смена завершена. Убежище расходует ресурсы."
                    else -> "Связь с поверхностью отсутствует. Ожидание новых данных..."
                },
                color = White, fontSize = 13.sp, lineHeight = 19.sp
            )
        }

        Button(
            onClick = { power = (power + 12).coerceAtMost(100); message = 1 },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Void),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("ПЕРЕЗАПУСТИТЬ ГЕНЕРАТОР", fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { water = (water + 10).coerceAtMost(100); message = 2 },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = Neon),
                shape = RoundedCornerShape(8.dp)
            ) { Text("ФИЛЬТР ВОДЫ", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            Button(
                onClick = {
                    day += 1
                    power = (power - 8).coerceAtLeast(0)
                    water = (water - 5).coerceAtLeast(0)
                    oxygen = (oxygen - 6).coerceAtLeast(0)
                    supplies = (supplies - 7).coerceAtLeast(0)
                    message = 3
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF202A24), contentColor = Alert),
                shape = RoundedCornerShape(8.dp)
            ) { Text("ПРОЖИТЬ ДЕНЬ", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        }
        Text("BUILD 0.1.0 • OFFLINE PROTOTYPE", color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.size(4.dp))
    }
}

@Composable
private fun ResourceRow(label: String, value: Int, symbol: String) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(10.dp)).padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("$symbol  $label", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("$value%", color = if (value < 25) Alert else Neon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(Color(0xFF27332B), RoundedCornerShape(4.dp))) {
            Box(
                modifier = Modifier.fillMaxWidth(value / 100f).height(5.dp)
                    .background(if (value < 25) Alert else Neon, RoundedCornerShape(4.dp))
            )
        }
    }
}
