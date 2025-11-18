package com.example.teluguclockwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;

import android.app.AlarmManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TeluguClockWidget extends AppWidgetProvider {

    private static final String ACTION_AUTO_UPDATE = "com.example.teluguclockwidget.ACTION_AUTO_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

        // Schedule AlarmManager for periodic updates
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TeluguClockWidget.class);
        intent.setAction(ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Update every minute
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1), pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_AUTO_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, TeluguClockWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        // Cancel AlarmManager when all widgets are deleted
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TeluguClockWidget.class);
        intent.setAction(ACTION_AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
        Bundle options = new Bundle();
        options.putInt("font_size", prefs.getInt("font_size", 48));
        options.putInt("font_color", prefs.getInt("font_color", Color.WHITE));
        options.putString("font_family", prefs.getString("font_family", null));
        options.putInt("horizontal_gravity", prefs.getInt("horizontal_gravity", Gravity.CENTER_HORIZONTAL));
        options.putInt("vertical_gravity", prefs.getInt("vertical_gravity", Gravity.CENTER_VERTICAL));
        options.putBoolean("is_12_hour_format", prefs.getBoolean("is_12_hour_format", true));
        options.putBoolean("show_am_pm_icon", prefs.getBoolean("show_am_pm_icon", false));
        options.putBoolean("is_font_bold", prefs.getBoolean("is_font_bold", false));
        options.putBoolean("show_background_overlay", prefs.getBoolean("show_background_overlay", false));
        options.putInt("background_overlay_color", prefs.getInt("background_overlay_color", Color.TRANSPARENT));
        options.putBoolean("show_date", prefs.getBoolean("show_date", false));

        updateAppWidgetWithPrefs(context, appWidgetManager, appWidgetId, options);
    }

    static void updateAppWidgetWithPrefs(Context context, AppWidgetManager appWidgetManager,
                                         int appWidgetId, Bundle options) {

        int fontSize = options.getInt("font_size", 48);
        int fontColor = options.getInt("font_color", Color.WHITE);
        String fontFamily = options.getString("font_family", null);
        int horizontal_gravity = options.getInt("horizontal_gravity", Gravity.CENTER_HORIZONTAL);
        int vertical_gravity = options.getInt("vertical_gravity", Gravity.CENTER_VERTICAL);
        boolean is12HourFormat = options.getBoolean("is_12_hour_format", true);
        boolean showAmPmIcon = options.getBoolean("show_am_pm_icon", false);
        boolean isFontBold = options.getBoolean("is_font_bold", false);
        boolean showBackgroundOverlay = options.getBoolean("show_background_overlay", false);
        int backgroundOverlayColor = options.getInt("background_overlay_color", Color.TRANSPARENT);
        boolean showDate = options.getBoolean("show_date", false);
        boolean showTextShadow = options.getBoolean("show_text_shadow", false);

        Log.d("WidgetUpdate", "Updating widget " + appWidgetId + " with: " +
                "fontSize=" + fontSize +
                ", fontColor=" + String.format("#%08X", fontColor) +
                ", horizontalGravity=" + horizontal_gravity +
                ", verticalGravity=" + vertical_gravity +
                ", is12HourFormat=" + is12HourFormat +
                ", showAmPmIcon=" + showAmPmIcon +
                ", isFontBold=" + isFontBold +
                ", showBackgroundOverlay=" + showBackgroundOverlay +
                ", backgroundOverlayColor=" + String.format("#%08X", backgroundOverlayColor) +
                ", showDate=" + showDate +
                ", showTextShadow=" + showTextShadow);


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock);

        // Get current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Convert to Telugu numerals
        String teluguTime = toTeluguNumerals(hour) + ":" + toTeluguNumerals(minute);

        views.setTextViewText(R.id.clock_text, teluguTime);
        views.setTextViewTextSize(R.id.clock_text, 0, fontSize);
        views.setTextColor(R.id.clock_text, fontColor);
        // if (isFontBold) {
        //     views.setInt(R.id.clock_text, "setTypeface", 1); // 1 for BOLD
        // } else {
        //     views.setInt(R.id.clock_text, "setTypeface", 0); // 0 for NORMAL
        // }

        // if (showBackgroundOverlay) {
        //     views.setInt(R.id.clock_text, "setBackgroundColor", backgroundOverlayColor);
        // } else {
        //     views.setInt(R.id.clock_text, "setBackgroundColor", Color.TRANSPARENT);
        // }

        // if (showDate) {
        //     int day = calendar.get(Calendar.DAY_OF_MONTH);
        //     int month = calendar.get(Calendar.MONTH);
        //     int year = calendar.get(Calendar.YEAR);

        //     String teluguDate = toTeluguNumerals(day) + " " + toTeluguMonthName(month) + " " + toTeluguNumerals(year);
        //     views.setTextViewText(R.id.date_text, teluguDate);
        //     views.setViewVisibility(R.id.date_text, View.VISIBLE);
        // } else {
        //     views.setViewVisibility(R.id.date_text, View.GONE);
        // }

        // if (is12HourFormat) {
        //     int displayHour = calendar.get(Calendar.HOUR); // 12-hour format (0-11)
        //     if (displayHour == 0) {
        //         displayHour = 12; // Convert 0 to 12 for 12-hour format
        //     }
        //     teluguTime = toTeluguNumerals(displayHour) + ":" + toTeluguNumerals(minute);

        //     if (showAmPmIcon) {
        //         views.setViewVisibility(R.id.am_pm_icon, View.VISIBLE);
        //         if (calendar.get(Calendar.AM_PM) == Calendar.PM) {
        //             views.setImageViewResource(R.id.am_pm_icon, R.drawable.ic_pm);
        //         } else {
        //             views.setImageViewResource(R.id.am_pm_icon, R.drawable.ic_am);
        //         }
        //     } else {
        //         views.setViewVisibility(R.id.am_pm_icon, View.GONE);
        //     }
        // } else {
        //     teluguTime = toTeluguNumerals(hour) + ":" + toTeluguNumerals(minute); // 24-hour format
        //     views.setViewVisibility(R.id.am_pm_icon, View.GONE);
        // }

        // if (showTextShadow) {
        //     views.setTextViewText(R.id.clock_text_shadow, teluguTime);
        //     views.setTextViewTextSize(R.id.clock_text_shadow, 0, fontSize);
        //     views.setViewVisibility(R.id.clock_text_shadow, View.VISIBLE);
        // } else {
        //     views.setViewVisibility(R.id.clock_text_shadow, View.GONE);
        // }

        if (fontFamily != null) {
            // This requires a custom solution for applying fonts to RemoteViews,
            // as RemoteViews does not directly support setting Typeface.
            // For simplicity, this is omitted here.
        }

        views.setInt(R.id.clock_text, "setGravity", vertical_gravity | horizontal_gravity);


        // Create an Intent to launch the configuration activity
        Intent intent = new Intent(context, TeluguClockWidgetConfigureActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.clock_text, pendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static String toTeluguNumerals(int number) {
        String normalNumber = String.valueOf(number);
        StringBuilder teluguNumber = new StringBuilder();
        for (int i = 0; i < normalNumber.length(); i++) {
            char c = normalNumber.charAt(i);
            switch (c) {
                case '0':
                    teluguNumber.append('౦');
                    break;
                case '1':
                    teluguNumber.append('౧');
                    break;
                case '2':
                    teluguNumber.append('౨');
                    break;
                case '3':
                    teluguNumber.append('౩');
                    break;
                case '4':
                    teluguNumber.append('౪');
                    break;
                case '5':
                    teluguNumber.append('౫');
                    break;
                case '6':
                    teluguNumber.append('౬');
                    break;
                case '7':
                    teluguNumber.append('౭');
                    break;
                case '8':
                    teluguNumber.append('౮');
                    break;
                case '9':
                    teluguNumber.append('౯');
                    break;
            }
        }
        return teluguNumber.toString();
    }

    public static String toTeluguMonthName(int month) {
        String[] teluguMonths = {
                "జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్",
                "జూలై", "ఆగస్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్"
        };
        if (month >= 0 && month < teluguMonths.length) {
            return teluguMonths[month];
        }
        return "";
    }
}
