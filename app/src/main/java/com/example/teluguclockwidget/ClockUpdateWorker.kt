package com.example.teluguclockwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ClockUpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(applicationContext, TeluguClockWidget::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            TeluguClockWidget.updateWidget(applicationContext, appWidgetManager, appWidgetId)
        }
        return Result.success()
    }
}
