package com.holidaycountdown.data.remote

import org.junit.Assert.assertThrows
import org.junit.Test

class DatasetValidatorTest {
    @Test fun validDatasetPasses() { DatasetValidator.validate(dataset()) }

    @Test fun olderRevisionIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { DatasetValidator.validate(dataset(), 2) }
    }

    @Test fun overlappingHolidaysAreRejected() {
        val value = dataset().copy(years = listOf(dataset().years.first().copy(holidays = listOf(
            HolidayDto("a", "甲", "2027-01-01", "2027-01-03"),
            HolidayDto("b", "乙", "2027-01-03", "2027-01-04")
        ))))
        assertThrows(IllegalArgumentException::class.java) { DatasetValidator.validate(value) }
    }

    @Test fun workdayInsideHolidayIsRejected() {
        val value = dataset().copy(years = listOf(dataset().years.first().copy(specialWorkdays = listOf("2027-01-02"))))
        assertThrows(IllegalArgumentException::class.java) { DatasetValidator.validate(value) }
    }

    private fun dataset() = HolidayDatasetDto(
        1, 2, "2026-11-04T17:00:00+08:00",
        listOf(HolidayYearDto(
            2027, "https://www.gov.cn/",
            listOf(HolidayDto("new_year", "元旦", "2027-01-01", "2027-01-03")),
            listOf("2027-01-04")
        ))
    )
}
