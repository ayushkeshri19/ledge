package com.ayush.sms.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.ayush.datastore.domain.usecase.ObserveSmsAutoDetectEnabledUseCase
import com.ayush.sms.domain.model.RawSms
import com.ayush.sms.domain.usecase.ProcessIncomingSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver @Inject constructor() : BroadcastReceiver() {

    @Inject
    lateinit var processIncomingSms: ProcessIncomingSmsUseCase

    @Inject
    lateinit var observeAutoDetectEnabled: ObserveSmsAutoDetectEnabledUseCase

    override fun onReceive(p0: Context?, p1: Intent?) {
        p1 ?: return

        if (p1.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(p1) ?: return
        if (messages.isEmpty()) return

        val sender = messages.first().displayOriginatingAddress ?: return
        val body = messages.joinToString(separator = "") { it.displayMessageBody.orEmpty() }
        val sms = RawSms(
            sender = sender,
            body = body,
            timestamp = messages.first().timestampMillis,
            receivedAt = System.currentTimeMillis()
        )

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (!observeAutoDetectEnabled().first()) return@launch
                processIncomingSms(sms)
            } finally {
                pendingResult.finish()
            }
        }
    }
}