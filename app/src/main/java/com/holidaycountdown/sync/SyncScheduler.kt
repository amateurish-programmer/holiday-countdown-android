package com.holidaycountdown.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private val networkConstraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun scheduleWeekly(context: Context) {
        val work = PeriodicWorkRequestBuilder<HolidaySyncWorker>(7, TimeUnit.DAYS)
            .setConstraints(networkConstraint).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("weekly_holiday_sync", ExistingPeriodicWorkPolicy.UPDATE, work)
    }

    fun syncNow(context: Context, url: String) = WorkManager.getInstance(context).enqueue(
        OneTimeWorkRequestBuilder<HolidaySyncWorker>()
            .setConstraints(networkConstraint)
            .setInputData(workDataOf("url" to url))
            .build()
    )
}
