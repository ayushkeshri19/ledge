package com.ayush.sms.domain.repository

import com.ayush.sms.domain.model.RawSms
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    suspend fun saveRawSms(sms: RawSms): Long
    fun observeRecent(limit: Int = 50): Flow<List<RawSms>>
    suspend fun clearAll()
}
