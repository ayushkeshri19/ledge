package com.ayush.transactions.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseTransactionDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun fetchAllForUser(userId: String): List<SupabaseTransactionDto> {
        return supabaseClient.from("transactions")
            .select { filter { eq("user_id", userId) } }
            .decodeList()
    }

    suspend fun insert(dto: SupabaseTransactionDto): SupabaseTransactionDto {
        return supabaseClient.from("transactions")
            .insert(dto) { select() }
            .decodeSingle()
    }

    suspend fun update(remoteId: String, dto: SupabaseTransactionDto) {
        supabaseClient.from("transactions")
            .update(dto) { filter { eq("id", remoteId) } }
    }

    suspend fun delete(remoteId: String) {
        supabaseClient.from("transactions")
            .delete { filter { eq("id", remoteId) } }
    }
}
