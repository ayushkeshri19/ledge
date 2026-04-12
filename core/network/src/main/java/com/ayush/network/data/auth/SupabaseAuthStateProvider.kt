package com.ayush.network.data.auth

import com.ayush.common.auth.AuthState
import com.ayush.common.auth.AuthStateProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class SupabaseAuthStateProvider @Inject constructor(
    private val supabaseClient: SupabaseClient,
) : AuthStateProvider {
    override val authState: Flow<AuthState> = supabaseClient.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> AuthState.Authenticated
            is SessionStatus.NotAuthenticated -> AuthState.NotAuthenticated
            SessionStatus.Initializing -> AuthState.Loading
            else -> AuthState.Loading
        }
    }

    override suspend fun validateSession() {
        if (supabaseClient.auth.currentSessionOrNull() == null) return
        try {
            supabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
        } catch (e: RestException) {
            Timber.w(e, "Session token invalid (${e.statusCode}), signing out")
            runCatching { supabaseClient.auth.signOut() }
        } catch (e: Exception) {
            Timber.w(e, "Session validation failed (network/other), keeping session")
        }
    }
}
