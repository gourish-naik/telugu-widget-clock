package com.example.teluguclockwidget

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar

class TeluguClockWidgetConfigureActivity : AppCompatActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    private lateinit var fontSizeEt: EditText
    private lateinit var colorPickerBtn: Button // Changed from colorPickerButton
    private lateinit var colorPreview: View
    private lateinit var fontSpinner: Spinner
    private lateinit var boldCb: CheckBox
    private lateinit var shadowCb: CheckBox
    private lateinit var overlayCb: CheckBox
    private lateinit var showDateCb: CheckBox
    private lateinit var dateFormatRg: RadioGroup
    private lateinit var overlayThemeRg: RadioGroup
    private lateinit var timeFormatRg: RadioGroup
    private lateinit var previewImage: ImageView
    private lateinit var fonts: Array<String>

    private var selectedFontColor: Int = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telugu_clock_widget_configure)

        setResult(Activity.RESULT_CANCELED)

        // Initialize views
        fontSizeEt = findViewById(R.id.font_size_edit_text)
        colorPickerBtn = findViewById(R.id.color_picker_button) // Changed from colorPickerButton
        colorPreview = findViewById(R.id.color_preview)
        fontSpinner = findViewById(R.id.font_family_spinner)
        boldCb = findViewById(R.id.font_bold_checkbox)
        shadowCb = findViewById(R.id.show_text_shadow_checkbox)
        overlayCb = findViewById(R.id.show_background_overlay_checkbox)
        showDateCb = findViewById(R.id.show_date_checkbox)
        dateFormatRg = findViewById(R.id.date_format_radio_group)
        overlayThemeRg = findViewById(R.id.overlay_theme_radio_group)
        timeFormatRg = findViewById(R.id.time_format_radio_group)
        val saveBtn = findViewById<Button>(R.id.save_button)
        previewImage = findViewById(R.id.preview_image)

        fonts = resources.getStringArray(R.array.font_family_array)
        fontSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fonts).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // get widget id
        intent.extras?.let {
            appWidgetId = it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // load prefs
        val prefs = WidgetPrefs(this, appWidgetId)
        fontSizeEt.setText(prefs.fontSize.toString())
        selectedFontColor = prefs.fontColor
        colorPreview.setBackgroundColor(selectedFontColor)
        boldCb.isChecked = prefs.bold
        shadowCb.isChecked = prefs.shadow
        overlayCb.isChecked = prefs.overlay
        showDateCb.isChecked = prefs.showDate
        timeFormatRg.check(if (prefs.is12Hour) R.id.format_12_hour_radio_button else R.id.format_24_hour_radio_button)
        dateFormatRg.check(when (prefs.dateFormat) {
            1 -> R.id.date_format_1
            else -> R.id.date_format_2
        })
        overlayThemeRg.check(when (prefs.overlayTheme) {
            "black" -> R.id.overlay_black
            else -> R.id.overlay_white
        })

        // set spinner selection
        val idx = fonts.indexOf(prefs.fontFamily)
        if (idx >= 0) fontSpinner.setSelection(idx)

        // Setup live preview listeners
        setupLivePreview()
        
        // Initial preview
        updatePreview()

        saveBtn.setOnClickListener {
            saveSettings()
        }

        // Color picker button listener
        colorPickerBtn.setOnClickListener { // Changed from colorPickerButton
            showColorPickerDialog("Select Font Color", selectedFontColor) { color ->
                selectedFontColor = color
                colorPreview.setBackgroundColor(selectedFontColor)
                updatePreview()
            }
        }
    }

    private fun showColorPickerDialog(title: String, initialColor: Int, onColorSelected: (Int) -> Unit) {
        val colors = arrayOf(
            Color.BLACK, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.WHITE,
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA,
            ContextCompat.getColor(this, android.R.color.holo_red_light),
            ContextCompat.getColor(this, android.R.color.holo_green_light),
            ContextCompat.getColor(this, android.R.color.holo_blue_light),
            ContextCompat.getColor(this, android.R.color.holo_orange_light),
            ContextCompat.getColor(this, android.R.color.holo_purple)
        )
        val colorNames = arrayOf(
            "Black", "Dark Gray", "Gray", "Light Gray", "White",
            "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta",
            "Light Red", "Light Green", "Light Blue", "Light Orange", "Light Purple"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val radioGroup = RadioGroup(this)
        radioGroup.orientation = LinearLayout.VERTICAL

        colors.forEachIndexed { index, color ->
            val radioButton = RadioButton(this).apply {
                text = colorNames[index]
                id = index
                if (color == initialColor) {
                    isChecked = true
                }
            }
            radioGroup.addView(radioButton)
        }
        layout.addView(radioGroup)
        builder.setView(layout)

        builder.setPositiveButton("Select") { dialog, _ ->
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                onColorSelected(colors[selectedId])
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun setupLivePreview() {
        // Font size changes
        fontSizeEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        })

        // Font family changes
        fontSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Checkbox changes
        val checkboxListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            updatePreview()
        }
        boldCb.setOnCheckedChangeListener(checkboxListener)
        shadowCb.setOnCheckedChangeListener(checkboxListener)
        overlayCb.setOnCheckedChangeListener(checkboxListener)
        showDateCb.setOnCheckedChangeListener(checkboxListener)

        // Time format changes
        timeFormatRg.setOnCheckedChangeListener { _, _ ->
            updatePreview()
        }

        // Date format changes
        dateFormatRg.setOnCheckedChangeListener { _, _ ->
            updatePreview()
        }

        // Overlay theme changes
        overlayThemeRg.setOnCheckedChangeListener { _, _ ->
            updatePreview()
        }
    }

    private fun updatePreview() {
        try {
            // Get current settings
            val prefs = WidgetPrefs(this, appWidgetId)
            val fontSize = fontSizeEt.text.toString().toIntOrNull() ?: prefs.fontSize
            val fontColor = selectedFontColor // Use selectedFontColor
            val fontFamily = fonts[fontSpinner.selectedItemPosition]
            val bold = boldCb.isChecked
            val shadow = shadowCb.isChecked
            val showDate = showDateCb.isChecked
            val dateFormat = when (dateFormatRg.checkedRadioButtonId) { // Get dateFormat
                R.id.date_format_1 -> 1
                else -> 2
            }
            val is12h = timeFormatRg.checkedRadioButtonId == R.id.format_12_hour_radio_button

            // Generate preview time
            val cal = Calendar.getInstance()
            var hour = if (is12h) cal.get(Calendar.HOUR) else cal.get(Calendar.HOUR_OF_DAY)
            if (is12h && hour == 0) hour = 12
            val minute = cal.get(Calendar.MINUTE)

            val formatted = String.format("%02d:%02d", hour, minute)
            val telugu = ClockBitmapRenderer.toTelugu(formatted)

            // Render preview bitmap
            val bmp = ClockBitmapRenderer.render(
                this,
                telugu,
                fontSize,
                fontColor,
                bold,
                shadow,
                fontFamily,
                showDate,
                dateFormat // Pass dateFormat
            )

            // Set preview image
            previewImage.setImageBitmap(bmp)
            
            // Remove preview background logic, as overlay is handled by TeluguClockWidget.kt
            previewImage.setBackgroundColor(Color.TRANSPARENT)
            previewImage.setPadding(0, 0, 0, 0)

        } catch (e: Exception) {
            // If preview fails, show error message
            Toast.makeText(this, "Preview error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSettings() {
        val prefs = WidgetPrefs(this, appWidgetId)
        
        val fontSize = fontSizeEt.text.toString().toIntOrNull() ?: prefs.fontSize
        val fontColor = selectedFontColor // Use selectedFontColor
        val fontFamily = fonts[fontSpinner.selectedItemPosition]
        val bold = boldCb.isChecked
        val shadow = shadowCb.isChecked
        val overlay = overlayCb.isChecked
        val showDate = showDateCb.isChecked
        val dateFormat = when (dateFormatRg.checkedRadioButtonId) { // Get dateFormat
            R.id.date_format_1 -> 1
            else -> 2
        }
        val overlayTheme = when (overlayThemeRg.checkedRadioButtonId) { // Get overlayTheme
            R.id.overlay_black -> "black"
            else -> "white"
        }
        val is12h = timeFormatRg.checkedRadioButtonId == R.id.format_12_hour_radio_button

        // save
        prefs.fontSize = fontSize
        prefs.fontColor = fontColor // Save selectedFontColor
        prefs.fontFamily = fontFamily
        prefs.bold = bold
        prefs.shadow = shadow
        prefs.overlay = overlay
        prefs.overlayTheme = overlayTheme // Save overlayTheme
        prefs.is12Hour = is12h
        prefs.showDate = showDate
        prefs.dateFormat = dateFormat // Save dateFormat

        // update widget immediately
        val mgr = AppWidgetManager.getInstance(this)
        TeluguClockWidget.updateWidget(this, mgr, appWidgetId)

        // return result
        val result = Intent().apply { 
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) 
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}