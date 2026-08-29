package com.holidaycountdown

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.holidaycountdown.domain.CountdownState
import com.holidaycountdown.ui.HolidayApp
import com.holidaycountdown.ui.HolidayViewModel
import com.holidaycountdown.ui.theme.HolidayCountdownTheme

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HolidayCountdownTheme {
                val vm: HolidayViewModel = viewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                val app = application as HolidayCountdownApp
                val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(vm::importFrom) }
                val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let(vm::exportTo) }
                val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

                LaunchedEffect(state.settings, state.countdown) {
                    val holidayMode = state.countdown is CountdownState.DuringHoliday
                    app.musicController.configure(holidayMode, state.settings.musicVolume, state.settings.switchMusicByState)
                    if (state.settings.autoPlayMusic && state.settings.musicEnabled) app.musicController.play() else app.musicController.pause()
                }
                DisposableEffect(Unit) { onDispose { app.musicController.pause() } }

                HolidayApp(
                    state = state,
                    viewModel = vm,
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                    onExport = { exportLauncher.launch("holiday-countdown.json") },
                    onRequestNotifications = {
                        if (android.os.Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onShare = { text ->
                        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
                        }, "分享倒计时"))
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (application as HolidayCountdownApp).musicController.pause()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val app = application as HolidayCountdownApp
            val settings = app.settingsStore.settings.first()
            if (settings.autoPlayMusic && settings.musicEnabled) app.musicController.play()
        }
    }
}
