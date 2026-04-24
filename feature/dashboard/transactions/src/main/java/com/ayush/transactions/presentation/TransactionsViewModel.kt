package com.ayush.transactions.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.ayush.common.result.ApiResult
import com.ayush.common.sync.SyncOrchestrator
import com.ayush.common.sync.SyncState
import com.ayush.common.sync.SyncStateHolder
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.TransactionListItem
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.usecase.DeleteTransactionUseCase
import com.ayush.transactions.domain.usecase.GetCategoriesUseCase
import com.ayush.transactions.domain.usecase.GetTransactionsUseCase
import com.ayush.transactions.domain.usecase.StopRecurringSeriesUseCase
import com.ayush.transactions.domain.usecase.UpdateTransactionUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MANUAL_REFRESH_DEBOUNCE_MS = 2_000L

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val syncStateHolder: SyncStateHolder,
    private val syncOrchestrator: SyncOrchestrator,
    private val stopRecurringSeriesUseCase: StopRecurringSeriesUseCase
) : BaseMviViewModel<TransactionsEvent, TransactionsState, TransactionsSideEffect>(
    initialState = TransactionsState()
) {

    private var lastManualRefreshAt: Long = 0L

    init {
        loadCategories()
        observeSyncState()
    }

    private fun observeSyncState() {
        viewModelScope.launch {
            var wasSyncing: Boolean? = null
            syncStateHolder.state.collect { syncState ->
                val syncing = syncState is SyncState.Syncing
                setState {
                    copy(
                        isSyncing = syncing,
                        hasSyncError = syncState is SyncState.Failed
                    )
                }
                if (wasSyncing == true && !syncing) {
                    invalidatePaging()
                }
                wasSyncing = syncing
            }
        }
    }

    private fun triggerManualRefresh() {
        val now = System.currentTimeMillis()
        if (now - lastManualRefreshAt < MANUAL_REFRESH_DEBOUNCE_MS) return
        lastManualRefreshAt = now
        viewModelScope.launch { syncOrchestrator.syncAll() }
    }

    private var categoriesJob: Job? = null
    private var searchJob: Job? = null
    private val pagingTrigger = MutableStateFlow(0)

    val transactionsPagingFlow: Flow<PagingData<TransactionListItem>> =
        pagingTrigger.flatMapLatest {
            val state = currentState()
            val pagingFlow = when {
                state.searchQuery.isNotBlank() ->
                    getTransactionsUseCase.search(state.searchQuery)

                state.filterState.isActive -> {
                    val (start, end) = state.filterState.resolvedDateRange()
                    getTransactionsUseCase.filter(
                        startDate = start,
                        endDate = end,
                        type = state.filterState.type,
                        categoryId = state.filterState.categoryId,
                    )
                }

                else -> getTransactionsUseCase()
            }

            pagingFlow.map { pagingData ->
                pagingData
                    .map<_, TransactionListItem> { TransactionListItem.Item(it) }
                    .insertSeparators { before, after ->
                        val beforeDate = (before as? TransactionListItem.Item)
                            ?.transaction?.date?.let { formatDateHeader(it) }
                        val afterDate = (after as? TransactionListItem.Item)
                            ?.transaction?.date?.let { formatDateHeader(it) }

                        if (afterDate != null && afterDate != beforeDate) {
                            TransactionListItem.Header(afterDate)
                        } else {
                            null
                        }
                    }
            }
        }.cachedIn(viewModelScope)


    override fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.SearchQueryChanged -> onSearchChanged(event.query)
            is TransactionsEvent.DeleteTransaction -> deleteTransaction(event.id)
            is TransactionsEvent.UpdateTransaction -> updateTransaction(event)
            is TransactionsEvent.ApplyFilters -> {
                setState { copy(filterState = event.filterState, searchQuery = "") }
                invalidatePaging()
            }

            is TransactionsEvent.ClearFilters -> {
                setState { copy(filterState = FilterState()) }
                invalidatePaging()
            }

            is TransactionsEvent.ClearSearch -> {
                setState { copy(searchQuery = "") }
                invalidatePaging()
            }

            is TransactionsEvent.StopSeriesRequested -> {
                sendSideEffect(TransactionsSideEffect.ShowStopSeriesConfirmation(event.parentId))
            }

            is TransactionsEvent.StopSeriesConfirmed -> stopSeries(event.parentId)

            TransactionsEvent.RefreshRequested -> triggerManualRefresh()
            TransactionsEvent.DismissSyncError -> syncStateHolder.onSyncErrorDismissed()
        }
    }

    private fun stopSeries(parentId: Long) {
        viewModelScope.launch {
            stopRecurringSeriesUseCase(parentId)
            sendSideEffect(TransactionsSideEffect.ShowToast("Recurring series stopped"))
        }
    }

    private fun invalidatePaging() {
        pagingTrigger.value++
    }

    private fun loadCategories() {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                setState { copy(categories = categories) }
            }
        }
    }

    private fun onSearchChanged(query: String) {
        setState { copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            invalidatePaging()
        }
    }

    private fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            when (val result = deleteTransactionUseCase(id)) {
                is ApiResult.Success ->
                    sendSideEffect(TransactionsSideEffect.ShowToast("Transaction deleted"))

                is ApiResult.Error ->
                    sendSideEffect(TransactionsSideEffect.ShowToast(result.message))
            }
        }
    }

    private fun updateTransaction(event: TransactionsEvent.UpdateTransaction) {
        viewModelScope.launch {
            when (
                val result = updateTransactionUseCase(
                    id = event.id,
                    amount = event.amount,
                    type = event.type,
                    categoryId = event.categoryId,
                    note = event.note,
                    date = event.date,
                    isRecurring = event.isRecurring,
                    recurrenceType = event.recurrenceType,
                )
            ) {
                is ApiResult.Success ->
                    sendSideEffect(TransactionsSideEffect.ShowToast("Transaction updated"))

                is ApiResult.Error ->
                    sendSideEffect(TransactionsSideEffect.ShowToast(result.message))
            }
        }
    }
}

@Stable
data class TransactionsState(
    val categories: List<Category> = emptyList(),
    val filterState: FilterState = FilterState(),
    val searchQuery: String = "",
    val isSyncing: Boolean = false,
    val hasSyncError: Boolean = false
)

sealed interface TransactionsEvent {
    data class SearchQueryChanged(val query: String) : TransactionsEvent
    data class DeleteTransaction(val id: Long) : TransactionsEvent
    data class UpdateTransaction(
        val id: Long,
        val amount: Double,
        val type: TransactionType,
        val categoryId: Long?,
        val note: String,
        val date: Long,
        val isRecurring: Boolean,
        val recurrenceType: String?,
    ) : TransactionsEvent

    data class StopSeriesRequested(val parentId: Long) : TransactionsEvent
    data class StopSeriesConfirmed(val parentId: Long) : TransactionsEvent

    data class ApplyFilters(val filterState: FilterState) : TransactionsEvent
    data object ClearFilters : TransactionsEvent
    data object ClearSearch : TransactionsEvent
    data object RefreshRequested : TransactionsEvent
    data object DismissSyncError : TransactionsEvent
}

sealed interface TransactionsSideEffect {
    data class ShowToast(val message: String) : TransactionsSideEffect
    data class ShowStopSeriesConfirmation(val parentId: Long) : TransactionsSideEffect
}
