package com.holidaycountdown.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class HolidayDatasetDto(
    val schemaVersion: Int,
    val revision: Long,
    val publishedAt: String,
    val years: List<HolidayYearDto>
)

@Serializable
data class HolidayYearDto(
    val year: Int,
    val sourceUrl: String,
    val holidays: List<HolidayDto>,
    val specialWorkdays: List<String>
)

@Serializable
data class HolidayDto(val id: String, val name: String, val startDate: String, val endDate: String)
