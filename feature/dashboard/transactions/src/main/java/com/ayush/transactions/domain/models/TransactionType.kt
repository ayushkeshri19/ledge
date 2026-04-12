package com.ayush.transactions.domain.models

enum class TransactionType(val value: String) {
    INCOME("income"),
    EXPENSE("expense");

    companion object {
        fun fromValue(value: String): TransactionType {
            return entries.firstOrNull { it.value == value } ?: EXPENSE
        }
    }
}
