package com.ayush.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ayush.database.data.TransactionEntity
import com.ayush.database.data.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun getTransactionsByDateRange(
        startDate: Long,
        endDate: Long,
    ): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE categoryId = :categoryId
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE type = :type
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun getTransactionsByType(type: String): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE note LIKE '%' || :query || '%'
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT SUM(amount) FROM transactions
        WHERE type = :type AND date BETWEEN :startDate AND :endDate
        """
    )
    suspend fun getTotalByTypeAndDateRange(
        type: String,
        startDate: Long,
        endDate: Long,
    ): Double?

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
        AND (:type IS NULL OR type = :type)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:minAmount IS NULL OR amount >= :minAmount)
        AND (:maxAmount IS NULL OR amount <= :maxAmount)
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun getFilteredTransactions(
        startDate: Long,
        endDate: Long,
        type: String? = null,
        categoryId: Long? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
    ): Flow<List<TransactionWithCategory>>
}
