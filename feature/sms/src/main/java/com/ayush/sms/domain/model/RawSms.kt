package com.ayush.sms.domain.model

data class RawSms(
    val sender: String,
    val body: String,
    val timestamp: Long,
    val receivedAt: Long
)
