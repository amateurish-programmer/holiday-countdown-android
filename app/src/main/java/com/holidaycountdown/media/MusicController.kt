package com.holidaycountdown.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.holidaycountdown.R

@OptIn(UnstableApi::class)
class MusicController(context: Context) {
    private val player = ExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_ONE
        setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
            true
        )
        setHandleAudioBecomingNoisy(true)
    }
    private val workUri = "android.resource://${context.packageName}/${R.raw.work_ambient}"
    private val holidayUri = "android.resource://${context.packageName}/${R.raw.holiday_ambient}"
    private var currentHolidayMode: Boolean? = null

    fun configure(holidayMode: Boolean, volume: Float, switchByState: Boolean) {
        player.volume = (volume * 1.35f).coerceIn(0f, 1f)
        val wanted = if (switchByState) holidayMode else false
        if (currentHolidayMode != wanted) {
            val wasPlaying = player.isPlaying
            currentHolidayMode = wanted
            player.setMediaItem(MediaItem.fromUri(if (wanted) holidayUri else workUri))
            player.prepare()
            if (wasPlaying) player.play()
        }
    }

    fun play() = player.play()
    fun pause() = player.pause()
    fun release() = player.release()
    val isPlaying: Boolean get() = player.isPlaying
}
