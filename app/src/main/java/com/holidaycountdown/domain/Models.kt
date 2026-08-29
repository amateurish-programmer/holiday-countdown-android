package com.holidaycountdown.domain

import java.time.LocalDate

data class HolidayPeriod(
    val id: String,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val year: Int,
    val sourceUrl: String
)

enum class DayKind { WORKDAY, WEEKEND, HOLIDAY }
enum class OverrideKind { WORKDAY, HOLIDAY }

data class CalendarOverride(val date: LocalDate, val kind: OverrideKind)

sealed interface CountdownState {
    data class BeforeHoliday(
        val holiday: HolidayPeriod,
        val calendarDays: Long,
        val workdays: Int,
        val progress: Float,
        val phaseStart: LocalDate,
        val phaseEnd: LocalDate
    ) : CountdownState

    data class DuringHoliday(
        val holiday: HolidayPeriod,
        val remainingDays: Long,
        val progress: Float
    ) : CountdownState

    data class AwaitingSchedule(val lastKnownYear: Int?) : CountdownState
}

data class HolidayCalendar(
    val holidays: List<HolidayPeriod>,
    val specialWorkdays: Set<LocalDate>,
    val overrides: Map<LocalDate, OverrideKind> = emptyMap()
)
