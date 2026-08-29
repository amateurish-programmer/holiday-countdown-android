package com.holidaycountdown.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.holidaycountdown.R
import com.holidaycountdown.domain.CountdownState
import com.holidaycountdown.domain.DayKind
import com.holidaycountdown.domain.HolidayPeriod
import com.holidaycountdown.domain.OverrideKind
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.sin

private val cyan = Color(0xFF65E9FF)
private val pink = Color(0xFFFF62D0)
private val green = Color(0xFF4DFFB8)
private val glass = Color(0xB3181A3C)

@Composable
fun HolidayApp(
    state: HolidayUiState,
    viewModel: HolidayViewModel,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onRequestNotifications: () -> Unit,
    onShare: (String) -> Unit
) {
    var selected by remember { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) { state.message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() } }
    if (!state.settings.musicConsentAnswered) MusicConsentDialog(viewModel)

    CyberBackground(animationsEnabled = state.settings.animationsEnabled) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar(containerColor = Color(0xE80A0B20), modifier = Modifier.navigationBarsPadding()) {
                    NavigationBarItem(selected == 0, { selected = 0 }, { Icon(Icons.Default.Home, null) }, label = { Text("倒计时") })
                    NavigationBarItem(selected == 1, { selected = 1 }, { Icon(Icons.Default.Settings, null) }, label = { Text("设置") })
                }
            }
        ) { padding ->
            AnimatedContent(selected, label = "page") { page ->
                if (page == 0) HomeScreen(state, viewModel, onShare, Modifier.padding(padding))
                else SettingsScreen(state, viewModel, onImport, onExport, onRequestNotifications, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun CyberBackground(animationsEnabled: Boolean, content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "neon")
    val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(8_000), RepeatMode.Reverse), label = "phase")
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF070818), Color(0xFF111037), Color(0xFF070818))))) {
        Canvas(Modifier.fillMaxSize()) {
            val p = if (animationsEnabled) phase else .4f
            drawCircle(Color(0x407B35FF), size.minDimension * .55f, Offset(size.width * (.15f + .1f * p), size.height * .2f))
            drawCircle(Color(0x2535E7FF), size.minDimension * .45f, Offset(size.width * .85f, size.height * (.58f + .08f * p)))
            repeat(76) { i ->
                val speed = 65f + (i % 7) * 13f
                val x = ((i * 83 + p * speed) % 100) / 100f * size.width
                val y = ((i * 47 + p * (95 + i % 5 * 16)) % 100) / 100f * size.height
                val color = when (i % 4) { 0 -> pink; 1 -> cyan; 2 -> green; else -> Color(0xFF9B7CFF) }
                drawCircle(color, 1.8f + (i % 4), Offset(x, y), alpha = .68f)
                if (i % 6 == 0) drawLine(color.copy(alpha = .25f), Offset(x - 14f, y + 10f), Offset(x, y), 1.5f)
            }
        }
        content()
    }
}

@Composable
private fun HomeScreen(state: HolidayUiState, vm: HolidayViewModel, onShare: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("假期倒计时", fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
                IconButton({ vm.setMusicEnabled(!state.settings.musicEnabled) }) {
                    Icon(if (state.settings.musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff, "音乐开关", tint = pink)
                }
            }
        }
        if (state.isSpecialWorkday) item {
            Text("⚠ 今天是调休上班日", color = Color(0xFFFFC857), fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0x30FFC857)).border(1.dp, Color(0x70FFC857), RoundedCornerShape(12.dp)).padding(12.dp))
        }
        item { CountdownHero(state, onShare) }
        item { StatusAnimation(state.todayKind, state.settings.animationsEnabled) }
        item { Text("2026 全年假期时间轴", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        items(state.calendar.holidays, key = { "${it.year}-${it.id}" }) { holiday -> TimelineItem(holiday) }
    }
}

@Composable
private fun CountdownHero(state: HolidayUiState, onShare: (String) -> Unit) {
    val countdown = state.countdown
    GlassCard {
        when (countdown) {
            is CountdownState.BeforeHoliday -> {
                Text("距离 ${countdown.holiday.name} 还有", color = Color.LightGray, fontSize = 17.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(countdown.calendarDays.toString(), color = pink, fontSize = 82.sp, lineHeight = 82.sp, fontWeight = FontWeight.Black)
                    Text(" 天", fontSize = 24.sp, modifier = Modifier.padding(bottom = 14.dp))
                }
                Text("还需上班 ${countdown.workdays} 天（含今天）", color = cyan, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                NeonProgress(
                    countdown.progress,
                    "统计区间：${countdown.phaseStart.format(DateTimeFormatter.ofPattern("MM月dd日"))}（上次假期结束后）→ ${countdown.phaseEnd.format(DateTimeFormatter.ofPattern("MM月dd日"))}"
                )
                Text("${countdown.holiday.startDate} — ${countdown.holiday.endDate}", color = Color.Gray, fontSize = 13.sp)
                ShareButton { onShare("距离${countdown.holiday.name}还有 ${countdown.calendarDays} 天，还需上班 ${countdown.workdays} 天！") }
            }
            is CountdownState.DuringHoliday -> {
                Text("${countdown.holiday.name}进行中", color = green, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("剩余 ${countdown.remainingDays} 天", color = green, fontSize = 54.sp, fontWeight = FontWeight.Black)
                Text(state.preciseRemaining, color = cyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp)); NeonProgress(countdown.progress, "本次假期进度")
                ShareButton { onShare("${countdown.holiday.name}进行中，假期还剩 ${countdown.remainingDays} 天！") }
            }
            is CountdownState.AwaitingSchedule -> {
                Text("等待新年度安排", color = pink, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text("下一年度法定节假日尚未发布或同步，请前往设置检查更新。", color = Color.LightGray)
            }
        }
    }
}

@Composable private fun NeonProgress(progress: Float, caption: String) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
        color = cyan,
        trackColor = Color(0x304A4E70)
    )
    Spacer(Modifier.height(8.dp))
    Text("$caption · 进度 ${(progress * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFFBFC5E8))
}

@Composable private fun ShareButton(onClick: () -> Unit) {
    TextButton(onClick, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Share, null); Spacer(Modifier.size(6.dp)); Text("分享此刻") }
}

@Composable private fun TimelineItem(holiday: HolidayPeriod) {
    val today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"))
    val status = when { today.isAfter(holiday.endDate) -> "已结束"; !today.isBefore(holiday.startDate) -> "进行中"; else -> "未开始" }
    val statusColor = when (status) { "进行中" -> green; "未开始" -> cyan; else -> Color.Gray }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(statusColor, CircleShape))
        Spacer(Modifier.size(12.dp))
        GlassCard(Modifier.weight(1f), innerPadding = 14.dp) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(holiday.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(status, color = statusColor, fontSize = 12.sp)
            }
            Text("${holiday.startDate.format(DateTimeFormatter.ofPattern("MM月dd日"))} — ${holiday.endDate.format(DateTimeFormatter.ofPattern("MM月dd日"))} · ${ChronoUnit.DAYS.between(holiday.startDate, holiday.endDate) + 1} 天", color = Color.LightGray, fontSize = 13.sp)
        }
    }
}

@Composable private fun StatusAnimation(dayKind: DayKind, enabled: Boolean) {
    val holiday = dayKind == DayKind.HOLIDAY
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (enabled) {
                if (holiday) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.holiday_flight))
                    val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
                    LottieAnimation(composition, { progress }, modifier = Modifier.size(128.dp))
                } else {
                    TetrisWorkAnimation(Modifier.size(142.dp))
                }
            } else {
                Text(if (holiday) "🎈" else "🧱", fontSize = 64.sp, modifier = Modifier.size(128.dp), textAlign = TextAlign.Center)
            }
            Column(Modifier.weight(1f)) {
                Text(if (holiday) "放飞自我模式" else "今日搬砖模式", color = if (holiday) green else pink, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text(if (holiday) "关掉闹钟，去拥抱世界。" else "再坚持一下，假期正在靠近。", color = Color.LightGray)
            }
        }
    }
}

@Composable
private fun TetrisWorkAnimation(modifier: Modifier = Modifier) {
    val engine = remember { com.holidaycountdown.domain.TetrisEngine() }
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(engine) {
        while (true) {
            delay(125)
            engine.tick()
            frame++
        }
    }
    val settled = remember(frame) { engine.settledCells() }
    val active = remember(frame) { engine.activeCells() }
    val clearing = remember(frame) { engine.clearingRows() }
    Canvas(modifier) {
        val cell = minOf(size.width / engine.columns, size.height / engine.rows)
        val left = (size.width - engine.columns * cell) / 2f
        val top = (size.height - engine.rows * cell) / 2f
        drawRoundRect(Color(0xF20A0D24), Offset(left - 5f, top - 5f), androidx.compose.ui.geometry.Size(engine.columns * cell + 10f, engine.rows * cell + 10f), CornerRadius(12f, 12f))
        repeat(engine.columns + 1) { c -> drawLine(Color(0x3565E9FF), Offset(left + c * cell, top), Offset(left + c * cell, top + engine.rows * cell), 1f) }
        repeat(engine.rows + 1) { r -> drawLine(Color(0x3565E9FF), Offset(left, top + r * cell), Offset(left + engine.columns * cell, top + r * cell), 1f) }
        settled.forEach { block ->
            val flash = block.y in clearing && frame % 2 == 0
            drawTetrisCell(left, top, cell, block.x, block.y.toFloat(), if (flash) Color.White else tetrisColor(block.color))
        }
        active.forEach { block -> drawTetrisCell(left, top, cell, block.x, block.y.toFloat(), tetrisColor(block.color)) }
    }
}

private fun tetrisColor(index: Int): Color = when (index) {
    1 -> Color(0xFFFFC857)
    2 -> cyan
    3 -> Color(0xFF9B7CFF)
    4 -> pink
    else -> green
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTetrisCell(left: Float, top: Float, cell: Float, x: Int, y: Float, color: Color) {
    val inset = 2.2f
    val offset = Offset(left + x * cell + inset, top + y * cell + inset)
    val blockSize = androidx.compose.ui.geometry.Size(cell - inset * 2f, cell - inset * 2f)
    drawRoundRect(color.copy(alpha = .28f), offset - Offset(3f, 3f), androidx.compose.ui.geometry.Size(blockSize.width + 6f, blockSize.height + 6f), CornerRadius(5f, 5f))
    drawRoundRect(color, offset, blockSize, CornerRadius(4f, 4f))
    drawLine(Color.White.copy(alpha = .60f), offset + Offset(3f, 3f), offset + Offset(blockSize.width - 3f, 3f), 1.4f)
}
@Composable private fun GlassCard(modifier: Modifier = Modifier, innerPadding: androidx.compose.ui.unit.Dp = 20.dp, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth().border(1.dp, Color(0x4065E9FF), RoundedCornerShape(22.dp)), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = glass, contentColor = Color(0xFFF7F5FF))) {
        Column(Modifier.padding(innerPadding), content = content)
    }
}

@Composable private fun MusicConsentDialog(vm: HolidayViewModel) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("开启背景音乐？") },
        text = { Text("同意后，应用位于前台时会自动循环播放本地氛围音乐；退到后台或锁屏会立即暂停。之后可随时在设置中关闭。") },
        confirmButton = { Button({ vm.respondMusicConsent(true) }) { Text("同意并播放") } },
        dismissButton = { TextButton({ vm.respondMusicConsent(false) }) { Text("暂不开启") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: HolidayUiState,
    vm: HolidayViewModel,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onRequestNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember(state.settings.remoteUrl) { mutableStateOf(state.settings.remoteUrl) }
    var overrideDate by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("设置中心", fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text("PREFERENCES // DATA", color = cyan, fontSize = 12.sp, letterSpacing = 2.sp)
        GlassCard {
            Text("声音与动效", fontSize = 19.sp, fontWeight = FontWeight.Bold)
            SettingSwitch("自动播放音乐", state.settings.autoPlayMusic, vm::setAutoPlay)
            SettingSwitch("音乐开关", state.settings.musicEnabled, vm::setMusicEnabled)
            Text("音量 ${(state.settings.musicVolume * 100).toInt()}%", color = Color.LightGray)
            Slider(state.settings.musicVolume, vm::setVolume)
            SettingSwitch("按上班/假期切换曲目", state.settings.switchMusicByState, vm::setSwitchTrack)
            SettingSwitch("动态粒子与 Lottie 动画", state.settings.animationsEnabled, vm::setAnimations)
        }
        GlassCard {
            Text("提醒", fontSize = 19.sp, fontWeight = FontWeight.Bold)
            SettingSwitch("假期开始与结束提醒", state.settings.remindersEnabled) { vm.setReminders(it); if (it) onRequestNotifications() }
            Text("开始前一天 18:00、结束当天 18:00 近似提醒", color = Color.Gray, fontSize = 12.sp)
        }
        GlassCard {
            Text("节假日数据", fontSize = 19.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(url, { url = it }, label = { Text("远程 HTTPS JSON 地址") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button({ vm.syncNow(url) }) { Text("立即更新") }
                TextButton({ vm.restoreBuiltIn() }) { Text("恢复内置") }
            }
            HorizontalDivider(color = Color(0x304A4E70))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onImport) { Text("导入 JSON") }
                TextButton(onExport) { Text("导出 JSON") }
            }
        }
        GlassCard {
            Text("手动日期覆盖", fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text("用于单位特殊安排；优先级高于远程和内置数据。", color = Color.Gray, fontSize = 12.sp)
            OutlinedTextField(overrideDate, { overrideDate = it }, label = { Text("日期 yyyy-MM-dd") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton({ vm.setOverride(overrideDate, OverrideKind.WORKDAY) }) { Text("设为上班") }
                TextButton({ vm.setOverride(overrideDate, OverrideKind.HOLIDAY) }) { Text("设为休息") }
                TextButton({ vm.setOverride(overrideDate, null) }) { Text("删除") }
            }
            TextButton({ vm.clearOverrides() }) { Text("清除全部手动覆盖") }
        }
        Text("假期倒计时 v1.0.0 · 中国大陆法定节假日", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f)); Switch(checked, onChange)
    }
}
