package com.holidaycountdown.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.holidaycountdown.HolidayCountdownApp
import com.holidaycountdown.domain.CountdownCalculator
import com.holidaycountdown.domain.CountdownState
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class HolidayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as HolidayCountdownApp
        val calendar = app.repository.calendar.first()
        val state = CountdownCalculator().calculate(LocalDate.now(ZoneId.of("Asia/Shanghai")), calendar)
        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize().background(Color(0xFF10132E)).padding(16.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text("假期倒计时", style = TextStyle(color = ColorProvider(Color(0xFF7DEBFF)), fontSize = 13.sp))
                Spacer(GlanceModifier.height(8.dp))
                when (state) {
                    is CountdownState.BeforeHoliday -> {
                        Text(state.holiday.name, style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                        Text("${state.calendarDays}", style = TextStyle(color = ColorProvider(Color(0xFFFF62D0)), fontSize = 36.sp, fontWeight = FontWeight.Bold))
                        Text("天 · 还需上班 ${state.workdays} 天", style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 12.sp))
                    }
                    is CountdownState.DuringHoliday -> {
                        Text("${state.holiday.name}进行中", style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                        Text("剩 ${state.remainingDays} 天", style = TextStyle(color = ColorProvider(Color(0xFF4DFFB8)), fontSize = 28.sp, fontWeight = FontWeight.Bold))
                    }
                    is CountdownState.AwaitingSchedule -> Text("等待新年度安排", style = TextStyle(color = ColorProvider(Color.White), fontSize = 16.sp))
                }
            }
        }
    }
}

class HolidayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HolidayWidget()
}

suspend fun updateHolidayWidgets(context: Context) {
    HolidayWidget().updateAll(context)
}
