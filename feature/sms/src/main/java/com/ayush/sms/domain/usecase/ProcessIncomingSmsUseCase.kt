package com.ayush.sms.domain.usecase

import com.ayush.sms.domain.model.RawSms
import com.ayush.sms.domain.repository.SmsRepository
import timber.log.Timber
import javax.inject.Inject

class ProcessIncomingSmsUseCase @Inject constructor(
    private val smsRepository: SmsRepository
) {
    suspend operator fun invoke(sms: RawSms) {
        Timber.tag(TAG).d("Received SMS from %s at %d", sms.sender, sms.timestamp)
        runCatching { smsRepository.saveRawSms(sms) }
            .onFailure { Timber.tag(TAG).e(it, "Failed to persist SMS") }
    }

    companion object {
        private const val TAG = "SmsIngestion"
    }
}