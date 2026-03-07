package com.ayush.auth.di

import com.ayush.auth.util.GoogleIdTokenProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GoogleSignInProviderEntryPoint {
    fun googleSignInIdTokenProvider(): GoogleIdTokenProvider
}
