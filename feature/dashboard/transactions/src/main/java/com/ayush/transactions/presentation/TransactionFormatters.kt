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

internal fun formatAmount(amount: Double): String =
    if (amount == amount.toLong().toDouble()) {
        String.format(Locale.getDefault(), "%,d", amount.toLong())
    } else {
        String.format(Locale.getDefault(), "%,.2f", amount)
    }

internal fun formatAmountForInput(amount: Double): String =
    if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        amount.toString()
    }
