package com.holidaycountdown.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.holidaycountdown.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("app_settings")

class SettingsStore(private val context: Context) {
    private object Keys {
        val consent = booleanPreferencesKey("music_consent_answered")
        val autoPlay = booleanPreferencesKey("auto_play_music")
        val musicEnabled = booleanPreferencesKey("music_enabled")
        val volume = floatPreferencesKey("music_volume")
        val switchTrack = booleanPreferencesKey("switch_music_by_state")
        val animation = booleanPreferencesKey("animations_enabled")
        val reminders = booleanPreferencesKey("reminders_enabled")
        val remoteUrl = stringPreferencesKey("remote_url")
        val lastSync = longPreferencesKey("last_sync_attempt")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            musicConsentAnswered = p[Keys.consent] ?: false,
            autoPlayMusic = p[Keys.autoPlay] ?: false,
            musicEnabled = p[Keys.musicEnabled] ?: true,
            musicVolume = p[Keys.volume] ?: 0.70f,
            switchMusicByState = p[Keys.switchTrack] ?: true,
            animationsEnabled = p[Keys.animation] ?: true,
            remindersEnabled = p[Keys.reminders] ?: true,
            remoteUrl = p[Keys.remoteUrl] ?: BuildConfig.HOLIDAY_DATA_URL,
            lastSyncAttempt = p[Keys.lastSync] ?: 0L
        )
    }

    suspend fun setMusicConsent(accepted: Boolean) = context.dataStore.edit {
        it[Keys.consent] = true
        it[Keys.autoPlay] = accepted
        it[Keys.musicEnabled] = accepted
    }
    suspend fun setAutoPlay(value: Boolean) = context.dataStore.edit { it[Keys.autoPlay] = value }
    suspend fun setMusicEnabled(value: Boolean) = context.dataStore.edit { it[Keys.musicEnabled] = value }
    suspend fun setVolume(value: Float) = context.dataStore.edit { it[Keys.volume] = value.coerceIn(0f, 1f) }
    suspend fun setSwitchTrack(value: Boolean) = context.dataStore.edit { it[Keys.switchTrack] = value }
    suspend fun setAnimations(value: Boolean) = context.dataStore.edit { it[Keys.animation] = value }
    suspend fun setReminders(value: Boolean) = context.dataStore.edit { it[Keys.reminders] = value }
    suspend fun setRemoteUrl(value: String) = context.dataStore.edit { it[Keys.remoteUrl] = value.trim() }
    suspend fun markSyncAttempt(time: Long = System.currentTimeMillis()) = context.dataStore.edit { it[Keys.lastSync] = time }
}
