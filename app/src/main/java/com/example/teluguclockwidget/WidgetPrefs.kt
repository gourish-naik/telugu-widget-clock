package com.example.teluguclockwidget

import android.content.Context
import androidx.core.content.ContextCompat

class WidgetPrefs(private val ctx: Context, private val widgetId: Int) {

    private val name = "widget_prefs_$widgetId"
    private val prefs = ctx.getSharedPreferences(name, Context.MODE_PRIVATE)

    var fontSize: Int
        get() = prefs.getInt("font_size", 48)
        set(v) = prefs.edit().putInt("font_size", v).apply()

    var fontColor: Int
        get() = prefs.getInt("font_color", ContextCompat.getColor(ctx, android.R.color.white))
        set(v) = prefs.edit().putInt("font_color", v).apply()

    var fontFamily: String?
        get() = prefs.getString("font_family", "noto_sans_telugu")
        set(v) = prefs.edit().putString("font_family", v).apply()

    var bold: Boolean
        get() = prefs.getBoolean("is_font_bold", false)
        set(v) = prefs.edit().putBoolean("is_font_bold", v).apply()

    var shadow: Boolean
        get() = prefs.getBoolean("show_text_shadow", true)
        set(v) = prefs.edit().putBoolean("show_text_shadow", v).apply()

    var overlay: Boolean
        get() = prefs.getBoolean("show_background_overlay", false)
        set(v) = prefs.edit().putBoolean("show_background_overlay", v).apply()

    var overlayColor: Int
        get() = prefs.getInt("background_overlay_color", 0x66000000.toInt())
        set(v) = prefs.edit().putInt("background_overlay_color", v).apply()

    var is12Hour: Boolean
        get() = prefs.getBoolean("is_12_hour_format", true)
        set(v) = prefs.edit().putBoolean("is_12_hour_format", v).apply()

    var showDate: Boolean
        get() = prefs.getBoolean("show_date", false)
        set(v) = prefs.edit().putBoolean("show_date", v).apply()
}
