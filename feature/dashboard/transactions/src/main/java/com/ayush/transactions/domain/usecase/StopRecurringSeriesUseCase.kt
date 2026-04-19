package com.ayush.transactions.domain.usecase

import com.ayush.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class StopRecurringSeriesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(templateId: Long) {
        transactionRepository.stopRecurringSeries(templateId)
    }
}