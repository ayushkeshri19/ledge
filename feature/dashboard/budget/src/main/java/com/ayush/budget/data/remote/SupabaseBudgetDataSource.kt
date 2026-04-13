package com.ayush.budget.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseBudgetDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun fetchAllForUser(userId: String): List<SupabaseBudgetDto> {
        return supabaseClient.from("budgets")
            .select { filter { eq("user_id", userId) } }
            .decodeList()
    }

    suspend fun insert(dto: SupabaseBudgetDto): SupabaseBudgetDto {
        return supabaseClient.from("budgets")
            .insert(dto) { select() }
            .decodeSingle()
    }

    suspend fun update(remoteId: String, dto: SupabaseBudgetDto) {
        supabaseClient.from("budgets")
            .update(dto) { filter { eq("id", remoteId) } }
    }

    suspend fun delete(remoteId: String) {
        supabaseClient.from("budgets")
            .delete { filter { eq("id", remoteId) } }
    }
}
