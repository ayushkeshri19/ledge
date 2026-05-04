package com.ayush.sms.data.repository

import com.ayush.sms.data.local.RawSmsDao
import com.ayush.sms.data.local.toDomain
import com.ayush.sms.data.local.toEntity
import com.ayush.sms.domain.model.RawSms
import com.ayush.sms.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class SmsRepositoryImpl @Inject constructor(
    private val dao: RawSmsDao
) : SmsRepository {

    override suspend fun saveRawSms(sms: RawSms): Long = dao.insert(sms.toEntity())

    override fun observeRecent(limit: Int): Flow<List<RawSms>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun clearAll() = dao.clear()
}
