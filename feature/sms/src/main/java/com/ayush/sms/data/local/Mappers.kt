package com.ayush.sms.data.local

import com.ayush.sms.domain.model.RawSms

internal fun RawSmsEntity.toDomain(): RawSms = RawSms(
    sender = sender,
    body = body,
    timestamp = timestamp,
    receivedAt = receivedAt
)

internal fun RawSms.toEntity(): RawSmsEntity = RawSmsEntity(
    sender = sender,
    body = body,
    timestamp = timestamp,
    receivedAt = receivedAt
)
