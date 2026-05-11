package com.ayush.sms.data.local.rawSms

import com.ayush.sms.domain.model.RawSms

fun RawSmsEntity.toDomain(): RawSms = RawSms(
    sender = sender,
    body = body,
    timestamp = timestamp,
    receivedAt = receivedAt
)

fun RawSms.toEntity(): RawSmsEntity = RawSmsEntity(
    sender = sender,
    body = body,
    timestamp = timestamp,
    receivedAt = receivedAt
)
