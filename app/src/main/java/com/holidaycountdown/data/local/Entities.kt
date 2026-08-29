package com.holidaycountdown.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val key: String,
    val holidayId: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val year: Int,
    val sourceUrl: String
)

@Entity(tableName = "special_workdays")
data class SpecialWorkdayEntity(@PrimaryKey val date: String, val year: Int)

@Entity(tableName = "calendar_overrides")
data class CalendarOverrideEntity(@PrimaryKey val date: String, val kind: String)

@Entity(tableName = "metadata")
data class MetadataEntity(@PrimaryKey val key: String, val value: String)
