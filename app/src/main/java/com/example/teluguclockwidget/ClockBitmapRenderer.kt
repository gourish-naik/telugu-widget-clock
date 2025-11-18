package com.example.teluguclockwidget

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat

object ClockBitmapRenderer {

    private val teluguDigits = listOf('౦', '౧', '౨', '౩', '౪', '౫', '౬', '౭', '౮', '౯')
    private val teluguMonths = listOf("జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్", "జూలై", "ఆగస్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్")

    fun toTelugu(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            if (ch.isDigit()) sb.append(teluguDigits[ch - '0'])
            else sb.append(ch)
        }
        return sb.toString()
    }

    private fun getTeluguMonth(month: Int): String {
        return teluguMonths.getOrElse(month) { "" }
    }

    fun render(
        context: Context,
        text: String,
        fontSizeSp: Int,
        color: Int,
        bold: Boolean,
        shadow: Boolean,
        fontResName: String?,
        showDate: Boolean
    ): Bitmap {
        val res = context.resources
        val textPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSizeSp.toFloat(), res.displayMetrics)
        val dateTextPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, res.displayMetrics)

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
        timePaint.color = color
        timePaint.textSize = textPx
        timePaint.textAlign = Paint.Align.CENTER

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
        datePaint.color = color
        datePaint.textSize = dateTextPx
        datePaint.textAlign = Paint.Align.CENTER

        // Try load font from res/font by name
        if (!fontResName.isNullOrBlank()) {
            val fontId = res.getIdentifier(fontResName, "font", context.packageName)
            if (fontId != 0) {
                try {
                    val tf = ResourcesCompat.getFont(context, fontId)
                    if (tf != null) {
                        timePaint.typeface = tf
                        datePaint.typeface = tf
                    }
                } catch (_: Exception) { /* fallback */ }
            }
        }

        if (bold) {
            val current = timePaint.typeface
            val boldTypeface = Typeface.create(current ?: Typeface.DEFAULT, Typeface.BOLD)
            timePaint.typeface = boldTypeface
            datePaint.typeface = boldTypeface
        }

        if (shadow) {
            timePaint.setShadowLayer(6f, 1f, 2f, Color.argb(140, 0, 0, 0))
            datePaint.setShadowLayer(6f, 1f, 2f, Color.argb(140, 0, 0, 0))
        }

        val timeBounds = Rect()
        timePaint.getTextBounds(text, 0, text.length, timeBounds)

        var dateText = ""
        val dateBounds = Rect()
        if (showDate) {
            val cal = java.util.Calendar.getInstance()
            val day = toTelugu(cal.get(java.util.Calendar.DAY_OF_MONTH).toString())
            val month = getTeluguMonth(cal.get(java.util.Calendar.MONTH))
            val year = toTelugu(cal.get(java.util.Calendar.YEAR).toString())
            dateText = "$day $month $year"
            datePaint.getTextBounds(dateText, 0, dateText.length, dateBounds)
        }

        val padding = (textPx * 0.2f).toInt()
        val totalHeight = timeBounds.height() + if (showDate) dateBounds.height() + padding else 0
        val bmpW = (timeBounds.width().coerceAtLeast(dateBounds.width()) + padding * 2).coerceAtLeast(1)
        val bmpH = (totalHeight + padding * 2).coerceAtLeast(1)

        val bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val x = bmpW / 2f
        val timeY = (bmpH - totalHeight) / 2f + timeBounds.height() - timePaint.descent()
        canvas.drawText(text, x, timeY, timePaint)

        if (showDate) {
            val dateY = timeY + padding + dateBounds.height() - datePaint.descent()
            canvas.drawText(dateText, x, dateY, datePaint)
        }

        return bmp
    }
}
