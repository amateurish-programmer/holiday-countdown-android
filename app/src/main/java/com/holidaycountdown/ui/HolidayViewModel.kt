package com.holidaycountdown.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.holidaycountdown.HolidayCountdownApp
import com.holidaycountdown.data.settings.AppSettings
import com.holidaycountdown.domain.CountdownCalculator
import com.holidaycountdown.domain.CountdownState
import com.holidaycountdown.domain.DayKind
import com.holidaycountdown.domain.HolidayCalendar
import com.holidaycountdown.domain.OverrideKind
import com.holidaycountdown.reminder.ReminderScheduler
import com.holidaycountdown.sync.SyncScheduler
import com.holidaycountdown.widget.updateHolidayWidgets
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.ZoneId

data class HolidayUiState(
    val loading: Boolean = true,
    val calendar: HolidayCalendar = HolidayCalendar(emptyList(), emptySet()),
    val countdown: CountdownState = CountdownState.AwaitingSchedule(null),
    val todayKind: DayKind = DayKind.WORKDAY,
    val isSpecialWorkday: Boolean = false,
    val preciseRemaining: String = "00:00:00",
    val settings: AppSettings = AppSettings(),
    val message: String? = null
)

class HolidayViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as HolidayCountdownApp
    private val calculator = CountdownCalculator()
    private val zone = ZoneId.of("Asia/Shanghai")
    private val ticker = MutableStateFlow(System.currentTimeMillis())
    private val message = MutableStateFlow<String?>(null)

    val uiState = combine(app.repository.calendar, app.settingsStore.settings, ticker, message) { calendar, settings, _, msg ->
        val today = LocalDate.now(zone)
        val state = calculator.calculate(today, calendar)
        HolidayUiState(
            loading = calendar.holidays.isEmpty(),
            calendar = calendar,
            countdown = state,
            todayKind = calculator.dayKind(today, calendar),
            isSpecialWorkday = today in calendar.specialWorkdays || calendar.overrides[today] == OverrideKind.WORKDAY,
            preciseRemaining = preciseRemaining(state),
            settings = settings,
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HolidayUiState())

    init {
        viewModelScope.launch { while (true) { ticker.value = System.currentTimeMillis(); delay(1_000) } }
    }

    fun respondMusicConsent(accepted: Boolean) = launch { app.settingsStore.setMusicConsent(accepted) }
    fun setAutoPlay(value: Boolean) = launch { app.settingsStore.setAutoPlay(value) }
    fun setMusicEnabled(value: Boolean) = launch { app.settingsStore.setMusicEnabled(value) }
    fun setVolume(value: Float) = launch { app.settingsStore.setVolume(value) }
    fun setSwitchTrack(value: Boolean) = launch { app.settingsStore.setSwitchTrack(value) }
    fun setAnimations(value: Boolean) = launch { app.settingsStore.setAnimations(value) }
    fun setRemoteUrl(value: String) = launch { app.settingsStore.setRemoteUrl(value) }
    fun setReminders(value: Boolean) = launch {
        app.settingsStore.setReminders(value)
        ReminderScheduler.reschedule(app, app.repository, value)
    }
    fun syncNow(url: String = uiState.value.settings.remoteUrl) {
        launch { app.settingsStore.setRemoteUrl(url) }
        SyncScheduler.syncNow(app, url)
        showMessage("已提交更新任务")
    }
    fun restoreBuiltIn() = launch {
        runCatching { app.repository.restoreBuiltIn() }.fold(
            { afterDataChange("已恢复内置 2026 数据") }, { showMessage(it.message ?: "恢复失败") }
        )
    }
    fun clearOverrides() = launch { app.repository.clearOverrides(); afterDataChange("已清除手动覆盖") }
    fun setOverride(dateText: String, kind: OverrideKind?) = launch {
        runCatching { app.repository.setOverride(LocalDate.parse(dateText), kind) }.fold(
            { afterDataChange("日期覆盖已保存") }, { showMessage("请输入 yyyy-MM-dd 格式的有效日期") }
        )
    }
    fun importFrom(uri: Uri) = launch {
        runCatching { app.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() } }
            .mapCatching { app.repository.importJson(it).getOrThrow() }
            .fold({ afterDataChange(it) }, { showMessage(it.message ?: "导入失败") })
    }
    fun exportTo(uri: Uri) = launch {
        runCatching { app.contentResolver.openOutputStream(uri)!!.bufferedWriter().use { it.write(app.repository.exportJson()) } }
            .fold({ showMessage("导出成功") }, { showMessage(it.message ?: "导出失败") })
    }
    fun clearMessage() { message.value = null }

    private fun preciseRemaining(state: CountdownState): String {
        if (state !is CountdownState.DuringHoliday) return ""
        val end = state.holiday.endDate.plusDays(1).atStartOfDay(zone)
        val duration = Duration.between(ZonedDateTime.now(zone), end).coerceAtLeast(Duration.ZERO)
        return "%02d:%02d:%02d".format(duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart())
    }
    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
    private suspend fun afterDataChange(text: String) {
        ReminderScheduler.reschedule(app, app.repository, uiState.value.settings.remindersEnabled)
        updateHolidayWidgets(app)
        showMessage(text)
    }
    private fun showMessage(text: String) { message.value = text }
}
