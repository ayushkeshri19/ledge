package com.ayush.sms.domain.parser

import com.ayush.sms.domain.parser.extractors.AccountExtractor
import com.ayush.sms.domain.parser.extractors.AmountExtractor
import com.ayush.sms.domain.parser.extractors.MerchantExtractor
import com.ayush.sms.domain.parser.extractors.StandardAccountExtractor
import com.ayush.sms.domain.parser.extractors.StandardAmountExtractor
import com.ayush.sms.domain.parser.extractors.StandardTypeExtractor
import com.ayush.sms.domain.parser.extractors.TypeExtractor

data class ParserRule(
    val name: String,
    val senderPattern: Regex,
    val bodyMustContain: List<Regex> = emptyList(),
    val amountExtractor: AmountExtractor = StandardAmountExtractor,
    val typeExtractor: TypeExtractor = StandardTypeExtractor,
    val merchantExtractor: MerchantExtractor,
    val accountExtractor: AccountExtractor = StandardAccountExtractor,
    val baseConfidence: Float = 0.9f
)