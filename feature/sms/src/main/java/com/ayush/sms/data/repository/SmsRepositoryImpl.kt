package com.ayush.sms.data.repository

import com.ayush.sms.data.local.PendingTransactionDao
import com.ayush.sms.data.local.RawSmsDao
import com.ayush.sms.data.local.toDomain
import com.ayush.sms.data.local.toEntity
import com.ayush.sms.domain.model.RawSms
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class SmsRepositoryImpl @Inject constructor(
    private val rawSmsDao: RawSmsDao,
    private val pendingTransactionDao: PendingTransactionDao
) : SmsRepository {

    override suspend fun saveRawSms(sms: RawSms): Long = rawSmsDao.insert(sms.toEntity())

    override fun observeRecent(limit: Int): Flow<List<RawSms>> =
        rawSmsDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun clearAll() = rawSmsDao.clear()

    override fun observePending(): Flow<List<PendingTransaction>> =
        pendingTransactionDao.observePending().map { list -> list.map { it.toDomain() } }

    override suspend fun getPendingById(id: Long): PendingTransaction? =
        pendingTransactionDao.getById(id)?.toDomain()

    override suspend fun savePending(record: PendingTransaction): Long =
        pendingTransactionDao.insert(record.toEntity())

    override suspend fun updatePendingState(id: Long, state: PendingTransaction.State) =
        pendingTransactionDao.updateState(id, state.name)
}
