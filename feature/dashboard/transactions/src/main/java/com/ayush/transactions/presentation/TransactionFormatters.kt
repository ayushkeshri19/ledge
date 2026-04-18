package com.ayush.transactions.presentation

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun formatDate(millis: Long, showYear: Boolean = false): String {
    val pattern = if (showYear) "dd MMM yyyy" else "dd MMM"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}

internal fun formatTime(millis: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))

internal fun formatTime(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
}

internal fun formatAmountForInput(amount: Double): String =
    if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        amount.toString()
    }

internal fun formatDateHeader(millis: Long): String {
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
        else -> formatDate(millis, showYear = true)
    }
}
