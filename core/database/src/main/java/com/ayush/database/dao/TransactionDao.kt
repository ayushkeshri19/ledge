package com.ayush.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ayush.database.data.CategorySpendTuple
import com.ayush.database.data.SyncStatus
import com.ayush.database.data.TransactionEntity
import com.ayush.database.data.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE syncStatus != 'PENDING_DELETE'
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
        AND syncStatus != 'PENDING_DELETE'
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
        AND syncStatus != 'PENDING_DELETE'
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE type = :type
        AND syncStatus != 'PENDING_DELETE'
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun getTransactionsByType(type: String): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE note LIKE '%' || :query || '%'
        AND syncStatus != 'PENDING_DELETE'
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun searchTransactions(query: String): Flow<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions WHERE id = :id AND syncStatus != 'PENDING_DELETE'")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): TransactionEntity?

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
        AND syncStatus != 'PENDING_DELETE'
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
        AND syncStatus != 'PENDING_DELETE'
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

    @Query("SELECT * FROM transactions WHERE syncStatus != 'SYNCED' ORDER BY createdAt ASC")
    suspend fun getPendingSyncTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus)

    @Query(
        """
        SELECT t.categoryId, c.name AS categoryName, c.colorHex AS categoryColorHex,
               SUM(t.amount) AS totalAmount
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'expense'
          AND t.date BETWEEN :startDate AND :endDate
          AND t.syncStatus != 'PENDING_DELETE'
        GROUP BY t.categoryId
        ORDER BY totalAmount DESC
        """
    )
    suspend fun getExpensesByCategory(startDate: Long, endDate: Long): List<CategorySpendTuple>

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE syncStatus != 'PENDING_DELETE'
        ORDER BY date DESC, createdAt DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentTransactions(limit: Int): List<TransactionWithCategory>
}
