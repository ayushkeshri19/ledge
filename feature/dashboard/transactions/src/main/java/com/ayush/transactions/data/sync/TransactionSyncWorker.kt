package com.ayush.transactions.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ayush.database.data.SyncStatus
import com.ayush.transactions.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class TransactionSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TransactionRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = repository.currentUserId() ?: return Result.retry()

        val pending = repository.getPendingSync()
        if (pending.isEmpty()) return Result.success()

        var hasFailure = false

        for (transaction in pending) {
            try {
                when (transaction.syncStatus) {
                    SyncStatus.SYNCED -> continue
                    SyncStatus.PENDING_CREATE -> repository.pushCreate(transaction, userId)
                    SyncStatus.PENDING_UPDATE -> repository.pushUpdate(transaction, userId)
                    SyncStatus.PENDING_DELETE -> repository.pushDelete(transaction)
                }
            } catch (e: Exception) {
                Timber.e(e, "Sync failed for transaction id=${transaction.id}, status=${transaction.syncStatus}")
                hasFailure = true
            }
        }

        return if (hasFailure) Result.retry() else Result.success()
    }
}
