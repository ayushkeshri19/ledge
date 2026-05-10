package com.ayush.sms.domain.usecase

import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePendingTransactionsUseCase @Inject constructor(
    private val repository: SmsRepository
) {
    operator fun invoke(): Flow<List<PendingTransaction>> = repository.observePending()
}
