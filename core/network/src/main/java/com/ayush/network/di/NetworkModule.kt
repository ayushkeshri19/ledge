package com.ayush.network.di

import com.ayush.common.auth.AuthStateProvider
import com.ayush.common.auth.PasswordRecoveryStateHolder
import com.ayush.common.deeplink.DeepLinkHandler
import com.ayush.network.BuildConfig
import com.ayush.network.data.auth.SupabaseAuthStateProvider
import com.ayush.network.data.deeplink.SupabaseDeepLinkHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.HttpTimeout
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @OptIn(SupabaseInternal::class)
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                flowType = FlowType.IMPLICIT
                scheme = "com.ayush.ledge"
                host = "login-callback"
            }
            install(Postgrest)
            install(Storage)
            httpConfig {
                install(HttpTimeout) {
                    connectTimeoutMillis = 15_000
                    requestTimeoutMillis = 30_000
                    socketTimeoutMillis = 30_000
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideDeepLinkHandler(
        supabaseClient: SupabaseClient,
        passwordRecoveryStateHolder: PasswordRecoveryStateHolder
    ): DeepLinkHandler {
        return SupabaseDeepLinkHandler(supabaseClient, passwordRecoveryStateHolder)
    }

    @Provides
    @Singleton
    fun provideAuthStateProvider(
        supabaseClient: SupabaseClient,
    ): AuthStateProvider {
        return SupabaseAuthStateProvider(supabaseClient)
    }
}