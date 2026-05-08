package com.ayush.sms.domain.usecase

import com.ayush.common.transactions.AutoDetectedTransactionInput
import com.ayush.common.transactions.AutoDetectedTransactionWriter
import com.ayush.common.transactions.AutoDetectedType
import com.ayush.sms.data.local.PendingTransactionDao
import com.ayush.sms.data.local.PendingTransactionEntity
import com.ayush.sms.domain.classifier.CategorySlugResolver
import com.ayush.sms.domain.parser.TransactionType
import javax.inject.Inject

class ConfirmPendingTransactionUseCase @Inject constructor(
    private val pendingDao: PendingTransactionDao,
    private val writer: AutoDetectedTransactionWriter,
    private val categoryResolver: CategorySlugResolver
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        val pending = pendingDao.getById(id)
            ?: return Result.failure(IllegalStateException("Pending transaction $id not found"))

        if (pending.state != PendingTransactionEntity.State.PENDING.name) {
            return Result.failure(IllegalStateException("Pending transaction $id is not in PENDING state"))
        }

        val resolvedCategoryId = categoryResolver.resolve(pending.suggestedCategoryId)
        val type = when (TransactionType.valueOf(pending.type)) {
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
            pendingDao.updateState(id, PendingTransactionEntity.State.CONFIRMED.name)
        }
    }
}
