package com.ayush.network.data.repository

import com.ayush.common.models.User
import com.ayush.common.result.AuthResult
import com.ayush.network.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthResult<User> = runCatching {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        getCurrentUser()
    }.fold(
        onSuccess = { user -> user?.let { AuthResult.Success(it) } ?: AuthResult.Error("Sign-in succeeded but no user in session") },
        onFailure = { e -> AuthResult.Error(e.message ?: "Sign-in failed", e) }
    )

    override suspend fun signUpWithEmail(
        email: String,
        password: String
    ): AuthResult<User> = runCatching {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        getCurrentUser()
    }.fold(
        onSuccess = { user -> user?.let { AuthResult.Success(it) } ?: AuthResult.Error("Sign-up succeeded but email confirmation may be required") },
        onFailure = { e -> AuthResult.Error(e.message ?: "Sign-up failed", e) }
    )

    override suspend fun getCurrentUser(): User? {
        return supabaseClient.auth.currentUserOrNull()?.let { currentUser ->
            val metadata = currentUser.userMetadata
            return User(
                id = currentUser.id,
                email = currentUser.email ?: "",
                fullName = metadata?.get("full_name")?.toString()?.trim('"') ?: "User",
                avatarUrl = metadata?.get("avatar_url")?.toString()?.trim('"')?.takeIf { it != "null" },
                isEmailVerified = currentUser.emailConfirmedAt != null
            )
        } ?: run {
            // fetch from db. if null there as well, return null finally
            null
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthResult<User> = runCatching {
        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
        getCurrentUser()
    }.fold(
        onSuccess = { user -> user?.let { AuthResult.Success(it) } ?: AuthResult.Error("Sign-in succeeded but no user in session") },
        onFailure = { e -> AuthResult.Error(e.message ?: "Google sign-in failed", e) }
    )


    override suspend fun resetPasswordForEmail(email: String): AuthResult<Unit> = runCatching {
        supabaseClient.auth.resetPasswordForEmail(email)
    }.fold(
        onSuccess = { AuthResult.Success(Unit) },
        onFailure = { AuthResult.Error(it.message ?: "Password reset failed", it) }
    )

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}