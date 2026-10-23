package ru.dimlife.lastshelter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Void = Color(0xFF070B09)
private val Panel = Color(0xFF111A15)
private val Panel2 = Color(0xFF17231B)
private val Neon = Color(0xFF73FF8A)
private val Muted = Color(0xFF8A9A8E)
private val White = Color(0xFFE8F2E9)
private val Alert = Color(0xFFFFC857)
private val Red = Color(0xFFFF6D6D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Void) {
                    LastShelterGame(applicationContext)
                }
            }
        }
    }
}

private data class Room(val name: String, val icon: String, val cost: Int, val description: String)
private val roomCatalog = listOf(
    Room("Фильтрация", "◈", 20, "Вода +8 за день"),
    Room("Ферма", "▦", 30, "Провизия +8 за день"),
    Room("Медблок", "+", 35, "Поддерживает здоровье"),
    Room("Мастерская", "⚙", 25, "Открывает улучшения")
)

@Composable
private fun LastShelterGame(context: Context) {
    val prefs = remember { context.getSharedPreferences("last_shelter_save", Context.MODE_PRIVATE) }
    var power by remember { mutableIntStateOf(prefs.getInt("power", 72)) }
    var water by remember { mutableIntStateOf(prefs.getInt("water", 64)) }
    var oxygen by remember { mutableIntStateOf(prefs.getInt("oxygen", 88)) }
    var food by remember { mutableIntStateOf(prefs.getInt("food", 35)) }
    var day by remember { mutableIntStateOf(prefs.getInt("day", 1)) }
    var scrap by remember { mutableIntStateOf(prefs.getInt("scrap", 55)) }
    var tab by remember { mutableStateOf("БАЗА") }
    var message by remember { mutableStateOf("Сканер поверхности молчит. Убежище ждёт приказов.") }
    var eventCount by remember { mutableIntStateOf(0) }
    val builtRooms = remember {
        mutableStateListOf<String>().apply {
            addAll(prefs.getStringSet("rooms", setOf("Командный центр", "Реактор")) ?: setOf("Командный центр", "Реактор"))
        }
    }

    fun save() {
        prefs.edit().putInt("power", power).putInt("water", water).putInt("oxygen", oxygen)
            .putInt("food", food).putInt("day", day).putInt("scrap", scrap)
            .putStringSet("rooms", builtRooms.toSet()).apply()
    }
    fun report(text: String) { message = text; save() }

    Column(
        modifier = Modifier.fillMaxSize().background(Void).verticalScroll(rememberScrollState()).padding(bottom = 18.dp)
    ) {
        Header(day, scrap)
        when (tab) {
            "БАЗА" -> {
                BunkerMap(builtRooms)
                SectionLabel("СИСТЕМЫ ЖИЗНЕОБЕСПЕЧЕНИЯ", "LIVE MONITOR")
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    ResourceRow("ЭНЕРГИЯ", power, "⚡")
                    ResourceRow("ВОДА", water, "◈")
                    ResourceRow("КИСЛОРОД", oxygen, "◎")
                    ResourceRow("ПРОВИЗИЯ", food, "▦")
                }
                SectionLabel("ЖУРНАЛ СИСТЕМЫ", "RECENT EVENT")
                Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(15.dp)) {
                    Text("● SYSTEM MESSAGE", color = Neon, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(7.dp))
                    Text(message, color = White, fontSize = 13.sp, lineHeight = 19.sp)
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    ActionButton("⚡  ОБСЛУЖИТЬ РЕАКТОР", "Затрачено 5 металлолома. Энергосистема стабилизирована.") {
                        if (scrap >= 5) { scrap -= 5; power = (power + 18).coerceAtMost(100); report("Реактор обслужен. Заряд энергии +18%.") }
                        else report("Недостаточно металлолома для ремонта реактора.")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        SmallAction("◈  ОЧИСТИТЬ ВОДУ", Modifier.weight(1f)) {
                            if (power >= 5) { power -= 5; water = (water + 16).coerceAtMost(100); report("Фильтры запущены. Запас воды +16%.") }
                            else report("Недостаточно энергии для запуска фильтров.")
                        }
                        SmallAction("☾  СМЕНА ДНЯ", Modifier.weight(1f), Alert) {
                            day += 1
                            power = (power - 8).coerceAtLeast(0)
                            oxygen = (oxygen - 7).coerceAtLeast(0)
                            water = (water - (if ("Фильтрация" in builtRooms) 2 else 6)).coerceAtLeast(0)
                            food = (food + (if ("Ферма" in builtRooms) 3 else -8)).coerceIn(0, 100)
                            if (day % 3 == 0) { scrap += 8; report("День $day. Разобрали обломки в техническом тоннеле: металлолом +8.") }
                            else report("День $day завершён. Системы потребили ресурсы. Проверь запасы.")
                            save()
                        }
                    }
                }
            }
            "СТРОЙКА" -> {
                SectionLabel("РАСШИРЕНИЕ УБЕЖИЩА", "BUILD MODULE")
                Text("Каждая комната меняет экономику базы. Выбирай, что поможет пережить следующий день.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    roomCatalog.forEach { room ->
                        val built = room.name in builtRooms
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp))
                                .border(1.dp, if (built) Neon.copy(alpha = 0.5f) else Color(0xFF26352A), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(48.dp).background(Panel2, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
                                Text(room.icon, color = Neon, fontSize = 23.sp, fontWeight = FontWeight.Black)
                            }
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(room.name.uppercase(), color = White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                Text(room.description, color = Muted, fontSize = 11.sp)
                                Text(if (built) "МОДУЛЬ УСТАНОВЛЕН" else "СТОИМОСТЬ: ◈ ${room.cost} МЕТАЛЛА", color = if (built) Neon else Alert, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            if (built) Text("✓", color = Neon, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            else Button(
                                onClick = {
                                    if (scrap >= room.cost) {
                                        scrap -= room.cost
                                        builtRooms.add(room.name)
                                        report("Модуль «${room.name}» построен. Его эффект начнёт действовать при следующей смене.")
                                    } else report("Не хватает металлолома. Отправься на разведку.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Void),
                                shape = RoundedCornerShape(7.dp)
                            ) { Text("СТРОИТЬ", fontSize = 9.sp, fontWeight = FontWeight.Black) }
                        }
                    }
                }
                SectionLabel("ТЕКУЩИЕ ПРИОРИТЕТЫ", "TACTICAL NOTE")
                Text("Рекомендуется построить ферму, прежде чем запасы провизии упадут до нуля.", color = Alert, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
            }
            "РАЗВЕДКА" -> {
                SectionLabel("ВЫХОД НА ПОВЕРХНОСТЬ", "EXPLORATION")
                SurfaceCard {
                    Text("СЕКТОР: СТАРЫЙ ГОРОД", color = Neon, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Радиационный фон нестабилен. За дверью шлюза могут быть полезные материалы — или то, что привело мир к гибели.", color = White, fontSize = 14.sp, lineHeight = 21.sp)
                    Canvas(modifier = Modifier.fillMaxWidth().height(145.dp)) {
                        val w = size.width
                        val h = size.height
                        for (i in 0..8) {
                            val x = w * i / 8f
                            drawLine(Color(0xFF213C2A), Offset(x, 0f), Offset(x, h), 1f)
                        }
                        for (i in 0..5) {
                            val y = h * i / 5f
                            drawLine(Color(0xFF213C2A), Offset(0f, y), Offset(w, y), 1f)
                        }
                        val skyline = Path().apply {
                            moveTo(0f, h * .82f); lineTo(w * .12f, h * .82f); lineTo(w * .12f, h * .35f)
                            lineTo(w * .22f, h * .35f); lineTo(w * .22f, h * .55f); lineTo(w * .35f, h * .55f)
                            lineTo(w * .35f, h * .18f); lineTo(w * .48f, h * .18f); lineTo(w * .48f, h * .68f)
                            lineTo(w * .62f, h * .68f); lineTo(w * .62f, h * .3f); lineTo(w * .78f, h * .3f)
                            lineTo(w * .78f, h * .58f); lineTo(w, h * .58f)
                        }
                        drawPath(skyline, Neon.copy(alpha = .8f), style = Stroke(width = 3f, cap = StrokeCap.Square))
                        drawCircle(Alert, radius = 6f, center = Offset(w * .8f, h * .13f))
                    }
                    Text("ПОСЛЕДНИЙ СИГНАЛ: 03:17 / ИСТОЧНИК НЕИЗВЕСТЕН", color = Muted, fontSize = 9.sp)
                }
                ActionButton("↗  ОТПРАВИТЬ РАЗВЕДГРУППУ", "Экспедиция вернулась.") {
                    eventCount += 1
                    when (eventCount % 3) {
                        1 -> { scrap += 18; report("Экспедиция вернулась с металлоломом: +18. Ни одного живого человека не найдено.") }
                        2 -> { scrap += 8; food = (food + 14).coerceAtMost(100); report("В заброшенном складе найдены консервы и детали. Провизия +14%, металл +8.") }
                        else -> { oxygen = (oxygen - 8).coerceAtLeast(0); scrap += 25; report("Разведчики нашли редкие детали, но повредили фильтр шлюза. Металл +25, кислород -8%.") }
                    }
                    save()
                }
                Text("Экспедиции доступны без ожидания. Каждый выход меняет состояние ресурсов и приносит новую запись в журнал.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(horizontal = 16.dp))
            }
            else -> {
                SectionLabel("СИСТЕМНЫЙ ЖУРНАЛ", "ARCHIVE")
                SurfaceCard {
                    Text("ДЕНЬ $day — ТЕКУЩИЙ ОТЧЁТ", color = Neon, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text(message, color = White, fontSize = 14.sp, lineHeight = 21.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("МЕТАЛЛОЛОМ: $scrap", color = Alert, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("МОДУЛИ: ${builtRooms.joinToString(", ")}", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
                }
                ActionButton("СОХРАНИТЬ ПРОГРЕСС", "Прогресс сохранён локально на устройстве.") { save(); report("Сохранение обновлено. Убежище будет ждать тебя.") }
            }
        }
        Spacer(Modifier.height(12.dp))
        BottomNav(tab) { tab = it }
        Text("DIMLIFE SYSTEMS • BUILD 0.2.0", color = Color(0xFF53645A), fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
    }
}

@Composable
private fun Header(day: Int, scrap: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("DIMLIFE // SURVIVAL SYSTEM", color = Neon, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text("LAST SHELTER", color = White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("DAY %02d".format(day), color = Neon, fontSize = 12.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.border(1.dp, Neon.copy(alpha = .7f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 7.dp))
                Spacer(Modifier.height(5.dp))
                Text("◈ $scrap SCRAP", color = Alert, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BunkerMap(rooms: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().background(Panel, RoundedCornerShape(14.dp))
        .border(1.dp, Neon.copy(alpha = .3f), RoundedCornerShape(14.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("SECTOR 01 / COMMAND DECK", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text("● ONLINE", color = Neon, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(210.dp).padding(top = 12.dp)) {
            val cellW = size.width / 3f
            val cellH = size.height / 2f
            drawLine(Neon.copy(alpha = .45f), Offset(cellW / 2, cellH), Offset(cellW * 2.5f, cellH), 3f)
            drawLine(Neon.copy(alpha = .45f), Offset(cellW * 1.5f, cellH / 2), Offset(cellW * 1.5f, cellH * 1.5f), 3f)
            val rects = listOf(
                floatArrayOf(.04f,.06f,.27f,.38f), floatArrayOf(.365f,.06f,.27f,.38f), floatArrayOf(.69f,.06f,.27f,.38f),
                floatArrayOf(.04f,.56f,.27f,.38f), floatArrayOf(.365f,.56f,.27f,.38f), floatArrayOf(.69f,.56f,.27f,.38f)
            )
            val labels = listOf("REACTOR", "COMMAND", "AIRLOCK", "WATER", "HABITAT", "STORAGE")
            rects.forEachIndexed { i, r ->
                val left = size.width * r[0]; val top = size.height * r[1]
                val rw = size.width * r[2]; val rh = size.height * r[3]
                val active = i == 0 || i == 1 || (i == 3 && "Фильтрация" in rooms) || (i == 4 && "Ферма" in rooms)
                drawRoundRect(if (active) Color(0xFF183B23) else Color(0xFF111B15), Offset(left, top), Size(rw, rh), androidx.compose.ui.geometry.CornerRadius(10f))
                drawRoundRect(if (active) Neon else Color(0xFF3D5143), Offset(left, top), Size(rw, rh), androidx.compose.ui.geometry.CornerRadius(10f), style = Stroke(if (active) 2f else 1f))
                drawCircle(if (active) Neon else Muted, 4f, Offset(left + rw/2, top + rh*.34f))
                val p = android.graphics.Paint().apply { color = if (active) android.graphics.Color.rgb(130,255,150) else android.graphics.Color.rgb(135,154,140); textSize = 10f; textAlign = android.graphics.Paint.Align.CENTER; typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD) }
                drawContext.canvas.nativeCanvas.drawText(labels[i], left + rw/2, top + rh*.76f, p)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("СХЕМА УБЕЖИЩА • ${rooms.size} МОДУЛЕЙ АКТИВНО", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionLabel(title: String, tag: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 17.dp, end = 17.dp, top = 22.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(tag, color = Color(0xFF4F6655), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ResourceRow(label: String, value: Int, symbol: String) {
    Column(modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(10.dp)).padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("$symbol  $label", color = White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Text("$value%", color = if (value < 25) Alert else Neon, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
        Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(Color(0xFF27332B), RoundedCornerShape(4.dp))) {
            Box(modifier = Modifier.fillMaxWidth(value / 100f).height(5.dp).background(if (value < 25) Alert else Neon, RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun SurfaceCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().background(Panel, RoundedCornerShape(14.dp))
        .border(1.dp, Neon.copy(alpha = .25f), RoundedCornerShape(14.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

@Composable
private fun ActionButton(label: String, description: String, action: () -> Unit) {
    Button(onClick = action, modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Void), shape = RoundedCornerShape(8.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SmallAction(label: String, modifier: Modifier = Modifier, color: Color = Neon, action: () -> Unit) {
    Button(onClick = action, modifier = modifier.height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Panel2, contentColor = color),
        shape = RoundedCornerShape(8.dp)) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

@Composable
private fun BottomNav(selected: String, onSelect: (String) -> Unit) {
    val items = listOf("БАЗА" to "⌂", "СТРОЙКА" to "▦", "РАЗВЕДКА" to "↗", "ЖУРНАЛ" to "≡")
    Row(modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        items.forEach { (label, icon) ->
            val active = label == selected
            Column(modifier = Modifier.weight(1f).background(if (active) Color(0xFF203C27) else Color.Transparent, RoundedCornerShape(8.dp))
                .clickable { onSelect(label) }.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(icon, color = if (active) Neon else Muted, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(label, color = if (active) Neon else Muted, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
