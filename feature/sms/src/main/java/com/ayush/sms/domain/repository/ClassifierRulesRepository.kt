package com.ayush.sms.domain.repository

import com.ayush.sms.domain.classifier.ClassifierRule

interface ClassifierRulesRepository {
    suspend fun refresh(): Result<Unit>
    suspend fun getCached(): List<ClassifierRule>
}