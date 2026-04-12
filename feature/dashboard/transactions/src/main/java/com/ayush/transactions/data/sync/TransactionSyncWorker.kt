package com.ayush.transactions.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ayush.database.dao.CategoryDao
import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.SyncStatus
import com.ayush.transactions.data.remote.SupabaseTransactionDataSource
import com.ayush.transactions.data.remote.SupabaseTransactionDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import timber.log.Timber

@HiltWorker
class TransactionSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val supabaseDataSource: SupabaseTransactionDataSource,
    private val supabaseClient: SupabaseClient,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = supabaseClient.auth.currentSessionOrNull()?.user?.id ?: return Result.retry()

        val pending = transactionDao.getPendingSyncTransactions()

        if (pending.isEmpty()) return Result.success()

        var hasFailure = false

        for (transaction in pending) {
            try {
                when (transaction.syncStatus) {

                    SyncStatus.SYNCED -> continue

                    SyncStatus.PENDING_CREATE -> {
                        val categoryName = transaction.categoryId?.let {
                            categoryDao.getCategoryById(it)?.name
                        }
                        val dto = SupabaseTransactionDto(
                            userId = userId,
                            amount = transaction.amount,
                            type = transaction.type,
                            categoryName = categoryName,
                            note = transaction.note,
                            date = transaction.date,
                            isRecurring = transaction.isRecurring,
                            recurrenceType = transaction.recurrenceType,
                            createdAt = transaction.createdAt,
                        )
                        val response = supabaseDataSource.insert(dto)
                        transactionDao.update(
                            transaction.copy(
                                remoteId = response.id,
                                syncStatus = SyncStatus.SYNCED,
                            )
                        )
                    }

                    SyncStatus.PENDING_UPDATE -> {
                        val remoteId = transaction.remoteId ?: continue

                        val categoryName = transaction.categoryId?.let {
                            categoryDao.getCategoryById(it)?.name
                        }

                        val dto = SupabaseTransactionDto(
                            userId = userId,
                            amount = transaction.amount,
                            type = transaction.type,
                            categoryName = categoryName,
                            note = transaction.note,
                            date = transaction.date,
                            isRecurring = transaction.isRecurring,
                            recurrenceType = transaction.recurrenceType,
                            createdAt = transaction.createdAt,
                        )
                        supabaseDataSource.update(remoteId, dto)

                        transactionDao.updateSyncStatus(transaction.id, SyncStatus.SYNCED)
                    }

                    SyncStatus.PENDING_DELETE -> {
                        transaction.remoteId?.let { remoteId ->
                            supabaseDataSource.delete(remoteId)
                        }
                        transactionDao.deleteById(transaction.id)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Sync failed for transaction id=${transaction.id}, status=${transaction.syncStatus}")
                hasFailure = true
            }
        }

        return if (hasFailure) Result.retry() else Result.success()
    }
}