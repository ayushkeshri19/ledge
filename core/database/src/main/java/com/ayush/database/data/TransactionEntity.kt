package com.ayush.database.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index("date"),
        Index("type"),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,
    val categoryId: Long?,
    val note: String,
    val date: Long,
    val isRecurring: Boolean = false,
    val recurrenceType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val userId: String = "",
)
