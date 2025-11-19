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
        overlayTheme: String = "black", // "black" or "white"
        width: Int,
        height: Int
    ): Bitmap {
        val res = context.resources
        
        // Ensure we have some minimum dimensions to work with
        val safeWidth = if (width > 0) width else 400
        val safeHeight = if (height > 0) height else 200
        
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
            safeWidth, safeHeight
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
        
        // Gap between time and date
        val verticalGap = if (showDate) {
             TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6f, // 6dp gap
                context.resources.displayMetrics
            )
        } else 0f

        val contentHeight = timeHeight + verticalGap + dateHeight
        
        // Padding for overlay/bitmap
        val paddingH = (contentWidth * 0.1f).toInt().coerceAtLeast(20)
        val paddingV = (contentHeight * 0.1f).toInt().coerceAtLeast(20)
        
        val bmpW = (contentWidth + paddingH * 2).toInt().coerceAtLeast(safeWidth)
        val bmpH = (contentHeight + paddingV * 2).toInt().coerceAtLeast(safeHeight)
        
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
        
        // Calculate vertical start position to center the block
        // Total block height = timeHeight + gap + dateHeight
        // We want the center of this block to be at cy
        val totalBlockHeight = timeHeight + verticalGap + dateHeight
        val blockTop = cy - (totalBlockHeight / 2f)
        
        // Draw time
        // timeBaseline is blockTop + timeAscent (since ascent is negative) -> wait, drawText y is baseline.
        // Distance from top of time text to baseline is -ascent.
        val timeBaseline = blockTop - timeFontMetrics.ascent
        canvas.drawText(text, cx, timeBaseline, timePaint)

        // Draw date below time if enabled
        if (showDate) {
            // Date starts at blockTop + timeHeight + gap
            val dateTop = blockTop + timeHeight + verticalGap
            val dateBaseline = dateTop - dateFontMetrics.ascent
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
        
        // Safety check
        if (maxWidth <= 0 || maxHeight <= 0) return Pair(timeSize, dateSize)

        var iteration = 0
        while (iteration < 30) {
            timePaint.textSize = timeSize
            datePaint.textSize = dateSize
            
            val timeWidth = timePaint.measureText(timeText)
            val dateWidth = if (showDate) datePaint.measureText(dateText) else 0f
            
            val timeHeight = timePaint.fontMetrics.descent - timePaint.fontMetrics.ascent
            val dateHeight = if (showDate) datePaint.fontMetrics.descent - datePaint.fontMetrics.ascent else 0f
            
            val totalWidth = maxOf(timeWidth, dateWidth)
            
            val verticalGap = if (showDate) {
                 TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6f,
                    android.content.res.Resources.getSystem().displayMetrics
                )
            } else 0f
            
            val totalHeight = timeHeight + verticalGap + dateHeight
            
            // Check if it fits with some padding (10%)
            if (totalWidth <= (maxWidth * 0.9f) && totalHeight <= (maxHeight * 0.9f)) {
                break
            }
            
            timeSize *= 0.95f
            dateSize *= 0.95f
            iteration++
        }
        
        // Minimum sizes
        val minTimeSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 20f,
            android.content.res.Resources.getSystem().displayMetrics
        )
        val minDateSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 10f,
            android.content.res.Resources.getSystem().displayMetrics
        )
        
        timeSize = timeSize.coerceAtLeast(minTimeSize)
        dateSize = dateSize.coerceAtLeast(minDateSize)
        
        return Pair(timeSize, dateSize)
    }
}
