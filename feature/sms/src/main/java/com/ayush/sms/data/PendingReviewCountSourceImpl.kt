package com.ayush.sms.data

import com.ayush.common.transactions.PendingReviewCountSource
import com.ayush.sms.domain.repository.SmsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingReviewCountSourceImpl @Inject constructor(
    private val smsRepository: SmsRepository
) : PendingReviewCountSource {
    override fun observe(): Flow<Int> = smsRepository.observePendingCount()
}
