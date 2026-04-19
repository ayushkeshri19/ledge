package com.ayush.security.data.di

import com.ayush.security.domain.repository.BiometricAuthenticator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BiometricAuthenticatorEntryPoint {
    fun biometricAuthenticator(): BiometricAuthenticator
}