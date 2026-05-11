package com.ayush.sms.data.local.parserMiss

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parser_misses")
data class ParserMissEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val bodyLength: Int,
    val failureReason: String,
    val hadCurrencyToken: Boolean,
    val hadVerbToken: Boolean,
    val timestamp: Long
)
