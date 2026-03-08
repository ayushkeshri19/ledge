package com.ayush.auth.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.ayush.auth.data.repository.AuthRepository
import com.ayush.auth.data.repository.AuthRepositoryImpl
import com.ayush.auth.domain.usecase.AuthEligibilityUseCase
import com.ayush.auth.util.GoogleIdTokenProvider
import com.ayush.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        supabaseClient: SupabaseClient,
        @ApplicationContext context: Context
    ): AuthRepository {
        return AuthRepositoryImpl(
            supabaseClient = supabaseClient,
            context = context
        )
    }

    @Provides
    fun provideAuthEligibilityUseCase(): AuthEligibilityUseCase = AuthEligibilityUseCase()

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    @Provides
    @Singleton
    @Named("google_web_client_id")
    fun providesWebClientId(): String {
        return BuildConfig.GOOGLE_WEB_CLIENT_ID
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
