package com.ayush.sms.domain.usecase

import com.ayush.common.transactions.AutoDetectedTransactionInput
import com.ayush.common.transactions.AutoDetectedTransactionWriter
import com.ayush.common.transactions.AutoDetectedType
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.repository.SmsRepository
import javax.inject.Inject

class ConfirmEditedPendingTransactionUseCase @Inject constructor(
    private val repository: SmsRepository,
    private val writer: AutoDetectedTransactionWriter
) {
    suspend operator fun invoke(
        id: Long,
        amount: Double,
        type: AutoDetectedType,
        categoryId: Long?,
        note: String,
        date: Long
    ): Result<Unit> {
        val pending = repository.getPendingById(id)
            ?: return Result.failure(IllegalStateException("Pending transaction $id not found"))

        if (pending.state != PendingTransaction.State.PENDING) {
            return Result.failure(IllegalStateException("Pending transaction $id is not in PENDING state"))
        }

        val writeResult = writer.write(
            AutoDetectedTransactionInput(
                amount = amount,
                type = type,
                categoryId = categoryId,
                merchant = pending.merchant,
                date = date,
                note = note
            )
        )

        return writeResult.onSuccess {
            repository.updatePendingState(id, PendingTransaction.State.CONFIRMED)
        }
    }
}
