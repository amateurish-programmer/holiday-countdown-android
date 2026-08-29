package com.holidaycountdown.data.settings

data class AppSettings(
    val musicConsentAnswered: Boolean = false,
    val autoPlayMusic: Boolean = false,
    val musicEnabled: Boolean = true,
    val musicVolume: Float = 0.70f,
    val switchMusicByState: Boolean = true,
    val animationsEnabled: Boolean = true,
    val remindersEnabled: Boolean = true,
    val remoteUrl: String = "",
    val lastSyncAttempt: Long = 0L
)
