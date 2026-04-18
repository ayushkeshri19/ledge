package com.ayush.insights.di

import com.ayush.insights.data.repository.InsightsRepositoryImpl
import com.ayush.insights.domain.repository.InsightsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InsightsModule {

    @Binds
    abstract fun bindInsightsRepository(impl: InsightsRepositoryImpl): InsightsRepository
}
