package com.ayush.sms.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClassifierRulesRemoteSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun fetchEnabledRules(): List<ClassifierRuleDto> = withContext(Dispatchers.IO) {
        supabaseClient.from(TABLE_NAME)
            .select {
                filter { eq("enabled", true) }
                order("priority", Order.DESCENDING)
            }
            .decodeList()
    }

    companion object {
        private const val TABLE_NAME = "merchant_classifier_rules"
    }

}