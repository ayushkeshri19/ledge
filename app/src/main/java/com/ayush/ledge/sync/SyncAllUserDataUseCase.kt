package com.ayush.ledge.sync

import com.ayush.budget.domain.repository.BudgetRepository
import com.ayush.common.sync.SyncOrchestrator
import com.ayush.common.sync.SyncState
import com.ayush.common.sync.SyncStateHolder
import com.ayush.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject

class SyncAllUserDataUseCase @Inject constructor(
    private val syncStateHolder: SyncStateHolder,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : SyncOrchestrator {

    override suspend fun syncAll() = coroutineScope {
        if (syncStateHolder.state.value is SyncState.Syncing) {
            Timber.d("syncAll() skipped — already in flight")
            return@coroutineScope
        }

        syncStateHolder.onSyncStarted()

        val categoriesResult = runCatching {
            transactionRepository.ensureDefaultCategories()
        }.onFailure { Timber.e(it, "ensureDefaultCategories failed") }

        val syncResults = listOf(
            async {
                runCatching { transactionRepository.syncFromRemote() }
                    .onFailure { Timber.e(it, "transaction syncFromRemote failed") }
            },
            async {
                runCatching { budgetRepository.syncFromRemote() }
                    .onFailure { Timber.e(it, "budget syncFromRemote failed") }
            }
        ).awaitAll()

        val firstError = listOf(categoriesResult, *syncResults.toTypedArray())
            .firstNotNullOfOrNull { it.exceptionOrNull() }

        syncStateHolder.onSyncCompleted(firstError)
    }
}
