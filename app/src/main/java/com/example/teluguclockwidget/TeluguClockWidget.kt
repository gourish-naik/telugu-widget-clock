package com.example.teluguclockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Calendar

class TeluguClockWidget : AppWidgetProvider() {

    companion object {
        fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val prefs = WidgetPrefs(context, appWidgetId)

            val cal = Calendar.getInstance()
            var hour = if (prefs.is12Hour) cal.get(Calendar.HOUR) else cal.get(Calendar.HOUR_OF_DAY)
            if (prefs.is12Hour && hour == 0) hour = 12
            val minute = cal.get(Calendar.MINUTE)

            val timeText = String.format("%02d:%02d", hour, minute)
            val teluguTime = TeluguStrings.toTelugu(timeText)

            val views = RemoteViews(context.packageName, R.layout.widget_clock)
            views.setTextViewText(R.id.time_text, teluguTime)
            views.setTextColor(R.id.time_text, prefs.fontColor)
            views.setTextViewTextSize(R.id.time_text, android.util.TypedValue.COMPLEX_UNIT_SP, prefs.fontSize.toFloat())

            if (prefs.showDate) {
                val day = TeluguStrings.toTelugu(cal.get(Calendar.DAY_OF_MONTH).toString())
                val month = TeluguStrings.teluguMonths[cal.get(Calendar.MONTH)]
                val year = TeluguStrings.toTelugu(cal.get(Calendar.YEAR).toString())
                val dateText = "$day $month $year"
                views.setTextViewText(R.id.date_text, dateText)
                views.setViewVisibility(R.id.date_text, android.view.View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.date_text, android.view.View.GONE)
            }

            val intent = Intent(context, TeluguClockWidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pi = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.clock_container, pi)

            manager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, manager, id)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        context.startService(Intent(context, ClockTickService::class.java))
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.stopService(Intent(context, ClockTickService::class.java))
    }
}