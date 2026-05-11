package com.ayush.sms.data.repository

import com.ayush.sms.data.local.classifier.BakedInClassifierRules
import com.ayush.sms.data.local.classifier.ClassifierRuleDao
import com.ayush.sms.data.local.classifier.toDomain
import com.ayush.sms.data.local.classifier.toEntity
import com.ayush.sms.data.remote.SupabaseClassifierRulesRemoteSource
import com.ayush.sms.domain.classifier.ClassifierRule
import com.ayush.sms.domain.repository.ClassifierRulesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ClassifierRulesRepositoryImpl @Inject constructor(
    private val remote: SupabaseClassifierRulesRemoteSource,
    private val dao: ClassifierRuleDao,
    private val bakedIn: BakedInClassifierRules
) : ClassifierRulesRepository {
    override suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val remoteRules = remote.fetchEnabledRules().map { it.toDomain() }
            if (remoteRules.isNotEmpty()) {
                dao.replaceAll(remoteRules.map { it.toEntity() })
            }
        }.onFailure {
            Timber.w(it, "Classifier rules refresh failed; keeping cached rules")
        }
    }

    override suspend fun getCached(): List<ClassifierRule> {
        return withContext(Dispatchers.IO) {
            val cached = dao.getAll().mapNotNull { it.toDomain() }
            cached.ifEmpty { bakedIn.rules }
        }
    }
}