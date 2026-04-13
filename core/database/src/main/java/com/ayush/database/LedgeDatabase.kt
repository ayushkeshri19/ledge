package com.ayush.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ayush.database.converter.SyncStatusConverter
import com.ayush.database.dao.BudgetDao
import com.ayush.database.dao.CategoryDao
import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.BudgetEntity
import com.ayush.database.data.CategoryEntity
import com.ayush.database.data.TransactionEntity
import com.ayush.database.data.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(SyncStatusConverter::class)
abstract class LedgeDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
}
