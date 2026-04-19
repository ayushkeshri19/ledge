package com.ayush.transactions.domain.models

import com.ayush.database.data.TransactionWithCategory

data class Transaction(
    val id: Long,
    val amount: Double,
    val type: TransactionType,
    val category: Category?,
    val note: String,
    val date: Long,
    val isRecurring: Boolean,
    val recurrenceType: RecurrenceType?,
    val parentId: Long? = null,
    val lastExecutedDate: Long? = null
)

fun TransactionWithCategory.toDomain(): Transaction = Transaction(
    id = transaction.id,
    amount = transaction.amount,
    type = TransactionType.fromValue(transaction.type),
    category = category?.toDomain(),
    note = transaction.note,
    date = transaction.date,
    isRecurring = transaction.isRecurring,
    recurrenceType = RecurrenceType.fromValue(transaction.recurrenceType),
    parentId = transaction.parentId,
    lastExecutedDate = transaction.lastExecutedDate
)
