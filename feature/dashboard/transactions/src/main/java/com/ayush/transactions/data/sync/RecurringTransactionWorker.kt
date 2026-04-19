package com.ayush.transactions.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ayush.transactions.domain.models.RecurrenceType
import com.ayush.transactions.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(appContext = context, params = params) {

    override suspend fun doWork(): Result {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        transactionRepository.getRecurringTransactions().forEach { template ->
            val recurrence = template.recurrenceType ?: return@forEach
            val baseMillis = template.lastExecutedDate ?: template.date
            val baseDate = Instant.ofEpochMilli(baseMillis).atZone(zone).toLocalDate()

            val nextDueDate = when (recurrence) {
                RecurrenceType.DAILY -> baseDate.plusDays(1)
                RecurrenceType.WEEKLY -> baseDate.plusWeeks(1)
                RecurrenceType.MONTHLY -> baseDate.plusMonths(1)
            }

            if (!nextDueDate.isAfter(today)) {
                val nextDueMillis = nextDueDate.atStartOfDay(zone).toInstant().toEpochMilli()
                transactionRepository.createRecurringInstance(template, nextDueMillis)
                transactionRepository.updateLastExecutedDate(template.id, nextDueMillis)
            }
        }

        return Result.success()
    }
}