package com.ayush.auth.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.ayush.auth.data.GoogleIdTokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    @Provides
    @Singleton
    fun provideGoogleIdTokenProvider(
        credentialManager: CredentialManager,
        @Named("google_web_client_id") serverClientId: String
    ): GoogleIdTokenProvider {
        return GoogleIdTokenProvider(
            credentialManager = credentialManager,
            serverClientId = serverClientId
        )
    }
}
