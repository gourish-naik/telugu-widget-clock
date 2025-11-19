import android.graphics.Color
import android.graphics.PorterDuff
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Calendar
import com.example.teluguclockwidget.WidgetPrefs
import com.example.teluguclockwidget.ClockBitmapRenderer
import com.example.teluguclockwidget.R
import com.example.teluguclockwidget.TeluguClockWidgetConfigureActivity

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
                prefs.showDate,
                prefs.dateFormat,
                prefs.overlay, // Pass overlay preference
                prefs.overlayTheme // Pass overlay theme
            )

            val views = RemoteViews(context.packageName, R.layout.widget_clock)
            views.setImageViewBitmap(R.id.clock_bitmap, bmp)

            // Set visibility of bg_overlay based on prefs.overlay
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

        private fun scheduleNextUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) return

            val intent = Intent(context, TeluguClockWidget::class.java).apply {
                action = ACTION_AUTO_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                0, 
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate EXACT next minute boundary (no delay)
            val now = System.currentTimeMillis()
            val nextMinuteMillis = now - (now % 60000) + 60000

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextMinuteMillis,
                pendingIntent
            )
        }

        fun cancelUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, TeluguClockWidget::class.java).apply {
                action = ACTION_AUTO_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                0, 
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.cancel(pendingIntent)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, manager, id)
        }
        scheduleNextUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_AUTO_UPDATE -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, TeluguClockWidget::class.java))
                
                if (ids.isNotEmpty()) {
                    for (id in ids) {
                        updateWidget(context, manager, id)
                    }
                    scheduleNextUpdate(context)
                }
            }
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_BOOT_COMPLETED -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, TeluguClockWidget::class.java))
                for (id in ids) {
                    updateWidget(context, manager, id)
                }
                scheduleNextUpdate(context)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdates(context)
    }
}