package com.holidaycountdown.data.remote

import java.time.LocalDate
import java.time.OffsetDateTime

object DatasetValidator {
    fun validate(dataset: HolidayDatasetDto, currentRevision: Long? = null) {
        require(dataset.schemaVersion == 1) { "不支持的数据格式版本" }
        require(dataset.revision > 0) { "revision 必须为正数" }
        require(currentRevision == null || dataset.revision > currentRevision) { "数据不是更新版本" }
        OffsetDateTime.parse(dataset.publishedAt)
        require(dataset.years.isNotEmpty()) { "年度数据不能为空" }
        require(dataset.years.map { it.year }.distinct().size == dataset.years.size) { "年度重复" }
        dataset.years.forEach { year ->
            require(year.year in 2020..2100) { "年份超出范围" }
            val periods = year.holidays.map { item ->
                val start = LocalDate.parse(item.startDate)
                val end = LocalDate.parse(item.endDate)
                require(item.id.isNotBlank() && item.name.isNotBlank()) { "节日名称或 ID 为空" }
                require(start.year == year.year && end.year == year.year) { "节日年份不一致" }
                require(!end.isBefore(start)) { "节日结束日期早于开始日期" }
                start..end
            }
            periods.sortedBy { it.start }.zipWithNext().forEach { (a, b) ->
                require(a.endInclusive.isBefore(b.start)) { "节假日日期重叠" }
            }
            val holidayDates = periods.flatMap { period -> generateSequence(period.start) { date -> date.plusDays(1).takeUnless { it.isAfter(period.endInclusive) } }.toList() }.toSet()
            val workdays = year.specialWorkdays.map(LocalDate::parse)
            require(workdays.all { it.year == year.year }) { "调休上班日年份不一致" }
            require(workdays.distinct().size == workdays.size) { "调休上班日重复" }
            require(workdays.none { it in holidayDates }) { "调休上班日与假期冲突" }
        }
    }
}
