package com.ayush.budget.di

import com.ayush.budget.data.repository.BudgetRepositoryImpl
import com.ayush.budget.domain.repository.BudgetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BudgetModule {

    @Binds
    abstract fun bindsBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
}