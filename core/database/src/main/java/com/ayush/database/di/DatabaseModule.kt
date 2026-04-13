package com.ayush.database.di

import android.content.Context
import androidx.room.Room
import com.ayush.database.LedgeDatabase
import com.ayush.database.dao.BudgetDao
import com.ayush.database.dao.CategoryDao
import com.ayush.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LedgeDatabase {
        return Room.databaseBuilder(
            context,
            LedgeDatabase::class.java,
            "ledge_db",
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideTransactionDao(database: LedgeDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideCategoryDao(database: LedgeDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun providesBudgetDao(database: LedgeDatabase): BudgetDao {
        return database.budgetDao()
    }
}
