package com.ayush.auth.data.repository

import android.content.Context
import com.ayush.common.models.User
import com.ayush.common.result.ApiResult
import com.ayush.common.utils.toast
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @param:ApplicationContext private val context: Context
) : AuthRepository {

    companion object {
        private const val REDIRECT_URL = "com.ayush.ledge://login-callback"
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): ApiResult<User> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            getCurrentUser()
        }.fold(
            onSuccess = { user ->
                user?.let { ApiResult.Success(it) } ?: ApiResult.Error("Sign-in succeeded but no user in session")
            },
            onFailure = { e -> ApiResult.Error(e.message ?: "Sign-in failed", e) }
        )
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String
    ): ApiResult<User> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.auth.signUpWith(Email, redirectUrl = REDIRECT_URL) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("full_name", name)
                }
            }
            getCurrentUser()
        }.fold(
            onSuccess = { user ->
                user?.let { ApiResult.Success(it) }
                    ?: ApiResult.Error("Sign-up succeeded but email confirmation may be required")
            },
            onFailure = { e -> ApiResult.Error(e.message ?: "Sign-up failed", e) }
        )
    }

    override suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            supabaseClient.auth.currentUserOrNull()?.let { currentUser ->
                val metadata = currentUser.userMetadata
                User(
                    id = currentUser.id,
                    email = currentUser.email ?: "",
                    fullName = metadata?.get("full_name")?.toString()?.trim('"') ?: "User",
                    avatarUrl = metadata?.get("avatar_url")?.toString()?.trim('"')?.takeIf { it != "null" },
                    isEmailVerified = currentUser.emailConfirmedAt != null
                )
            }
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String, rawNonce: String): ApiResult<User> =
        withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.nonce = rawNonce
                provider = Google
            }
            getCurrentUser()
        }.fold(
            onSuccess = { user ->
                user?.let { ApiResult.Success(it) } ?: ApiResult.Error("Sign-in succeeded but no user in session")
            },
            onFailure = { e -> ApiResult.Error(e.message ?: "Google sign-in failed", e) }
        )
    }

    override suspend fun resetPasswordForEmail(email: String): ApiResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.auth.resetPasswordForEmail(email, redirectUrl = REDIRECT_URL)
        }.fold(
            onSuccess = { ApiResult.Success(Unit) },
            onFailure = { ApiResult.Error(it.message ?: "Password reset failed", it) }
        )
    }

    override suspend fun updatePassword(newPassword: String): ApiResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.auth.updateUser { password = newPassword }
        }.fold(
            onSuccess = { ApiResult.Success(Unit) },
            onFailure = { ApiResult.Error(it.message ?: "Password update failed", it) }
        )
    }

    override suspend fun signOut() {
        withContext(Dispatchers.Main) {
            "Signed out successfully".toast(context)
            withContext(Dispatchers.IO) {
                supabaseClient.auth.signOut()
            }
        }
    }
}
