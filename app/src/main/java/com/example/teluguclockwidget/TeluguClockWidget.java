package com.example.teluguclockwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;

import android.app.AlarmManager;
import android.content.ComponentName;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TeluguClockWidget extends AppWidgetProvider {

    private static final String ACTION_AUTO_UPDATE = "com.example.teluguclockwidget.ACTION_AUTO_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Update all widgets
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }

        // Schedule per-minute refresh
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TeluguClockWidget.class);
        intent.setAction(ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60_000,
                60_000,
                pendingIntent
        );
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_AUTO_UPDATE.equals(intent.getAction())) {

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName widget = new ComponentName(context, TeluguClockWidget.class);
            int[] ids = manager.getAppWidgetIds(widget);

            for (int id : ids) {
                updateWidget(context, manager, id);
            }
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {

        SharedPreferences prefs = context.getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);

        int fontSize = prefs.getInt("font_size", 48);
        int fontColor = prefs.getInt("font_color", Color.WHITE);
        boolean bold = prefs.getBoolean("is_font_bold", false);
        boolean shadow = prefs.getBoolean("show_text_shadow", true);
        boolean overlay = prefs.getBoolean("show_background_overlay", false);
        String fontFamily = prefs.getString("font_family", null);   // "noto_sans_telugu"
        boolean is12h = prefs.getBoolean("is_12_hour_format", true);

        // Get time
        Calendar calendar = Calendar.getInstance();
        int hour = is12h ? calendar.get(Calendar.HOUR) : calendar.get(Calendar.HOUR_OF_DAY);
        if (is12h && hour == 0) hour = 12;
        int minute = calendar.get(Calendar.MINUTE);

        String timeStr = (hour < 10 ? "0" : "") + hour
                + ":" + (minute < 10 ? "0" : "") + minute;

        // Convert to Telugu digits
        timeStr = ClockBitmapRenderer.convertToTelugu(timeStr);

        // Render BITMAP for clock text
        Bitmap bmp = ClockBitmapRenderer.renderClockBitmap(
                context,
                timeStr,
                fontSize,
                fontColor,
                bold,
                shadow,
                fontFamily
        );

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock);

        // Set bitmap
        views.setImageViewBitmap(R.id.clock_bitmap, bmp);

        // Background overlay
        if (overlay) {
            views.setViewVisibility(R.id.bg_overlay, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.bg_overlay, View.GONE);
        }

        // Tap -> open settings
        Intent configIntent = new Intent(context, TeluguClockWidgetConfigureActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                appWidgetId,
                configIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        views.setOnClickPendingIntent(R.id.clock_bitmap, pi);

        // Apply widget update
        manager.updateAppWidget(appWidgetId, views);
    }
}
