package com.ayush.sms.domain.usecase

import com.ayush.sms.data.local.ParserMissDao
import com.ayush.sms.data.local.ParserMissEntity
import com.ayush.sms.data.local.ProcessedSmsDao
import com.ayush.sms.data.local.ProcessedSmsEntity
import com.ayush.sms.data.local.SmsIdKey
import com.ayush.sms.domain.classifier.MerchantClassifier
import com.ayush.sms.domain.model.RawSms
import com.ayush.sms.domain.parser.ParseResult
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.parser.SMSParser
import com.ayush.sms.domain.parser.rules.Guards
import com.ayush.sms.domain.repository.SmsRepository
import javax.inject.Inject

class ProcessIncomingSmsUseCase @Inject constructor(
    private val processedSmsDao: ProcessedSmsDao,
    private val repository: SmsRepository,
    private val parserMissDao: ParserMissDao,
    private val parser: SMSParser,
    private val classifier: MerchantClassifier
) {
    suspend operator fun invoke(sms: RawSms) {
        val key = SmsIdKey.of(sms.sender, sms.body, sms.timestamp)
        if (processedSmsDao.exists(key)) return

        when (val result = parser.parse(sms.sender, sms.body, sms.timestamp)) {
            is ParseResult.Success -> {
                val parsed = result.parsedTransaction
                val classification = classifier.classify(parsed.merchant)
                val finalConfidence = (parsed.parserConfidence + classification.classifierConfidence) / 2f
                repository.savePending(
                    PendingTransaction(
                        id = 0,
                        amount = parsed.amount,
                        type = parsed.type,
                        merchant = parsed.merchant,
                        suggestedCategoryId = classification.categoryId,
                        accountLastFour = parsed.accountLastFour,
                        smsTimestamp = parsed.smsTimestamp,
                        rawSnippet = parsed.rawSnippet,
                        sender = parsed.sender,
                        finalConfidence = finalConfidence,
                        state = PendingTransaction.State.PENDING
                    )
                )
            }

            is ParseResult.Miss -> {
                parserMissDao.insert(
                    ParserMissEntity(
                        sender = sms.sender,
                        bodyLength = sms.body.length,
                        failureReason = result.reason.name,
                        hadCurrencyToken = Guards.hasCurrencyToken(sms.body),
                        hadVerbToken = Guards.hasVerbToken(sms.body),
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            ParseResult.Rejected -> Unit
        }

        processedSmsDao.insert(ProcessedSmsEntity(key, System.currentTimeMillis()))
    }
}
