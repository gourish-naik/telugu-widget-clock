package com.example.teluguclockwidget

object TeluguStrings {
    private val teluguDigits = listOf('౦', '౧', '౨', '౩', '౪', '౫', '౬', '౭', '౮', '౯')
    val teluguMonths = listOf("జనవరి", "ఫిబ్రవరి", "మార్చి", "ఏప్రిల్", "మే", "జూన్", "జూలై", "ఆగస్టు", "సెప్టెంబర్", "అక్టోబర్", "నవంబర్", "డిసెంబర్")

    fun toTelugu(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            if (ch.isDigit()) sb.append(teluguDigits[ch - '0'])
            else sb.append(ch)
        }
        return sb.toString()
    }
}
