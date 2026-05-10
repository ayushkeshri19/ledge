package com.ayush.sms.domain.usecase

import com.ayush.common.transactions.AutoDetectedTransactionInput
import com.ayush.common.transactions.AutoDetectedTransactionWriter
import com.ayush.common.transactions.AutoDetectedType
import com.ayush.sms.domain.classifier.CategorySlugResolver
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.parser.TransactionType
import com.ayush.sms.domain.repository.SmsRepository
import javax.inject.Inject

class ConfirmPendingTransactionUseCase @Inject constructor(
    private val repository: SmsRepository,
    private val writer: AutoDetectedTransactionWriter,
    private val categoryResolver: CategorySlugResolver
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        val pending = repository.getPendingById(id)
            ?: return Result.failure(IllegalStateException("Pending transaction $id not found"))

        if (pending.state != PendingTransaction.State.PENDING) {
            return Result.failure(IllegalStateException("Pending transaction $id is not in PENDING state"))
        }

        val resolvedCategoryId = categoryResolver.resolve(pending.suggestedCategoryId)
        val type = when (pending.type) {
            TransactionType.DEBIT -> AutoDetectedType.DEBIT
            TransactionType.CREDIT -> AutoDetectedType.CREDIT
        }

        val writeResult = writer.write(
            AutoDetectedTransactionInput(
                amount = pending.amount,
                type = type,
                categoryId = resolvedCategoryId,
                merchant = pending.merchant,
                date = pending.smsTimestamp,
                note = pending.merchant
            )
        )

        return writeResult.onSuccess {
            repository.updatePendingState(id, PendingTransaction.State.CONFIRMED)
        }
    }
}
