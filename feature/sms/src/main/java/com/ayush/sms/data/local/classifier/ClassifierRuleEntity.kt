package com.ayush.sms.data.local.classifier

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classifier_rules_cache")
data class ClassifierRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val categorySlug: String,
    val matchType: String,
    val priority: Int,
    val source: String
)
