package com.ayush.sms.domain.classifier

import com.ayush.sms.domain.repository.ClassifierRulesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantClassifier @Inject constructor(
    private val rulesRepository: ClassifierRulesRepository
) {
    suspend fun classify(merchant: String?, body: String? = null): Classification {
        val rules = rulesRepository.getCached()

        merchant?.takeIf { it.isNotBlank() }?.lowercase()?.let { m ->
            val byMerchant = matchInText(m, rules)
            if (byMerchant.categoryId != null) return byMerchant
        }

        body?.takeIf { it.isNotBlank() }?.lowercase()?.let { b ->
            val byBody = matchInText(b, rules)
            if (byBody.categoryId != null) {
                return byBody.copy(classifierConfidence = byBody.classifierConfidence * 0.7f)
            }
        }

        return Classification(null, 0f)
    }

    private fun matchInText(lower: String, rules: List<ClassifierRule>): Classification {
        for (rule in rules) {
            val matches = when (rule.matchType) {
                MatchType.EXACT -> lower == rule.keyword.lowercase()
                MatchType.SUBSTRING -> rule.keyword.lowercase() in lower
            }
            if (matches) {
                val confidence = when (rule.source) {
                    Source.CURATED -> 1.0f
                    Source.AGGREGATOR_DEFAULT -> 0.6f
                    Source.USER_CORRECTION -> 0.9f
                }
                return Classification(rule.categorySlug, confidence)
            }
        }
        return Classification(null, 0f)
    }
}

data class Classification(val categoryId: String?, val classifierConfidence: Float)