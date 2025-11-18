package com.example.teluguclockwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.util.Log;

public class TeluguClockWidgetConfigureActivity extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private EditText mFontSizeEditText;
    private EditText mFontColorEditText;

    private RadioGroup mHorizontalGravityRadioGroup;
    private RadioGroup mVerticalGravityRadioGroup;
    private RadioGroup mTimeFormatRadioGroup;

    private CheckBox mShowAmPmIconCheckBox;
    private CheckBox mFontBoldCheckBox;
    private CheckBox mShowBackgroundOverlayCheckBox;
    private EditText mBackgroundOverlayColorEditText;
    private CheckBox mShowDateCheckBox;
    private CheckBox mShowTextShadowCheckBox;

    private Spinner mFontFamilySpinner;

    // Available fonts in "res/font/"
    private final String[] AVAILABLE_FONTS = {
            "noto_sans_telugu",     // must match: res/font/noto_sans_telugu.ttf
            "noto_serif_telugu",
            "poppins_regular",
            "roboto_regular"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telugu_clock_widget_configure);

        setResult(RESULT_CANCELED);

        // Bind UI
        mFontSizeEditText = findViewById(R.id.font_size_edit_text);
        mFontColorEditText = findViewById(R.id.font_color_edit_text);

        mHorizontalGravityRadioGroup = findViewById(R.id.horizontal_gravity_radio_group);
        mVerticalGravityRadioGroup = findViewById(R.id.vertical_gravity_radio_group);
        mTimeFormatRadioGroup = findViewById(R.id.time_format_radio_group);

        mShowAmPmIconCheckBox = findViewById(R.id.show_am_pm_icon_checkbox);
        mFontBoldCheckBox = findViewById(R.id.font_bold_checkbox);
        mShowBackgroundOverlayCheckBox = findViewById(R.id.show_background_overlay_checkbox);
        mBackgroundOverlayColorEditText = findViewById(R.id.background_overlay_color_edit_text);
        mShowDateCheckBox = findViewById(R.id.show_date_checkbox);
        mShowTextShadowCheckBox = findViewById(R.id.show_text_shadow_checkbox);

        mFontFamilySpinner = findViewById(R.id.font_family_spinner);

        // Spinner: font family
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                AVAILABLE_FONTS);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFontFamilySpinner.setAdapter(fontAdapter);

        // Read widgetId
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Load existing prefs
        loadPrefs(this, mAppWidgetId);

        // Save button
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> saveConfigAndFinish());
    }

    private void saveConfigAndFinish() {
        Context context = TeluguClockWidgetConfigureActivity.this;

        int fontSize = getIntOrDefault(mFontSizeEditText.getText().toString(), 48);
        int fontColor = parseColorOrDefault(mFontColorEditText.getText().toString(), Color.WHITE);

        // Gravity
        int horizontalGravity = Gravity.CENTER_HORIZONTAL;
        int selectedH = mHorizontalGravityRadioGroup.getCheckedRadioButtonId();
        if (selectedH == R.id.left_radio_button) horizontalGravity = Gravity.LEFT;
        else if (selectedH == R.id.right_radio_button) horizontalGravity = Gravity.RIGHT;

        int verticalGravity = Gravity.CENTER_VERTICAL;
        int selectedV = mVerticalGravityRadioGroup.getCheckedRadioButtonId();
        if (selectedV == R.id.top_radio_button) verticalGravity = Gravity.TOP;
        else if (selectedV == R.id.bottom_radio_button) verticalGravity = Gravity.BOTTOM;

        boolean is12h = mTimeFormatRadioGroup.getCheckedRadioButtonId() == R.id.format_12_hour_radio_button;

        boolean showAmPm = mShowAmPmIconCheckBox.isChecked();
        boolean bold = mFontBoldCheckBox.isChecked();
        boolean overlay = mShowBackgroundOverlayCheckBox.isChecked();
        boolean showDate = mShowDateCheckBox.isChecked();
        boolean shadow = mShowTextShadowCheckBox.isChecked();

        int overlayColor = parseColorOrDefault(mBackgroundOverlayColorEditText.getText().toString(), Color.TRANSPARENT);

        // Selected font family
        String fontFamily = AVAILABLE_FONTS[mFontFamilySpinner.getSelectedItemPosition()];

        // Save prefs
        savePrefs(context, mAppWidgetId, fontSize, fontColor, horizontalGravity, verticalGravity,
                is12h, showAmPm, bold, overlay, overlayColor, showDate, shadow, fontFamily);

        // Trigger widget update
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        TeluguClockWidget.updateWidgetStatic(context, manager, mAppWidgetId);

        // Return result
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private int parseColorOrDefault(String str, int def) {
        try {
            return Color.parseColor(str);
        } catch (Exception e) {
            return def;
        }
    }

    private int getIntOrDefault(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return def;
        }
    }

    private void savePrefs(Context context, int id,
                           int fontSize, int fontColor,
                           int hGravity, int vGravity,
                           boolean is12h, boolean showAmPm,
                           boolean bold, boolean overlay,
                           int overlayColor,
                           boolean showDate, boolean shadow,
                           String fontFamily) {

        SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs_" + id, MODE_PRIVATE).edit();

        prefs.putInt("font_size", fontSize);
        prefs.putInt("font_color", fontColor);
        prefs.putInt("horizontal_gravity", hGravity);
        prefs.putInt("vertical_gravity", vGravity);
        prefs.putBoolean("is_12_hour_format", is12h);
        prefs.putBoolean("show_am_pm_icon", showAmPm);
        prefs.putBoolean("is_font_bold", bold);
        prefs.putBoolean("show_background_overlay", overlay);
        prefs.putInt("background_overlay_color", overlayColor);
        prefs.putBoolean("show_date", showDate);
        prefs.putBoolean("show_text_shadow", shadow);

        // NEW LINE: save selected font family  
        prefs.putString("font_family", fontFamily);

        prefs.apply();
    }

    private void loadPrefs(Context context, int id) {
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs_" + id, MODE_PRIVATE);

        int fontSize = prefs.getInt("font_size", 48);
        int fontColor = prefs.getInt("font_color", Color.WHITE);

        mFontSizeEditText.setText(String.valueOf(fontSize));
        mFontColorEditText.setText(String.format("#%06X", (0xFFFFFF & fontColor)));

        // Gravity
        int h = prefs.getInt("horizontal_gravity", Gravity.CENTER_HORIZONTAL);
        if (h == Gravity.LEFT) mHorizontalGravityRadioGroup.check(R.id.left_radio_button);
        else if (h == Gravity.RIGHT) mHorizontalGravityRadioGroup.check(R.id.right_radio_button);
        else mHorizontalGravityRadioGroup.check(R.id.center_horizontal_radio_button);

        int v = prefs.getInt("vertical_gravity", Gravity.CENTER_VERTICAL);
        if (v == Gravity.TOP) mVerticalGravityRadioGroup.check(R.id.top_radio_button);
        else if (v == Gravity.BOTTOM) mVerticalGravityRadioGroup.check(R.id.bottom_radio_button);
        else mVerticalGravityRadioGroup.check(R.id.center_vertical_radio_button);

        boolean is12h = prefs.getBoolean("is_12_hour_format", true);
        if (is12h) mTimeFormatRadioGroup.check(R.id.format_12_hour_radio_button);
        else mTimeFormatRadioGroup.check(R.id.format_24_hour_radio_button);

        mShowAmPmIconCheckBox.setChecked(prefs.getBoolean("show_am_pm_icon", false));
        mFontBoldCheckBox.setChecked(prefs.getBoolean("is_font_bold", false));
        mShowBackgroundOverlayCheckBox.setChecked(prefs.getBoolean("show_background_overlay", false));
        mBackgroundOverlayColorEditText.setText(String.format("#%08X", prefs.getInt("background_overlay_color", Color.TRANSPARENT)));
        mShowDateCheckBox.setChecked(prefs.getBoolean("show_date", false));
        mShowTextShadowCheckBox.setChecked(prefs.getBoolean("show_text_shadow", true));

        // Load font family
        String savedFont = prefs.getString("font_family", AVAILABLE_FONTS[0]);

        // Set spinner selection
        for (int i = 0; i < AVAILABLE_FONTS.length; i++) {
            if (AVAILABLE_FONTS[i].equals(savedFont)) {
                mFontFamilySpinner.setSelection(i);
                break;
            }
        }
    }
}
