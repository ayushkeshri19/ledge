package com.ayush.sms.domain.usecase

import com.ayush.sms.data.local.PendingTransactionDao
import com.ayush.sms.data.local.PendingTransactionEntity
import javax.inject.Inject

class DismissPendingTransactionUseCase @Inject constructor(
    private val pendingDao: PendingTransactionDao
) {
    suspend operator fun invoke(id: Long) {
        pendingDao.updateState(id, PendingTransactionEntity.State.DISMISSED.name)
    }
}
