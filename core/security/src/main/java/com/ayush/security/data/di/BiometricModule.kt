package com.ayush.security.data.di

import com.ayush.security.data.repository.AppLockManagerImpl
import com.ayush.security.data.repository.BiometricAuthenticatorImpl
import com.ayush.security.domain.repository.AppLockManager
import com.ayush.security.domain.repository.BiometricAuthenticator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BiometricModule {

    @Binds
    abstract fun bindsAppLockManager(impl: AppLockManagerImpl): AppLockManager

    @Binds
    abstract fun bindsBiometricAuthenticator(impl: BiometricAuthenticatorImpl): BiometricAuthenticator

}