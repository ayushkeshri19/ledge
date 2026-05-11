package com.ayush.sms.data.local.classifier

import com.ayush.sms.data.remote.ClassifierRuleDto
import com.ayush.sms.domain.classifier.ClassifierRule
import com.ayush.sms.domain.classifier.MatchType
import com.ayush.sms.domain.classifier.Source

fun ClassifierRuleDto.toDomain(): ClassifierRule = ClassifierRule(
    keyword = keyword,
    categorySlug = categorySlug,
    matchType = MatchType.fromValue(matchType),
    priority = priority,
    source = Source.fromValue(source)
)

fun ClassifierRule.toEntity(): ClassifierRuleEntity = ClassifierRuleEntity(
    keyword = keyword,
    categorySlug = categorySlug,
    matchType = matchType.name,
    priority = priority,
    source = source.name
)

fun ClassifierRuleEntity.toDomain(): ClassifierRule? = try {
    ClassifierRule(
        keyword = keyword,
        categorySlug = categorySlug,
        matchType = MatchType.valueOf(matchType),
        priority = priority,
        source = Source.valueOf(source)
    )
} catch (_: IllegalArgumentException) {
    null
}