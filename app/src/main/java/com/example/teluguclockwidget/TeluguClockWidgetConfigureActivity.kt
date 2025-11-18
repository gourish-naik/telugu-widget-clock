package com.example.teluguclockwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TeluguClockWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telugu_clock_widget_configure)

        // default canceled
        setResult(Activity.RESULT_CANCELED)

        val fontSizeEt = findViewById<EditText>(R.id.font_size_edit_text)
        val fontColorEt = findViewById<EditText>(R.id.font_color_edit_text)
        val fontSpinner = findViewById<Spinner>(R.id.font_family_spinner)
        val boldCb = findViewById<CheckBox>(R.id.font_bold_checkbox)
        val shadowCb = findViewById<CheckBox>(R.id.show_text_shadow_checkbox)
        val overlayCb = findViewById<CheckBox>(R.id.show_background_overlay_checkbox)
        val showDateCb = findViewById<CheckBox>(R.id.show_date_checkbox)
        val overlayColorEt = findViewById<EditText>(R.id.background_overlay_color_edit_text)
        val timeFormatRg = findViewById<RadioGroup>(R.id.time_format_radio_group)
        val saveBtn = findViewById<Button>(R.id.save_button)

        // load spinner items from arrays.xml
        val fonts = resources.getStringArray(R.array.font_family_array)
        fontSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fonts).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // get widget id
        intent.extras?.let {
            appWidgetId = it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish(); return
        }

        // load prefs
        val prefs = WidgetPrefs(this, appWidgetId)
        fontSizeEt.setText(prefs.fontSize.toString())
        fontColorEt.setText(String.format("#%06X", 0xFFFFFF and prefs.fontColor))
        overlayColorEt.setText(String.format("#%08X", prefs.overlayColor))
        boldCb.isChecked = prefs.bold
        shadowCb.isChecked = prefs.shadow
        overlayCb.isChecked = prefs.overlay
        showDateCb.isChecked = prefs.showDate
        timeFormatRg.check(if (prefs.is12Hour) R.id.format_12_hour_radio_button else R.id.format_24_hour_radio_button)

        // set spinner selection
        val idx = fonts.indexOf(prefs.fontFamily)
        if (idx >= 0) fontSpinner.setSelection(idx)

        saveBtn.setOnClickListener {
            val fontSize = fontSizeEt.text.toString().toIntOrNull() ?: prefs.fontSize
            val fontColor = try { Color.parseColor(fontColorEt.text.toString()) } catch (_:Exception) { prefs.fontColor }
            val overlayColor = try { Color.parseColor(overlayColorEt.text.toString()) } catch (_:Exception) { prefs.overlayColor }
            val fontFamily = fonts[fontSpinner.selectedItemPosition]
            val bold = boldCb.isChecked
            val shadow = shadowCb.isChecked
            val overlay = overlayCb.isChecked
            val showDate = showDateCb.isChecked
            val is12h = timeFormatRg.checkedRadioButtonId == R.id.format_12_hour_radio_button

            // save
            prefs.fontSize = fontSize
            prefs.fontColor = fontColor
            prefs.fontFamily = fontFamily
            prefs.bold = bold
            prefs.shadow = shadow
            prefs.overlay = overlay
            prefs.overlayColor = overlayColor
            prefs.is12Hour = is12h
            prefs.showDate = showDate

            // update widget immediately
            val mgr = AppWidgetManager.getInstance(this)
            TeluguClockWidget.updateWidget(this, mgr, appWidgetId)

            // return result
            val result = Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}
