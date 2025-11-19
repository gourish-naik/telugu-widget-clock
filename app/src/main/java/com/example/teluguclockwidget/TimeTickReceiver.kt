package com.example.teluguclockwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import android.content.ComponentName

class TimeTickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIME_TICK) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TeluguClockWidget::class.java))
            for (id in ids) {
                TeluguClockWidget.updateWidget(context, manager, id)
            }
        }
    }
}
