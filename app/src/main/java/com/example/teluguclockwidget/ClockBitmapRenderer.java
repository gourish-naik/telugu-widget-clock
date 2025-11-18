package com.example.teluguclockwidget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.TypedValue;

public class ClockBitmapRenderer {

    private static final char[] TELUGU_DIGITS = {
            '౦', '౧', '౨', '౩', '౪', '౫', '౬', '౭', '౮', '౯'
    };

    public static String convertToTelugu(String input) {
        StringBuilder out = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                out.append(TELUGU_DIGITS[c - '0']);
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static Bitmap renderClockBitmap(
            Context context,
            String timeStr,
            int fontSizeSp,
            int color,
            boolean bold,
            boolean shadow,
            String fontFamilyResourceName    // e.g. "noto_sans_telugu"
    ) {
        Resources res = context.getResources();

        // Convert sp to px
        float fontSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                fontSizeSp,
                res.getDisplayMetrics()
        );

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(fontSizePx);
        paint.setTextAlign(Paint.Align.LEFT);

        if (bold) {
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }

        if (shadow) {
            paint.setShadowLayer(6f, 1f, 2f, Color.argb(120, 0, 0, 0));
        }

        // Load custom font from res/font (if exists)
        if (fontFamilyResourceName != null) {
            int fontId = res.getIdentifier(fontFamilyResourceName, "font", context.getPackageName());
            if (fontId != 0) {
                Typeface tf = res.getFont(fontId);
                if (tf != null) {
                    paint.setTypeface(tf);
                }
            }
        }

        // Measure text bounds
        Rect bounds = new Rect();
        paint.getTextBounds(timeStr, 0, timeStr.length(), bounds);

        int width = bounds.width() + 40;
        int height = bounds.height() + 40;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.TRANSPARENT);

        float x = 20;
        float y = height / 2f - (paint.descent() + paint.ascent()) / 2f;

        canvas.drawText(timeStr, x, y, paint);

        return bmp;
    }
}
