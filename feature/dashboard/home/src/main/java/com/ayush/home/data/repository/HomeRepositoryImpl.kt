package com.ayush.home.data.repository

import com.ayush.common.models.User
import com.ayush.home.domain.repository.HomeRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : HomeRepository {

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        supabaseClient.auth.currentUserOrNull()?.let { currentUser ->
            val metadata = currentUser.userMetadata
            User(
                id = currentUser.id,
                email = currentUser.email ?: "",
                fullName = metadata?.get("full_name")?.toString()?.trim('"') ?: "User",
                avatarUrl = metadata?.get("avatar_url")?.toString()?.trim('"')?.takeIf { it != "null" },
                isEmailVerified = currentUser.emailConfirmedAt != null,
            )
        }
    }
}
