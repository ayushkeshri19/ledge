package com.ayush.security.domain.repository

import androidx.fragment.app.FragmentActivity
import com.ayush.security.domain.models.BiometricResult

interface BiometricAuthenticator {
    suspend fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null
    ): BiometricResult
}