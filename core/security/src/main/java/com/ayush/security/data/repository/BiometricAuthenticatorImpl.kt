package com.ayush.security.data.repository

import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ayush.security.domain.models.BiometricResult
import com.ayush.security.domain.repository.BiometricAuthenticator
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BiometricAuthenticatorImpl @Inject constructor() : BiometricAuthenticator {

    override suspend fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String?
    ): BiometricResult = suspendCancellableCoroutine { cont ->
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (cont.isActive) cont.resume(BiometricResult.Success)
            }

            override fun onAuthenticationError(code: Int, msg: CharSequence) {
                if (!cont.isActive) return
                val mapped = when (code) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_CANCELED -> BiometricResult.UserCancelled

                    else -> BiometricResult.Error(code, msg.toString())
                }
                cont.resume(mapped)
            }

            override fun onAuthenticationFailed() {
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply { if (subtitle != null) setSubtitle(subtitle) }
            .setAllowedAuthenticators(authenticators())
            .build()

        prompt.authenticate(info)
    }

    private fun authenticators(): Int {
        return BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    }
}