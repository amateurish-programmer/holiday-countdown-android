package com.holidaycountdown

import android.app.Application
import com.holidaycountdown.data.HolidayRepository
import com.holidaycountdown.data.local.HolidayDatabase
import com.holidaycountdown.data.settings.SettingsStore
import com.holidaycountdown.media.MusicController
import com.holidaycountdown.reminder.ReminderScheduler
import com.holidaycountdown.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HolidayCountdownApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lateinit var repository: HolidayRepository
        private set
    lateinit var settingsStore: SettingsStore
        private set
    lateinit var musicController: MusicController
        private set

    override fun onCreate() {
        super.onCreate()
        repository = HolidayRepository(this, HolidayDatabase.get(this).holidayDao())
        settingsStore = SettingsStore(this)
        musicController = MusicController(this)
        SyncScheduler.scheduleWeekly(this)
        appScope.launch {
            repository.ensureSeeded()
            val settings = settingsStore.settings.first()
            if (System.currentTimeMillis() - settings.lastSyncAttempt >= 24 * 60 * 60 * 1000L) {
                settingsStore.markSyncAttempt()
                repository.sync(settings.remoteUrl)
            }
            ReminderScheduler.reschedule(this@HolidayCountdownApp, repository, settings.remindersEnabled)
        }
    }
}
