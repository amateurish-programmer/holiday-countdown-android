package com.holidaycountdown.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CountdownCalculator {
    fun calculate(today: LocalDate, calendar: HolidayCalendar): CountdownState {
        val holidays = calendar.holidays.sortedBy { it.startDate }
        holidays.firstOrNull { !today.isBefore(it.startDate) && !today.isAfter(it.endDate) }?.let { current ->
            val total = ChronoUnit.DAYS.between(current.startDate, current.endDate.plusDays(1)).coerceAtLeast(1)
            val elapsed = ChronoUnit.DAYS.between(current.startDate, today).coerceAtLeast(0)
            return CountdownState.DuringHoliday(
                current,
                ChronoUnit.DAYS.between(today, current.endDate) + 1,
                (elapsed.toFloat() / total).coerceIn(0f, 1f)
            )
        }
        val next = holidays.firstOrNull { it.startDate.isAfter(today) }
            ?: return CountdownState.AwaitingSchedule(holidays.maxOfOrNull { it.year })
        val previousEnd = holidays.lastOrNull { it.endDate.isBefore(today) }?.endDate
            ?: LocalDate.of(today.year, 1, 1).minusDays(1)
        val phaseStart = previousEnd.plusDays(1)
        val phaseLength = ChronoUnit.DAYS.between(phaseStart, next.startDate).coerceAtLeast(1)
        val elapsed = ChronoUnit.DAYS.between(phaseStart, today).coerceAtLeast(0)
        return CountdownState.BeforeHoliday(
            next,
            ChronoUnit.DAYS.between(today, next.startDate),
            countWorkdays(today, next.startDate, calendar),
            (elapsed.toFloat() / phaseLength).coerceIn(0f, 1f),
            phaseStart,
            next.startDate
        )
    }

    fun dayKind(date: LocalDate, calendar: HolidayCalendar): DayKind {
        calendar.overrides[date]?.let { return if (it == OverrideKind.WORKDAY) DayKind.WORKDAY else DayKind.HOLIDAY }
        if (calendar.specialWorkdays.contains(date)) return DayKind.WORKDAY
        if (calendar.holidays.any { !date.isBefore(it.startDate) && !date.isAfter(it.endDate) }) return DayKind.HOLIDAY
        return if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) DayKind.WEEKEND else DayKind.WORKDAY
    }

    fun countWorkdays(fromInclusive: LocalDate, toExclusive: LocalDate, calendar: HolidayCalendar): Int {
        if (!fromInclusive.isBefore(toExclusive)) return 0
        var count = 0
        var date = fromInclusive
        while (date.isBefore(toExclusive)) {
            if (dayKind(date, calendar) == DayKind.WORKDAY) count++
            date = date.plusDays(1)
        }
        return count
    }
}
