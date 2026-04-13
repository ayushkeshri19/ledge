package com.ayush.common.utils

import java.util.Locale

fun formatAmount(amount: Double): String =
    if (amount == amount.toLong().toDouble()) {
        String.format(Locale.getDefault(), "%,d", amount.toLong())
    } else {
        String.format(Locale.getDefault(), "%,.2f", amount)
    }