package com.ayush.network.di

import android.content.Context
import com.ayush.common.auth.AuthStateProvider
import com.ayush.common.deeplink.DeepLinkHandler
import com.ayush.network.BuildConfig
import com.ayush.network.data.auth.SupabaseAuthStateProvider
import com.ayush.network.data.deeplink.SupabaseDeepLinkHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

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
        }
    }

    @Provides
    @Singleton
    fun provideDeepLinkHandler(supabaseClient: SupabaseClient): DeepLinkHandler {
        return SupabaseDeepLinkHandler(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideAuthStateProvider(
        supabaseClient: SupabaseClient,
        @ApplicationContext context: Context
    ): AuthStateProvider {
        return SupabaseAuthStateProvider(
            supabaseClient = supabaseClient,
            context = context
        )
    }
}