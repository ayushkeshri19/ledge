package com.ayush.sms.di

import com.ayush.sms.domain.parser.ParserRule
import com.ayush.sms.domain.parser.rules.AxisRule
import com.ayush.sms.domain.parser.rules.BobRule
import com.ayush.sms.domain.parser.rules.CanaraRule
import com.ayush.sms.domain.parser.rules.HdfcRule
import com.ayush.sms.domain.parser.rules.IciciRule
import com.ayush.sms.domain.parser.rules.IndusIndRule
import com.ayush.sms.domain.parser.rules.KotakRule
import com.ayush.sms.domain.parser.rules.PnbRule
import com.ayush.sms.domain.parser.rules.SbiRule
import com.ayush.sms.domain.parser.rules.UpiCreditRule
import com.ayush.sms.domain.parser.rules.UpiDebitRule
import com.ayush.sms.domain.parser.rules.YesBankRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    fun provideParserRules(): List<ParserRule> = listOf(
        HdfcRule, IciciRule, SbiRule, AxisRule, KotakRule,
        PnbRule, BobRule, YesBankRule, IndusIndRule, CanaraRule,
        UpiCreditRule, UpiDebitRule
    )
}