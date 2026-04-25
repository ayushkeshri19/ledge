package com.ayush.database.data

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
