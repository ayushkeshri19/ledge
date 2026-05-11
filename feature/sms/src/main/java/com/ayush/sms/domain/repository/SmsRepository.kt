package com.ayush.sms.domain.repository

import com.ayush.sms.domain.model.RawSms
import com.ayush.sms.domain.parser.PendingTransaction
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    suspend fun saveRawSms(sms: RawSms): Long
    fun observeRecent(limit: Int = 50): Flow<List<RawSms>>
    suspend fun clearAll()

    fun observePending(): Flow<List<PendingTransaction>>
    fun observePendingCount(): Flow<Int>
    suspend fun getPendingById(id: Long): PendingTransaction?
    suspend fun savePending(record: PendingTransaction): Long
    suspend fun updatePendingState(id: Long, state: PendingTransaction.State)
}
