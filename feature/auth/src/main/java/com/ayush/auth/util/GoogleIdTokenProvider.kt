package com.ayush.auth.util

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom

class GoogleIdTokenProvider(
    private val credentialManager: CredentialManager,
    private val serverClientId: String
) {

    suspend fun getIdToken(activity: Activity): Result<String> = withContext(Dispatchers.Main.immediate) {
        runCatching {
            val nonce = generateNonce()

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            googleIdTokenCredential.idToken
        }.recover { e ->
            when (e) {
                is GetCredentialCancellationException -> throw Exception("Sign-in cancelled")
                is NoCredentialException -> throw Exception("No Google account found on this device")
                else -> throw Exception("Google sign-in failed. Please try again.")
            }
        }
    }

    private fun generateNonce(): String {
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return MessageDigest.getInstance("SHA-256").digest(raw)
            .joinToString("") { "%02x".format(it) }
    }
}
