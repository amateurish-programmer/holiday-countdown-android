package com.holidaycountdown.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CountdownCalculatorTest {
    private val calculator = CountdownCalculator()
    private val spring = HolidayPeriod("spring", "春节", date("2026-02-15"), date("2026-02-23"), 2026, "official")
    private val labor = HolidayPeriod("labor", "劳动节", date("2026-05-01"), date("2026-05-05"), 2026, "official")
    private val national = HolidayPeriod("national", "国庆节", date("2026-10-01"), date("2026-10-07"), 2026, "official")
    private val calendar = HolidayCalendar(
        listOf(spring, labor, national),
        setOf(date("2026-02-14"), date("2026-02-28"), date("2026-05-09"), date("2026-09-20"), date("2026-10-10"))
    )

    @Test fun ordinaryWeekdayCountsCalendarAndWorkDays() {
        val state = calculator.calculate(date("2026-02-10"), calendar) as CountdownState.BeforeHoliday
        assertEquals(5, state.calendarDays)
        assertEquals(5, state.workdays)
        assertEquals(date("2026-01-01"), state.phaseStart)
        assertEquals(date("2026-02-15"), state.phaseEnd)
    }

    @Test fun specialSaturdayIsWorkday() {
        assertEquals(DayKind.WORKDAY, calculator.dayKind(date("2026-02-14"), calendar))
        val state = calculator.calculate(date("2026-02-14"), calendar) as CountdownState.BeforeHoliday
        assertEquals(1, state.workdays)
    }

    @Test fun ordinaryWeekendIsWeekend() {
        assertEquals(DayKind.WEEKEND, calculator.dayKind(date("2026-02-07"), calendar))
    }

    @Test fun firstHolidayDayIncludesEveryRemainingDay() {
        val state = calculator.calculate(date("2026-02-15"), calendar) as CountdownState.DuringHoliday
        assertEquals(9, state.remainingDays)
        assertEquals(0f, state.progress)
    }

    @Test fun lastHolidayDayHasOneDayRemaining() {
        val state = calculator.calculate(date("2026-02-23"), calendar) as CountdownState.DuringHoliday
        assertEquals(1, state.remainingDays)
        assertTrue(state.progress > .8f)
    }

    @Test fun dayAfterHolidayTargetsFollowingHoliday() {
        val state = calculator.calculate(date("2026-02-24"), calendar) as CountdownState.BeforeHoliday
        assertEquals("劳动节", state.holiday.name)
        assertEquals(date("2026-02-24"), state.phaseStart)
        assertEquals(date("2026-05-01"), state.phaseEnd)
    }

    @Test fun afterFinalHolidayWaitsForSchedule() {
        val state = calculator.calculate(date("2026-10-08"), calendar) as CountdownState.AwaitingSchedule
        assertEquals(2026, state.lastKnownYear)
    }

    @Test fun userOverrideWins() {
        val overridden = calendar.copy(overrides = mapOf(date("2026-02-14") to OverrideKind.HOLIDAY))
        assertEquals(DayKind.HOLIDAY, calculator.dayKind(date("2026-02-14"), overridden))
    }

    private fun date(value: String) = LocalDate.parse(value)
}
