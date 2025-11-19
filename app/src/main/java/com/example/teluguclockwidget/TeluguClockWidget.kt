package com.example.teluguclockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TeluguClockWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_AUTO_UPDATE = "com.example.teluguclockwidget.ACTION_AUTO_UPDATE"

        fun updateWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val prefs = WidgetPrefs(context, appWidgetId)

            val cal = Calendar.getInstance()
            var hour = if (prefs.is12Hour) cal.get(Calendar.HOUR) else cal.get(Calendar.HOUR_OF_DAY)
            if (prefs.is12Hour && hour == 0) hour = 12
            val minute = cal.get(Calendar.MINUTE)

            val formatted = String.format("%02d:%02d", hour, minute)
            val telugu = ClockBitmapRenderer.toTelugu(formatted)

            val bmp = ClockBitmapRenderer.render(
                context,
                telugu,
                prefs.fontSize,
                prefs.fontColor,
                prefs.bold,
                prefs.shadow,
                prefs.fontFamily,
                prefs.showDate
            )

            val views = RemoteViews(context.packageName, R.layout.widget_clock)
            views.setImageViewBitmap(R.id.clock_bitmap, bmp)

            views.setViewVisibility(
                R.id.bg_overlay,
                if (prefs.overlay) android.view.View.VISIBLE else android.view.View.GONE
            )

            val intent = Intent(context, TeluguClockWidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pi = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.clock_bitmap, pi)

            manager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, manager, id)
        }
        // Schedule WorkManager task for periodic updates
        val workRequest = PeriodicWorkRequestBuilder<ClockUpdateWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "TeluguClockWidgetUpdate",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // WorkManager will handle periodic updates, no need for ACTION_AUTO_UPDATE here
    }
}