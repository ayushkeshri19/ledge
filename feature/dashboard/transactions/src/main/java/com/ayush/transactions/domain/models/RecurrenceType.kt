package com.ayush.transactions.domain.models

enum class RecurrenceType(val value: String) {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    companion object {
        fun fromValue(value: String?): RecurrenceType? {
            if (value == null) return null
            return entries.firstOrNull { it.value == value }
        }
    }
}
