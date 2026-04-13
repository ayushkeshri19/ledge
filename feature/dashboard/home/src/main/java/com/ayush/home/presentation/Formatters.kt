package com.ayush.home.presentation

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun formatDate(millis: Long): String {
    fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    val date = Calendar.getInstance().apply { timeInMillis = millis; clearTime() }
    val today = Calendar.getInstance().apply { clearTime() }
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1); clearTime() }
    return when (date.timeInMillis) {
        today.timeInMillis -> "Today"
        yesterday.timeInMillis -> "Yesterday"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))
    }
}

internal fun formatTime(millis: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))
