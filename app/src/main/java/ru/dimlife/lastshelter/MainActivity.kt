package ru.dimlife.lastshelter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

private val Void = Color(0xFF060A08)
private val Wall = Color(0xFF334139)
private val Floor = Color(0xFF17221B)
private val FloorLight = Color(0xFF202E25)
private val Neon = Color(0xFF79FF91)
private val TextWhite = Color(0xFFEAF4EC)
private val Muted = Color(0xFF91A096)
private val Amber = Color(0xFFFFC65C)
private val Blue = Color(0xFF76D8FF)
private val Red = Color(0xFFFF7474)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize(), color = Void) { ShelterGame(applicationContext) } } }
    }
}

private data class Chamber(val name: String, val kind: Int, val cost: Int, val built: Boolean = false)
private val roomNames = listOf("РЕАКТОР", "КОМАНДНЫЙ ЦЕНТР", "ЖИЛОЙ БЛОК", "ФИЛЬТР ВОДЫ", "ФЕРМА", "МЕДБЛОК")

@Composable
private fun ShelterGame(context: Context) {
    val prefs = remember { context.getSharedPreferences("last_shelter_save", Context.MODE_PRIVATE) }
    var power by remember { mutableIntStateOf(prefs.getInt("power", 72)) }
    var water by remember { mutableIntStateOf(prefs.getInt("water", 64)) }
    var oxygen by remember { mutableIntStateOf(prefs.getInt("oxygen", 88)) }
    var food by remember { mutableIntStateOf(prefs.getInt("food", 35)) }
    var day by remember { mutableIntStateOf(prefs.getInt("day", 1)) }
    var scrap by remember { mutableIntStateOf(prefs.getInt("scrap", 55)) }
    var survivors by remember { mutableIntStateOf(prefs.getInt("survivors", 4)) }
    var selected by remember { mutableIntStateOf(1) }
    var builtWater by remember { mutableStateOf(prefs.getBoolean("built_water", false)) }
    var builtFarm by remember { mutableStateOf(prefs.getBoolean("built_farm", false)) }
    var builtMed by remember { mutableStateOf(prefs.getBoolean("built_med", false)) }
    var message by remember { mutableStateOf("Смена дежурных завершена. Все системы ожидают проверки.") }
    var tab by remember { mutableStateOf("УБЕЖИЩЕ") }

    fun save() {
        prefs.edit().putInt("power", power).putInt("water", water).putInt("oxygen", oxygen)
            .putInt("food", food).putInt("day", day).putInt("scrap", scrap).putInt("survivors", survivors)
            .putBoolean("built_water", builtWater).putBoolean("built_farm", builtFarm).putBoolean("built_med", builtMed).apply()
    }
    fun log(s: String) { message = s; save() }

    Column(Modifier.fillMaxSize().background(Void)) {
        Header(day, scrap, survivors)
        when (tab) {
            "УБЕЖИЩЕ" -> {
                Text("ЖИЛОЙ СЕКТОР  /  КАМЕРА НАБЛЮДЕНИЯ", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 8.dp))
                Box(Modifier.fillMaxWidth().height(420.dp).padding(horizontal = 10.dp)) {
                    ShelterWorld(
                        selected = selected, builtWater = builtWater, builtFarm = builtFarm, builtMed = builtMed,
                        onSelect = { selected = it }
                    )
                }
                SelectedRoom(selected, builtWater, builtFarm, builtMed, message)
                ResourceStrip(power, water, oxygen, food)
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameButton("СМЕНА ДНЯ", Modifier.weight(1f), Amber) {
                        day++
                        power = (power - 8).coerceAtLeast(0)
                        oxygen = (oxygen - 6).coerceAtLeast(0)
                        water = (water - if (builtWater) 2 else 7).coerceAtLeast(0)
                        food = (food + (if (builtFarm) 6 else -8)).coerceIn(0, 100)
                        if (food == 0 && survivors > 1) survivors--
                        log("День $day. Выжившие: $survivors. Проверь системы и запасы.")
                    }
                    GameButton("РЕАКТОР +", Modifier.weight(1f), Neon) {
                        if (scrap >= 5) { scrap -= 5; power = (power + 20).coerceAtMost(100); log("Реактор обслужен. Энергия +20%.") }
                        else log("Не хватает металлолома для обслуживания.")
                    }
                }
            }
            "СТРОИТЬ" -> BuildScreen(
                scrap, builtWater, builtFarm, builtMed,
                build = { kind ->
                    when (kind) {
                        0 -> if (!builtWater && scrap >= 20) { scrap -= 20; builtWater = true; log("Фильтрационная станция построена. Расход воды снижен.") } else log("Не хватает металлолома или модуль уже построен.")
                        1 -> if (!builtFarm && scrap >= 30) { scrap -= 30; builtFarm = true; log("Гидропонная ферма запущена. Производство еды увеличено.") } else log("Не хватает металлолома или модуль уже построен.")
                        else -> if (!builtMed && scrap >= 35) { scrap -= 35; builtMed = true; log("Медблок готов. Медики смогут лечить раненых.") } else log("Не хватает металлолома или модуль уже построен.")
                    }
                    save()
                }
            )
            "ПОВЕРХНОСТЬ" -> SurfaceScreen(scrap, survivors, onExplore = {
                if (survivors > 1) {
                    survivors--
                    scrap += 18
                    food = (food + 12).coerceAtMost(100)
                    survivors++
                    log("Разведчики вернулись. Найдены детали (+18) и консервы (+12%).")
                } else log("Недостаточно людей для безопасной экспедиции.")
                save()
            })
            else -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text("ЖУРНАЛ УБЕЖИЩА", color = Neon, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Text("ДЕНЬ $day  •  $survivors ВЫЖИВШИХ", color = Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(message, color = TextWhite, fontSize = 14.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(18.dp))
                Text("СОСТОЯНИЕ СИСТЕМ", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("Энергия $power%   •   Вода $water%   •   Кислород $oxygen%   •   Провизия $food%", color = TextWhite, fontSize = 13.sp, lineHeight = 22.sp)
                Spacer(Modifier.height(16.dp))
                GameButton("СОХРАНИТЬ ИГРУ", Modifier.fillMaxWidth(), Neon) { save(); log("Прогресс сохранён на этом устройстве.") }
            }
        }
        BottomBar(tab) { tab = it }
    }
}

@Composable
private fun Header(day: Int, scrap: Int, survivors: Int) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("DIMLIFE // SURVIVAL SYSTEM", color = Neon, fontSize = 9.sp, fontWeight = FontWeight.Black)
            Text("LAST SHELTER", color = TextWhite, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text("ДЕНЬ $day  •  ЛЮДИ $survivors  •  МЕТАЛЛ $scrap", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("● LIVE", color = Neon, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text("SECTOR 01", color = Muted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ShelterWorld(selected: Int, builtWater: Boolean, builtFarm: Boolean, builtMed: Boolean, onSelect: (Int) -> Unit) {
    val motion = rememberInfiniteTransition(label = "shelter-motion")
    val walkPhase by motion.animateFloat(initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(2400)), label = "walk-phase")
    Canvas(
        Modifier.fillMaxSize().background(Color(0xFF080E0A), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF354A3A), RoundedCornerShape(12.dp))
            .pointerInput(selected, builtWater, builtFarm, builtMed) {
                detectTapGestures { p ->
                    val x = p.x / size.width
                    val y = p.y / size.height
                    val col = (x * 2).toInt().coerceIn(0, 1)
                    val row = (y * 3).toInt().coerceIn(0, 2)
                    onSelect(row * 2 + col)
                }
            }
    ) {
        val cw = size.width / 2f
        val ch = size.height / 3f
        drawRect(Color(0xFF0A110C), size = size)
        for (x in 0..12) drawLine(Color(0xFF14251A), Offset(size.width*x/12f,0f), Offset(size.width*x/12f,size.height), 1f)
        for (y in 0..16) drawLine(Color(0xFF14251A), Offset(0f,size.height*y/16f), Offset(size.width,size.height*y/16f), 1f)
        for (i in 0..5) {
            val col = i % 2
            val row = i / 2
            val left = col*cw + 5f
            val top = row*ch + 5f
            val w = cw - 10f
            val h = ch - 10f
            val isBuilt = i == 0 || i == 1 || (i == 3 && builtWater) || (i == 4 && builtFarm) || (i == 5 && builtMed)
            val floor = if (isBuilt) FloorLight else Color(0xFF111813)
            drawRoundRect(floor, Offset(left,top), Size(w,h), CornerRadius(8f))
            drawRoundRect(if (i == selected) Neon else if (isBuilt) Color(0xFF52765A) else Color(0xFF29382D),
                Offset(left,top), Size(w,h), CornerRadius(8f), style = Stroke(if (i == selected) 3f else 1.4f))
            // Layered industrial walls, metal floor plates, overhead conduit and ceiling lights.
            drawRoundRect(Color(0xFF26352B), Offset(left+4f,top+5f), Size(w-8f,h*.17f), CornerRadius(4f))
            for (j in 1..4) drawLine(Color(0xFF314238), Offset(left+8f,top+h*j/5f), Offset(left+w-8f,top+h*j/5f), 1f)
            drawLine(Color(0xFF63766A), Offset(left+7f,top+5f), Offset(left+w-7f,top+5f), 2f)
            drawLine(Color(0xFF516457), Offset(left+8f,top+h*.15f), Offset(left+w-8f,top+h*.15f), 2f)
            // Small ceiling lamps and wall-mounted service panels.
            for (lamp in 0..2) {
                val lx=left+w*(.22f+lamp*.28f)
                drawRoundRect(Color(0xFF9CB6A2), Offset(lx,top+8f), Size(w*.12f,3f), CornerRadius(2f))
                drawRoundRect(Neon.copy(alpha=.12f), Offset(lx-3f,top+5f), Size(w*.12f+6f,9f), CornerRadius(3f))
            }
            drawRoundRect(Color(0xFF0A120D), Offset(left+w*.04f,top+h*.22f), Size(w*.07f,h*.18f), CornerRadius(2f))
            drawRoundRect(Color(0xFF53685A), Offset(left+w*.05f,top+h*.24f), Size(w*.05f,h*.12f), CornerRadius(2f))
            // wall braces
            drawLine(Wall, Offset(left+4f,top+4f), Offset(left+w-4f,top+4f), 3f)
            when (i) {
                0 -> drawReactor(left, top, w, h)
                1 -> drawCommandDesk(left, top, w, h)
                2 -> drawBeds(left, top, w, h)
                3 -> if (builtWater) drawMachine(left, top, w, h, Blue) else drawCrates(left, top, w, h)
                4 -> if (builtFarm) drawFarm(left, top, w, h) else drawCrates(left, top, w, h)
                5 -> if (builtMed) drawMedical(left, top, w, h) else drawCrates(left, top, w, h)
            }
            drawDoor(left+w*.42f, top+h-2f, w*.16f, Neon.copy(alpha=.8f))
            val label = when (i) { 0 -> "РЕАКТОР"; 1 -> "УПРАВЛЕНИЕ"; 2 -> "ЖИЛОЙ БЛОК"; 3 -> "ВОДА"; 4 -> "ФЕРМА"; else -> "МЕДБЛОК" }
            drawContext.canvas.nativeCanvas.drawText(label, left+w/2, top+h-12f,
                android.graphics.Paint().apply { color = if (isBuilt) android.graphics.Color.rgb(130,255,150) else android.graphics.Color.rgb(132,151,137); textSize = min(11f, cw/13f); textAlign = android.graphics.Paint.Align.CENTER; typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD) })
        }
        // corridor and glowing connection nodes
        drawLine(Neon.copy(alpha=.35f), Offset(cw, ch*.5f), Offset(cw, ch*2.5f), 4f)
        drawLine(Neon.copy(alpha=.35f), Offset(cw*.5f, ch), Offset(cw*1.5f,ch), 4f)
        drawCircle(Neon, 4f, Offset(cw,ch))
        // Animated residents patrol connected rooms
        val patrol = (walkPhase - .5f) * cw * .30f
        drawPerson(cw*.48f + patrol, ch*.72f, Neon, 1.9f)
        drawPerson(cw*1.43f - patrol, ch*.70f, Blue, 1.85f)
        drawPerson(cw*.48f - patrol*.7f, ch*1.72f, Amber, 1.9f)
        drawPerson(cw*1.43f + patrol*.7f, ch*1.70f, Neon, 1.85f)
        drawPerson(cw*.48f + patrol*.6f, ch*2.72f, Blue, 1.9f)
        drawPerson(cw*1.43f - patrol*.6f, ch*2.70f, Amber, 1.85f)
        // overhead status lights
        drawCircle(Neon, 3f, Offset(size.width-15f, 14f))
        drawCircle(Amber, 3f, Offset(size.width-26f, 14f))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDoor(x: Float, y: Float, w: Float, color: Color) {
    drawRoundRect(Color(0xFF060907), Offset(x,y), Size(w,9f), CornerRadius(2f))
    drawLine(color, Offset(x+2f,y+2f), Offset(x+w-2f,y+2f), 2f)
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPerson(x: Float, y: Float, color: Color, scale: Float) {
    // Small but readable survivor sprite: shadow, boots, legs, jacket, arms, head, hair and backpack.
    drawOval(Color(0x99000000), Offset(x-8f*scale,y+10f*scale), Size(16f*scale,5f*scale))
    drawRoundRect(Color(0xFF111713), Offset(x-5f*scale,y+4f*scale), Size(4f*scale,9f*scale), CornerRadius(2f*scale))
    drawRoundRect(Color(0xFF111713), Offset(x+1f*scale,y+4f*scale), Size(4f*scale,9f*scale), CornerRadius(2f*scale))
    drawRoundRect(Color(0xFF59675B), Offset(x-6f*scale,y+10f*scale), Size(5f*scale,2.5f*scale), CornerRadius(1f*scale))
    drawRoundRect(Color(0xFF59675B), Offset(x+1f*scale,y+10f*scale), Size(5f*scale,2.5f*scale), CornerRadius(1f*scale))
    drawRoundRect(Color(0xFF29382E), Offset(x+3.5f*scale,y-2f*scale), Size(4f*scale,8f*scale), CornerRadius(1.5f*scale))
    drawRoundRect(color, Offset(x-5f*scale,y-4f*scale), Size(10f*scale,10f*scale), CornerRadius(2.5f*scale))
    drawLine(Color(0xFFD3E2D6), Offset(x,y-3f*scale), Offset(x,y+4f*scale), 1f*scale)
    drawLine(color.copy(alpha=.75f), Offset(x-4f*scale,y-1f*scale), Offset(x-8f*scale,y+4f*scale), 2.5f*scale, cap=StrokeCap.Round)
    drawLine(color.copy(alpha=.75f), Offset(x+4f*scale,y-1f*scale), Offset(x+7f*scale,y+3f*scale), 2.5f*scale, cap=StrokeCap.Round)
    drawCircle(Color(0xFFE2C7A5), 3.7f*scale, Offset(x,y-7f*scale))
    drawArc(Color(0xFF3B3026), 180f, 180f, false, Offset(x-3.8f*scale,y-10.7f*scale), Size(7.6f*scale,5f*scale), style=Stroke(2f*scale))
    drawCircle(Color(0xFF111713), 1f*scale, Offset(x+1.2f*scale,y-7f*scale))
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawReactor(x: Float,y: Float,w: Float,h: Float) {
    val cx=x+w/2; val cy=y+h*.43f; val r=min(w,h)*.23f
    // reinforced reactor bay with heavy containment frame
    drawRoundRect(Color(0xFF101912), Offset(x+w*.12f,y+h*.18f), Size(w*.76f,h*.54f), CornerRadius(9f))
    drawRoundRect(Color(0xFF34463A), Offset(x+w*.15f,y+h*.21f), Size(w*.70f,h*.48f), CornerRadius(7f), style=Stroke(4f))
    drawRoundRect(Color(0xFF080D09), Offset(cx-r*1.42f,cy-r*1.42f), Size(r*2.84f,r*2.84f), CornerRadius(r*.25f))
    drawCircle(Color(0xFF17291C),r*1.25f,Offset(cx,cy))
    drawCircle(Neon.copy(alpha=.12f),r*1.14f,Offset(cx,cy))
    drawCircle(Color(0xFF4B8759),r*.94f,Offset(cx,cy),style=Stroke(r*.13f))
    drawCircle(Neon.copy(alpha=.2f),r*.78f,Offset(cx,cy),style=Stroke(r*.18f))
    drawCircle(Neon,r*.57f,Offset(cx,cy),style=Stroke(r*.14f))
    drawCircle(Color(0xFFBFFFC9),r*.28f,Offset(cx,cy))
    for(a in 0..7){val an=a*Math.PI/4; val ax=kotlin.math.cos(an).toFloat(); val ay=kotlin.math.sin(an).toFloat()
        drawLine(Color(0xFFB5C6B8),Offset(cx+ax*r*1.08f,cy+ay*r*1.08f),Offset(cx+ax*r*1.42f,cy+ay*r*1.42f),3f)
        drawCircle(Amber.copy(alpha=.8f),2.5f,Offset(cx+ax*r*1.5f,cy+ay*r*1.5f))
    }
    for(i in 0..3){val py=y+h*(.26f+i*.105f);drawLine(Color(0xFF42584A),Offset(x+w*.08f,py),Offset(x+w*.24f,py),4f);drawLine(Color(0xFF42584A),Offset(x+w*.76f,py),Offset(x+w*.92f,py),4f)}
    drawRoundRect(Color(0xFF0A100C),Offset(x+w*.24f,y+h*.76f),Size(w*.52f,h*.10f),CornerRadius(4f))
    for(i in 0..5) drawRoundRect(if(i<4) Neon else Amber,Offset(x+w*(.29f+i*.07f),y+h*.79f),Size(w*.045f,h*.025f),CornerRadius(2f))
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCommandDesk(x: Float,y: Float,w: Float,h: Float) {
    val cx=x+w/2
    // command console, stacked displays, cable ducts and operator chair
    drawRoundRect(Color(0xFF0A100C),Offset(x+w*.12f,y+h*.18f),Size(w*.76f,h*.40f),CornerRadius(7f))
    drawRoundRect(Color(0xFF43594A),Offset(x+w*.16f,y+h*.21f),Size(w*.68f,h*.34f),CornerRadius(5f),style=Stroke(3f))
    drawRoundRect(Color(0xFF183A25),Offset(x+w*.20f,y+h*.24f),Size(w*.60f,h*.27f),CornerRadius(3f))
    for(i in 0..4){val yy=y+h*(.29f+i*.042f);drawLine(if(i==1) Blue else Neon.copy(alpha=.7f),Offset(x+w*.25f,yy),Offset(x+w*(.43f+(i%3)*.1f),yy),2f)}
    drawLine(Amber,Offset(x+w*.69f,y+h*.29f),Offset(x+w*.76f,y+h*.29f),3f)
    drawLine(Neon,Offset(x+w*.69f,y+h*.35f),Offset(x+w*.76f,y+h*.35f),3f)
    drawRoundRect(Color(0xFF34463A),Offset(x+w*.30f,y+h*.62f),Size(w*.40f,h*.09f),CornerRadius(3f))
    drawRoundRect(Color(0xFF202D24),Offset(cx-w*.13f,y+h*.71f),Size(w*.26f,h*.10f),CornerRadius(4f))
    drawLine(Color(0xFF65796A),Offset(cx,y+h*.70f),Offset(cx,y+h*.80f),3f)
    drawRoundRect(Color(0xFF0B120D),Offset(x+w*.09f,y+h*.82f),Size(w*.82f,h*.07f),CornerRadius(3f))
    drawPerson(cx,y+h*.78f,Amber,1.25f)
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBeds(x: Float,y: Float,w: Float,h: Float) {
    // Metal bunk frames, pillows, blankets, ladders and wall lamps.
    for (i in 0..1) {
        val bx=x+w*(.12f+i*.49f)
        drawRoundRect(Color(0xFF26372C), Offset(bx,y+h*.18f), Size(w*.34f,h*.49f), CornerRadius(4f))
        drawRoundRect(Color(0xFF71867A), Offset(bx+3f,y+h*.20f), Size(w*.28f,h*.18f), CornerRadius(3f))
        drawRoundRect(Color(0xFF416A4C), Offset(bx+3f,y+h*.40f), Size(w*.28f,h*.23f), CornerRadius(3f))
        drawLine(Color(0xFF9BB1A0), Offset(bx,y+h*.18f), Offset(bx,y+h*.68f), 3f)
        drawLine(Color(0xFF9BB1A0), Offset(bx+w*.34f,y+h*.18f), Offset(bx+w*.34f,y+h*.68f), 3f)
        drawCircle(Amber.copy(alpha=.8f), 3f, Offset(bx+w*.17f,y+h*.12f))
        for (r in 0..3) drawLine(Color(0xFFB4C8B7), Offset(bx+w*.37f,y+h*(.25f+r*.09f)), Offset(bx+w*.42f,y+h*(.25f+r*.09f)), 2f)
    }
    drawRoundRect(Color(0xFF28372C), Offset(x+w*.34f,y+h*.72f), Size(w*.32f,h*.07f), CornerRadius(3f))
    drawPerson(x+w*.52f,y+h*.82f,Blue,1.1f)
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMachine(x: Float,y: Float,w: Float,h: Float,c: Color) {
    // industrial water purifier with filter cylinders, pipes, pressure gauge and status screen
    drawRoundRect(Color(0xFF26372D),Offset(x+w*.17f,y+h*.20f),Size(w*.66f,h*.55f),CornerRadius(8f))
    drawRoundRect(Color(0xFF5D7563),Offset(x+w*.17f,y+h*.20f),Size(w*.66f,h*.55f),CornerRadius(8f),style=Stroke(3f))
    for(i in 0..2){val bx=x+w*(.25f+i*.19f)
        drawRoundRect(Color(0xFF101914),Offset(bx,y+h*.28f),Size(w*.13f,h*.32f),CornerRadius(5f))
        drawRoundRect(c.copy(alpha=.35f),Offset(bx+3f,y+h*.31f),Size(w*.13f-6f,h*.26f),CornerRadius(4f))
        drawLine(c,Offset(bx+w*.065f,y+h*.35f),Offset(bx+w*.065f,y+h*.54f),2f)
    }
    drawRoundRect(Color(0xFF0A110C),Offset(x+w*.31f,y+h*.62f),Size(w*.38f,h*.09f),CornerRadius(3f))
    drawCircle(c,min(w,h)*.035f,Offset(x+w*.5f,y+h*.665f))
    drawLine(Color(0xFF829487),Offset(x+w*.15f,y+h*.32f),Offset(x+w*.08f,y+h*.32f),4f)
    drawLine(Color(0xFF829487),Offset(x+w*.08f,y+h*.32f),Offset(x+w*.08f,y+h*.67f),4f)
    drawLine(Color(0xFF829487),Offset(x+w*.85f,y+h*.38f),Offset(x+w*.92f,y+h*.38f),4f)
    drawLine(Color(0xFF829487),Offset(x+w*.92f,y+h*.38f),Offset(x+w*.92f,y+h*.67f),4f)
    drawCircle(Amber,3f,Offset(x+w*.77f,y+h*.25f))
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCrates(x: Float,y: Float,w: Float,h: Float) {
    for (i in 0..2) {
        val bx=x+w*(.2f+(i%2)*.35f); val by=y+h*(.25f+(i/2)*.25f)
        drawRect(Color(0xFF344638), Offset(bx,by), Size(w*.28f,h*.2f))
        drawRect(Color(0xFF647A67), Offset(bx,by), Size(w*.28f,h*.2f), style=Stroke(2f))
        drawLine(Color(0xFF647A67), Offset(bx,by), Offset(bx+w*.28f,by+h*.2f), 1f)
    }
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFarm(x: Float,y: Float,w: Float,h: Float) {
    // hydroponics: three illuminated growing beds, leafy crops, nutrient tanks
    drawRoundRect(Color(0xFF0C1510),Offset(x+w*.09f,y+h*.19f),Size(w*.82f,h*.54f),CornerRadius(7f))
    for(i in 0..2){val yy=y+h*(.28f+i*.15f)
        drawRoundRect(Color(0xFF314B36),Offset(x+w*.17f,yy),Size(w*.66f,h*.10f),CornerRadius(4f))
        drawRoundRect(Color(0xFF0A100C),Offset(x+w*.19f,yy+h*.012f),Size(w*.62f,h*.025f),CornerRadius(2f))
        for(j in 0..5){val px=x+w*(.23f+j*.10f);val py=yy-h*.035f
            drawLine(Color(0xFF4C9C58),Offset(px,yy+h*.02f),Offset(px,py+h*.04f),2f)
            drawOval(if((j+i)%2==0) Color(0xFF77C96C) else Color(0xFF3F8C4D),Offset(px-w*.025f,py),Size(w*.06f,h*.055f))
            drawOval(Color(0xFF9AE98A),Offset(px,py+h*.01f),Size(w*.025f,h*.035f))
        }
        drawLine(Blue.copy(alpha=.7f),Offset(x+w*.18f,yy+h*.075f),Offset(x+w*.82f,yy+h*.075f),2f)
    }
    drawRoundRect(Color(0xFF2E4134),Offset(x+w*.25f,y+h*.76f),Size(w*.50f,h*.08f),CornerRadius(3f))
    drawCircle(Blue,min(w,h)*.035f,Offset(x+w*.38f,y+h*.80f))
    drawCircle(Neon,min(w,h)*.035f,Offset(x+w*.62f,y+h*.80f))
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMedical(x: Float,y: Float,w: Float,h: Float) {
    // clinic bed, monitor, IV stand, cabinet and medical cross
    drawRoundRect(Color(0xFF27392E),Offset(x+w*.12f,y+h*.26f),Size(w*.55f,h*.36f),CornerRadius(7f))
    drawRoundRect(Color(0xFF8EA99A),Offset(x+w*.15f,y+h*.28f),Size(w*.48f,h*.10f),CornerRadius(4f))
    drawRoundRect(Color(0xFF477A58),Offset(x+w*.15f,y+h*.39f),Size(w*.48f,h*.18f),CornerRadius(3f))
    drawRoundRect(Color(0xFF9AB2A0),Offset(x+w*.11f,y+h*.24f),Size(w*.58f,h*.06f),CornerRadius(3f))
    drawLine(Color(0xFF82998A),Offset(x+w*.18f,y+h*.62f),Offset(x+w*.18f,y+h*.71f),4f)
    drawLine(Color(0xFF82998A),Offset(x+w*.59f,y+h*.62f),Offset(x+w*.59f,y+h*.71f),4f)
    drawRoundRect(Color(0xFF0B130E),Offset(x+w*.68f,y+h*.22f),Size(w*.22f,h*.23f),CornerRadius(4f))
    drawRoundRect(Color(0xFF193D29),Offset(x+w*.71f,y+h*.25f),Size(w*.16f,h*.14f),CornerRadius(3f))
    drawLine(Neon,Offset(x+w*.73f,y+h*.32f),Offset(x+w*.78f,y+h*.32f),2f)
    drawLine(Neon,Offset(x+w*.78f,y+h*.32f),Offset(x+w*.81f,y+h*.28f),2f)
    drawLine(Neon,Offset(x+w*.81f,y+h*.28f),Offset(x+w*.85f,y+h*.35f),2f)
    drawLine(Color(0xFF9EB2A4),Offset(x+w*.78f,y+h*.45f),Offset(x+w*.78f,y+h*.70f),3f)
    drawLine(Color(0xFF9EB2A4),Offset(x+w*.69f,y+h*.70f),Offset(x+w*.87f,y+h*.70f),3f)
    drawLine(Blue,Offset(x+w*.78f,y+h*.45f),Offset(x+w*.78f,y+h*.51f),2f)
    drawRoundRect(Color(0xFF34493A),Offset(x+w*.24f,y+h*.76f),Size(w*.22f,h*.08f),CornerRadius(3f))
    drawLine(Red,Offset(x+w*.35f,y+h*.775f),Offset(x+w*.35f,y+h*.825f),3f)
    drawLine(Red,Offset(x+w*.32f,y+h*.80f),Offset(x+w*.38f,y+h*.80f),3f)
}