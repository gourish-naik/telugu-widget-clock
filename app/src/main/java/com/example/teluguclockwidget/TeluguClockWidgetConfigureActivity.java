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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telugu_clock_widget_configure);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

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


        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = TeluguClockWidgetConfigureActivity.this;

                // When the button is clicked, save the string in our prefs and return that they
                // clicked OK.
                String fontSizeStr = mFontSizeEditText.getText().toString();
                String fontColorStr = mFontColorEditText.getText().toString();

                int fontSize = 48;
                if (!fontSizeStr.isEmpty()) {
                    fontSize = Integer.parseInt(fontSizeStr);
                }

                int fontColor = Color.WHITE;
                if (!fontColorStr.isEmpty()) {
                    try {
                        fontColor = Color.parseColor(fontColorStr);
                    } catch (IllegalArgumentException e) {
                        // Invalid color format, use default
                    }
                }

                int selectedHorizontalGravityId = mHorizontalGravityRadioGroup.getCheckedRadioButtonId();
                int horizontalGravity = Gravity.CENTER_HORIZONTAL;
                if (selectedHorizontalGravityId == R.id.left_radio_button) {
                    horizontalGravity = Gravity.LEFT;
                } else if (selectedHorizontalGravityId == R.id.right_radio_button) {
                    horizontalGravity = Gravity.RIGHT;
                }

                int selectedVerticalGravityId = mVerticalGravityRadioGroup.getCheckedRadioButtonId();
                int verticalGravity = Gravity.CENTER_VERTICAL;
                if (selectedVerticalGravityId == R.id.top_radio_button) {
                    verticalGravity = Gravity.TOP;
                } else if (selectedVerticalGravityId == R.id.bottom_radio_button) {
                    verticalGravity = Gravity.BOTTOM;
                }

                int selectedTimeFormatId = mTimeFormatRadioGroup.getCheckedRadioButtonId();
                boolean is12HourFormat = (selectedTimeFormatId == R.id.format_12_hour_radio_button);
                boolean showAmPmIcon = mShowAmPmIconCheckBox.isChecked();
                boolean isFontBold = mFontBoldCheckBox.isChecked();
                boolean showBackgroundOverlay = mShowBackgroundOverlayCheckBox.isChecked();
                String backgroundOverlayColorStr = mBackgroundOverlayColorEditText.getText().toString();
                int backgroundOverlayColor = Color.TRANSPARENT;
                if (!backgroundOverlayColorStr.isEmpty()) {
                    try {
                        backgroundOverlayColor = Color.parseColor(backgroundOverlayColorStr);
                    } catch (IllegalArgumentException e) {
                        // Invalid color format, use default
                    }
                }
                boolean showDate = mShowDateCheckBox.isChecked();
                boolean showTextShadow = mShowTextShadowCheckBox.isChecked();

                final Bundle options = new Bundle();
                options.putInt("font_size", fontSize);
                options.putInt("font_color", fontColor);
                options.putInt("horizontal_gravity", horizontalGravity);
                options.putInt("vertical_gravity", verticalGravity);
                options.putBoolean("is_12_hour_format", is12HourFormat);
                options.putBoolean("show_am_pm_icon", showAmPmIcon);
                options.putBoolean("is_font_bold", isFontBold);
                options.putBoolean("show_background_overlay", showBackgroundOverlay);
                options.putInt("background_overlay_color", backgroundOverlayColor);
                options.putBoolean("show_date", showDate);
                options.putBoolean("show_text_shadow", showTextShadow);

                savePrefs(context, mAppWidgetId, fontSize, fontColor, horizontalGravity, verticalGravity, is12HourFormat, showAmPmIcon, isFontBold, showBackgroundOverlay, backgroundOverlayColor, showDate, showTextShadow);

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                TeluguClockWidget.updateAppWidgetWithPrefs(context, appWidgetManager, mAppWidgetId, options);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        loadPrefs(this, mAppWidgetId);
    }

    private void loadPrefs(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
        int fontSize = prefs.getInt("font_size", 48);
        int fontColor = prefs.getInt("font_color", Color.WHITE);
        int horizontalGravity = prefs.getInt("horizontal_gravity", Gravity.CENTER_HORIZONTAL);
        int verticalGravity = prefs.getInt("vertical_gravity", Gravity.CENTER_VERTICAL);
        boolean is12HourFormat = prefs.getBoolean("is_12_hour_format", true); // Default to 12-hour format
        boolean showAmPmIcon = prefs.getBoolean("show_am_pm_icon", false); // Default to not showing AM/PM icon
        boolean isFontBold = prefs.getBoolean("is_font_bold", false); // Default to not bold
        boolean showBackgroundOverlay = prefs.getBoolean("show_background_overlay", false);
        int backgroundOverlayColor = prefs.getInt("background_overlay_color", Color.TRANSPARENT);
        boolean showDate = prefs.getBoolean("show_date", false);
        boolean showTextShadow = prefs.getBoolean("show_text_shadow", false);

        mFontSizeEditText.setText(String.valueOf(fontSize));
        mFontColorEditText.setText(String.format("#%06X", (0xFFFFFF & fontColor)));

        if (horizontalGravity == Gravity.LEFT) {
            mHorizontalGravityRadioGroup.check(R.id.left_radio_button);
        } else if (horizontalGravity == Gravity.RIGHT) {
            mHorizontalGravityRadioGroup.check(R.id.right_radio_button);
        } else {
            mHorizontalGravityRadioGroup.check(R.id.center_horizontal_radio_button);
        }

        if (verticalGravity == Gravity.TOP) {
            mVerticalGravityRadioGroup.check(R.id.top_radio_button);
        } else if (verticalGravity == Gravity.BOTTOM) {
            mVerticalGravityRadioGroup.check(R.id.bottom_radio_button);
        } else {
            mVerticalGravityRadioGroup.check(R.id.center_vertical_radio_button);
        }

        if (is12HourFormat) {
            mTimeFormatRadioGroup.check(R.id.format_12_hour_radio_button);
        } else {
            mTimeFormatRadioGroup.check(R.id.format_24_hour_radio_button);
        }
        mShowAmPmIconCheckBox.setChecked(showAmPmIcon);
        mFontBoldCheckBox.setChecked(isFontBold);
        mShowBackgroundOverlayCheckBox.setChecked(showBackgroundOverlay);
        mBackgroundOverlayColorEditText.setText(String.format("#%08X", backgroundOverlayColor));
        mShowDateCheckBox.setChecked(showDate);
        mShowTextShadowCheckBox.setChecked(showTextShadow);

        Log.d("WidgetConfig", "Loaded prefs for widget " + appWidgetId + ": " +
                "fontSize=" + fontSize +
                ", fontColor=" + String.format("#%08X", fontColor) +
                ", horizontalGravity=" + horizontalGravity +
                ", verticalGravity=" + verticalGravity +
                ", is12HourFormat=" + is12HourFormat +
                ", showAmPmIcon=" + showAmPmIcon +
                ", isFontBold=" + isFontBold +
                ", showBackgroundOverlay=" + showBackgroundOverlay +
                ", backgroundOverlayColor=" + String.format("#%08X", backgroundOverlayColor) +
                ", showDate=" + showDate +
                ", showTextShadow=" + showTextShadow);
    }

    private void savePrefs(Context context, int appWidgetId, int fontSize, int fontColor, int horizontalGravity, int verticalGravity, boolean is12HourFormat, boolean showAmPmIcon, boolean isFontBold, boolean showBackgroundOverlay, int backgroundOverlayColor, boolean showDate, boolean showTextShadow) {
        SharedPreferences.Editor prefs = context.getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE).edit();
        prefs.putInt("font_size", fontSize);
        prefs.putInt("font_color", fontColor);
        prefs.putInt("horizontal_gravity", horizontalGravity);
        prefs.putInt("vertical_gravity", verticalGravity);
        prefs.putBoolean("is_12_hour_format", is12HourFormat);
        prefs.putBoolean("show_am_pm_icon", showAmPmIcon);
        prefs.putBoolean("is_font_bold", isFontBold);
        prefs.putBoolean("show_background_overlay", showBackgroundOverlay);
        prefs.putInt("background_overlay_color", backgroundOverlayColor);
        prefs.putBoolean("show_date", showDate);
        prefs.putBoolean("show_text_shadow", showTextShadow);
        prefs.apply();

        Log.d("WidgetConfig", "Saved prefs for widget " + appWidgetId + ": " +
                "fontSize=" + fontSize +
                ", fontColor=" + String.format("#%08X", fontColor) +
                ", horizontalGravity=" + horizontalGravity +
                ", verticalGravity=" + verticalGravity +
                ", is12HourFormat=" + is12HourFormat +
                ", showAmPmIcon=" + showAmPmIcon +
                ", isFontBold=" + isFontBold +
                ", showBackgroundOverlay=" + showBackgroundOverlay +
                ", backgroundOverlayColor=" + String.format("#%08X", backgroundOverlayColor) +
                ", showDate=" + showDate +
                ", showTextShadow=" + showTextShadow);
    }
}
