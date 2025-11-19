package com.example.teluguclockwidget

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import java.util.Calendar

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

    // load font if provided
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
        timePaint.typeface = Typeface.create(timePaint.typeface ?: Typeface.DEFAULT, Typeface.BOLD)
        datePaint.typeface = Typeface.create(datePaint.typeface ?: Typeface.DEFAULT, Typeface.BOLD)
    }

    if (shadow) {
        timePaint.setShadowLayer(6f, 1f, 2f, Color.argb(140, 0, 0, 0))
        datePaint.setShadowLayer(6f, 1f, 2f, Color.argb(140, 0, 0, 0))
    }

    // Build date text if required
    var dateText = ""
    if (showDate) {
        val cal = Calendar.getInstance()
        val day = toTelugu(cal.get(Calendar.DAY_OF_MONTH).toString())
        val month = getTeluguMonth(cal.get(Calendar.MONTH))
        val year = toTelugu(cal.get(Calendar.YEAR).toString())
        dateText = "$day $month $year"
    }

    // Measure widths/heights using Paint metrics (more accurate)
    val timeWidth = timePaint.measureText(text).toInt()
    val timeMetrics = timePaint.fontMetrics
    val timeHeight = (timeMetrics.descent - timeMetrics.ascent).toInt()

    var dateWidth = 0
    var dateHeight = 0
    if (showDate) {
        dateWidth = datePaint.measureText(dateText).toInt()
        val dateMetrics = datePaint.fontMetrics
        dateHeight = (dateMetrics.descent - dateMetrics.ascent).toInt()
    }

    val contentWidth = maxOf(timeWidth, dateWidth)
    val verticalGap = (textPx * 0.15f).toInt()
    val contentHeight = timeHeight + if (showDate) (verticalGap + dateHeight) else 0

    val padding = (textPx * 0.35f).toInt()
    val bmpW = (contentWidth + padding * 2).coerceAtLeast(1)
    val bmpH = (contentHeight + padding * 2).coerceAtLeast(1)

    val bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

    val cx = bmpW / 2f

    // compute baseline for time so entire block is vertically centered
    // baseline for time = center - (contentHeight/2) + (timeHeight - (timeMetrics.descent)). The baseline math uses ascent/descent.
    val timeBaseline = (bmpH / 2f) - (contentHeight / 2f) + (timeHeight - timeMetrics.descent)

    canvas.drawText(text, cx, timeBaseline, timePaint)

    if (showDate) {
        // date baseline is timeBaseline + timeHeight + verticalGap but adjusted for font descent
        val dateBaseline = timeBaseline + timeHeight + verticalGap.toFloat()
        canvas.drawText(dateText, cx, dateBaseline - datePaint.fontMetrics.descent, datePaint)
    }

    return bmp
}

}
