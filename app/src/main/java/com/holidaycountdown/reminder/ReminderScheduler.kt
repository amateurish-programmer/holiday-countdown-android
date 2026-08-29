package com.holidaycountdown.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.holidaycountdown.data.HolidayRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private val zone = ZoneId.of("Asia/Shanghai")

    suspend fun reschedule(context: Context, repository: HolidayRepository, enabled: Boolean) {
        val manager = WorkManager.getInstance(context)
        manager.cancelAllWorkByTag("holiday_reminder")
        if (!enabled) return
        val now = LocalDateTime.now(zone)
        repository.calendar.first().holidays.forEach { holiday ->
            listOf(
                Triple("start", holiday.startDate.minusDays(1).atTime(18, 0), "明天就是${holiday.name}，准备放飞吧！"),
                Triple("end", holiday.endDate.atTime(18, 0), "${holiday.name}即将结束，今晚记得收收心。")
            ).filter { it.second.isAfter(now) }.forEach { (kind, target, message) ->
                val delay = Duration.between(now, target).toMillis()
                val work = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf("title" to holiday.name, "message" to message, "id" to "${holiday.year}_${holiday.id}_$kind"))
                    .addTag("holiday_reminder").build()
                manager.enqueueUniqueWork("reminder_${holiday.year}_${holiday.id}_$kind", ExistingWorkPolicy.REPLACE, work)
            }
        }
    }
}
