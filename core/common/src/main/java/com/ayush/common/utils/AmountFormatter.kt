package com.ayush.common.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val IndianSymbols = DecimalFormatSymbols(Locale.US)

fun formatAmount(amount: Double): String {
    val pattern = if (amount == amount.toLong().toDouble()) "#,##,##0" else "#,##,##0.00"
    return DecimalFormat(pattern, IndianSymbols).format(amount)
}
