package com.holidaycountdown.data

import android.content.Context
import com.holidaycountdown.data.local.CalendarOverrideEntity
import com.holidaycountdown.data.local.HolidayDao
import com.holidaycountdown.data.local.HolidayEntity
import com.holidaycountdown.data.local.SpecialWorkdayEntity
import com.holidaycountdown.data.remote.DatasetValidator
import com.holidaycountdown.data.remote.HolidayDatasetDto
import com.holidaycountdown.domain.HolidayCalendar
import com.holidaycountdown.domain.HolidayPeriod
import com.holidaycountdown.domain.OverrideKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class HolidayRepository(
    private val context: Context,
    private val dao: HolidayDao
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    val calendar: Flow<HolidayCalendar> = combine(
        dao.observeHolidays(), dao.observeWorkdays(), dao.observeOverrides()
    ) { holidays, workdays, overrides ->
        HolidayCalendar(
            holidays.map { it.toDomain() },
            workdays.map { LocalDate.parse(it.date) }.toSet(),
            overrides.associate { LocalDate.parse(it.date) to OverrideKind.valueOf(it.kind) }
        )
    }

    suspend fun ensureSeeded() {
        if (dao.holidayCount() == 0) restoreBuiltIn()
    }

    suspend fun restoreBuiltIn() {
        val raw = withContext(Dispatchers.IO) { context.assets.open("holidays_2026.json").bufferedReader().use { it.readText() } }
        replaceFromJson(raw, requireNewer = false, source = "应用内置 · 国务院办公厅")
    }

    suspend fun sync(url: String): Result<String> = runCatching {
        require(url.startsWith("https://")) { "更新地址必须使用 HTTPS" }
        val raw = withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            try {
                require(connection.responseCode in 200..299) { "服务器返回 ${connection.responseCode}" }
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally { connection.disconnect() }
        }
        replaceFromJson(raw, requireNewer = true, source = url)
        "节假日数据已更新"
    }

    suspend fun importJson(raw: String): Result<String> = runCatching {
        replaceFromJson(raw, requireNewer = false, source = "用户导入")
        "导入成功"
    }

    suspend fun exportJson(): String {
        val holidays = dao.holidays().groupBy { it.year }
        val workdays = dao.workdays().groupBy { it.year }
        val revision = dao.metadata("revision")?.value?.toLongOrNull() ?: 1L
        val dto = HolidayDatasetDto(
            1, revision, dao.metadata("publishedAt")?.value ?: "2025-11-04T17:00:00+08:00",
            holidays.map { (year, items) ->
                com.holidaycountdown.data.remote.HolidayYearDto(
                    year,
                    items.firstOrNull()?.sourceUrl.orEmpty(),
                    items.map { com.holidaycountdown.data.remote.HolidayDto(it.holidayId, it.name, it.startDate, it.endDate) },
                    workdays[year].orEmpty().map { it.date }
                )
            }
        )
        return json.encodeToString(dto)
    }

    suspend fun setOverride(date: LocalDate, kind: OverrideKind?) {
        if (kind == null) dao.deleteOverride(date.toString())
        else dao.insertOverride(CalendarOverrideEntity(date.toString(), kind.name))
    }

    suspend fun clearOverrides() = dao.clearOverrides()

    suspend fun metadata(): Map<String, String> = listOf("revision", "publishedAt", "source").associateWith { dao.metadata(it)?.value.orEmpty() }

    private suspend fun replaceFromJson(raw: String, requireNewer: Boolean, source: String) {
        val dto = json.decodeFromString<HolidayDatasetDto>(raw)
        val current = dao.metadata("revision")?.value?.toLongOrNull()
        DatasetValidator.validate(dto, if (requireNewer) current else null)
        val holidays = dto.years.flatMap { year -> year.holidays.map {
            HolidayEntity("${year.year}:${it.id}", it.id, it.name, it.startDate, it.endDate, year.year, year.sourceUrl)
        } }
        val workdays = dto.years.flatMap { year -> year.specialWorkdays.map { SpecialWorkdayEntity(it, year.year) } }
        dao.replaceBaseData(holidays, workdays, dto.revision, dto.publishedAt, source)
    }

    private fun HolidayEntity.toDomain() = HolidayPeriod(
        holidayId, name, LocalDate.parse(startDate), LocalDate.parse(endDate), year, sourceUrl
    )
}
