package com.holidaycountdown.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.holidaycountdown.HolidayCountdownApp
import com.holidaycountdown.reminder.ReminderScheduler
import com.holidaycountdown.widget.updateHolidayWidgets
import kotlinx.coroutines.flow.first

class HolidaySyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as HolidayCountdownApp
        val url = inputData.getString("url") ?: app.settingsStore.settings.first().remoteUrl
        app.settingsStore.markSyncAttempt()
        return app.repository.sync(url).fold(
            onSuccess = {
                val enabled = app.settingsStore.settings.first().remindersEnabled
                ReminderScheduler.reschedule(applicationContext, app.repository, enabled)
                updateHolidayWidgets(applicationContext)
                Result.success()
            },
            onFailure = { if (runAttemptCount < 3) Result.retry() else Result.failure() }
        )
    }
}
