package com.example.teluguclockwidget

import android.content.Context
import android.graphics.*
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import java.util.Calendar

object ClockBitmapRenderer {

    private val teluguDigits = listOf('౦', '౧', '౨', '౩', '౪', '౫', '౬', '౭', '౮', '౯')
    private val teluguMonths = listOf("జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్", "జూలై", "ఆగస్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్")
    private val teluguDays = listOf("ఆది", "సోమ", "మంగళ", "బుధ", "గురు", "శుక్ర", "శని")

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

    private fun getTeluguDay(dayOfWeek: Int): String {
        // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, etc.
        return teluguDays.getOrElse(dayOfWeek - 1) { "" }
    }

    fun render(
        context: Context,
        text: String,
        fontSizeSp: Int,
        color: Int,
        bold: Boolean,
        shadow: Boolean,
        fontResName: String?,
        showDate: Boolean,
        dateFormat: Int = 1, // 1 or 2
        showOverlay: Boolean = false,
        overlayTheme: String = "black" // "black" or "white"
    ): Bitmap {
        val res = context.resources
        val displayMetrics = res.displayMetrics
        
        // Use larger canvas for expanded widgets
        val maxWidth = displayMetrics.widthPixels
        val maxHeight = (displayMetrics.heightPixels * 0.5f).toInt()
        
        // Build date text based on format
        var dateText = ""
        if (showDate) {
            val cal = Calendar.getInstance()
            val day = toTelugu(cal.get(Calendar.DAY_OF_MONTH).toString())
            val month = getTeluguMonth(cal.get(Calendar.MONTH))
            val year = toTelugu(cal.get(Calendar.YEAR).toString())
            val dayName = getTeluguDay(cal.get(Calendar.DAY_OF_WEEK))
            
            dateText = when (dateFormat) {
                1 -> "$dayName $day $month" // Day name, date, month
                2 -> "$day - $month - $year" // Date - Month - Year
                else -> "$day $month $year"
            }
        }
        
        // Create initial paint objects
        val timePaint = createPaint(context, fontSizeSp, color, bold, shadow, fontResName)
        val datePaint = createPaint(context, (fontSizeSp * 0.4f).toInt(), color, bold, shadow, fontResName)
        
        // Auto-scale font to fit within bounds
        val scaledSizes = autoScaleFonts(
            text, dateText, showDate,
            timePaint, datePaint,
            maxWidth, maxHeight
        )
        
        // Apply scaled sizes
        timePaint.textSize = scaledSizes.first
        datePaint.textSize = scaledSizes.second
        
        // Re-apply shadow after size change
        if (shadow) {
            val shadowSize = (scaledSizes.first * 0.04f).coerceAtLeast(6f)
            timePaint.setShadowLayer(shadowSize, shadowSize * 0.5f, shadowSize * 0.7f, Color.argb(200, 0, 0, 0))
            datePaint.setShadowLayer(shadowSize * 0.6f, shadowSize * 0.3f, shadowSize * 0.5f, Color.argb(160, 0, 0, 0))
        }
        
        // Measure final dimensions
        val timeWidth = timePaint.measureText(text)
        val timeFontMetrics = timePaint.fontMetrics
        val timeHeight = timeFontMetrics.descent - timeFontMetrics.ascent
        
        val dateWidth = if (showDate) datePaint.measureText(dateText) else 0f
        val dateFontMetrics = datePaint.fontMetrics
        val dateHeight = if (showDate) (dateFontMetrics.descent - dateFontMetrics.ascent) else 0f
        
        val contentWidth = maxOf(timeWidth, dateWidth)
        val verticalGap = if (showDate) (timeHeight * 0.2f) else 0f
        val contentHeight = timeHeight + verticalGap + dateHeight
        
        // Generous padding for overlay
        val paddingH = (contentWidth * 0.2f).toInt().coerceAtLeast(40)
        val paddingV = (contentHeight * 0.2f).toInt().coerceAtLeast(30)
        
        val bmpW = (contentWidth + paddingH * 2).toInt().coerceAtLeast(200)
        val bmpH = (contentHeight + paddingV * 2).toInt().coerceAtLeast(120)
        
        // Create bitmap
        val bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        
        // Draw rounded overlay background if enabled
        if (showOverlay) {
            val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val overlayColor = when (overlayTheme) {
                "white" -> Color.argb(128, 255, 255, 255) // 50% white
                else -> Color.argb(128, 0, 0, 0) // 50% black (default)
            }
            overlayPaint.color = overlayColor
            overlayPaint.style = Paint.Style.FILL
            
            val cornerRadius = (minOf(bmpW, bmpH) * 0.12f).coerceAtLeast(24f)
            val overlayRect = RectF(0f, 0f, bmpW.toFloat(), bmpH.toFloat())
            canvas.drawRoundRect(overlayRect, cornerRadius, cornerRadius, overlayPaint)
        }

        // Calculate center positions
        val cx = bmpW / 2f
        val cy = bmpH / 2f
        
        // Calculate vertical center for the entire text block
        val totalTextHeight = timeHeight + verticalGap + dateHeight
        val startY = cy - (totalTextHeight / 2f)

        // Draw time centered
        val timeBaseline = startY - timeFontMetrics.ascent
        canvas.drawText(text, cx, timeBaseline, timePaint)

        // Draw date below time if enabled
        if (showDate) {
            val dateBaseline = timeBaseline + timeHeight + verticalGap - dateFontMetrics.ascent
            canvas.drawText(dateText, cx, dateBaseline, datePaint)
        }
        
        return bmp
    }
    
    private fun createPaint(
        context: Context,
        fontSizeSp: Int,
        color: Int,
        bold: Boolean,
        shadow: Boolean,
        fontResName: String?
    ): Paint {
        val res = context.resources
        val textPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            fontSizeSp.toFloat(),
            res.displayMetrics
        )
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG)
        paint.color = color
        paint.textSize = textPx
        paint.textAlign = Paint.Align.CENTER
        
        // Load custom font
        if (!fontResName.isNullOrBlank()) {
            val fontId = res.getIdentifier(fontResName, "font", context.packageName)
            if (fontId != 0) {
                try {
                    val tf = ResourcesCompat.getFont(context, fontId)
                    if (tf != null) {
                        paint.typeface = tf
                    }
                } catch (_: Exception) { }
            }
        }
        
        // Apply bold
        if (bold) {
            paint.typeface = Typeface.create(paint.typeface ?: Typeface.DEFAULT, Typeface.BOLD)
        }
        
        // Apply shadow
        if (shadow) {
            val shadowSize = (textPx * 0.04f).coerceAtLeast(6f)
            paint.setShadowLayer(shadowSize, shadowSize * 0.5f, shadowSize * 0.7f, Color.argb(200, 0, 0, 0))
        }
        
        return paint
    }
    
    private fun autoScaleFonts(
        timeText: String,
        dateText: String,
        showDate: Boolean,
        timePaint: Paint,
        datePaint: Paint,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Float, Float> {
        
        var timeSize = timePaint.textSize
        var dateSize = datePaint.textSize
        
        var iteration = 0
        while (iteration < 25) {
            timePaint.textSize = timeSize
            datePaint.textSize = dateSize
            
            val timeWidth = timePaint.measureText(timeText)
            val dateWidth = if (showDate) datePaint.measureText(dateText) else 0f
            
            val timeHeight = timePaint.fontMetrics.descent - timePaint.fontMetrics.ascent
            val dateHeight = if (showDate) datePaint.fontMetrics.descent - datePaint.fontMetrics.ascent else 0f
            
            val totalWidth = maxOf(timeWidth, dateWidth)
            val verticalGap = if (showDate) (timeHeight * 0.2f) else 0f
            val totalHeight = timeHeight + verticalGap + dateHeight
            
            val paddingH = totalWidth * 0.2f
            val paddingV = totalHeight * 0.2f
            
            val requiredWidth = totalWidth + paddingH * 2
            val requiredHeight = totalHeight + paddingV * 2
            
            if (requiredWidth <= maxWidth && requiredHeight <= maxHeight) {
                break
            }
            
            timeSize *= 0.95f
            dateSize *= 0.95f
            iteration++
        }
        
        // Minimum sizes
        val minTimeSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 28f,
            android.content.res.Resources.getSystem().displayMetrics
        )
        val minDateSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 14f,
            android.content.res.Resources.getSystem().displayMetrics
        )
        
        timeSize = timeSize.coerceAtLeast(minTimeSize)
        dateSize = dateSize.coerceAtLeast(minDateSize)
        
        return Pair(timeSize, dateSize)
    }
}
