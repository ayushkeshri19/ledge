package com.ayush.transactions.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.common.result.ApiResult
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.repository.TransactionRepository
import com.ayush.transactions.domain.usecase.DeleteTransactionUseCase
import com.ayush.transactions.domain.usecase.GetCategoriesUseCase
import com.ayush.transactions.domain.usecase.GetTransactionsUseCase
import com.ayush.transactions.domain.usecase.UpdateTransactionUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val repository: TransactionRepository,
) : BaseMviViewModel<TransactionsEvent, TransactionsState, TransactionsSideEffect>(
    initialState = TransactionsState()
) {

    private var searchJob: Job? = null

    init {
        ensureCategories()
        loadTransactions()
        loadCategories()
        syncFromRemote()
    }

    override fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.SearchQueryChanged -> onSearchChanged(event.query)
            is TransactionsEvent.DeleteTransaction -> deleteTransaction(event.id)
            is TransactionsEvent.UpdateTransaction -> updateTransaction(event)
            is TransactionsEvent.ClearSearch -> {
                setState { copy(searchQuery = "") }
                loadTransactions()
            }
        }
    }

    private fun ensureCategories() {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            getTransactionsUseCase().collect { transactions ->
                setState {
                    copy(
                        transactions = transactions,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
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
            if (query.isBlank()) {
                loadTransactions()
            } else {
                getTransactionsUseCase.search(query).collect { results ->
                    setState { copy(transactions = results) }
                }
            }
        }
    }

    private fun syncFromRemote() {
        viewModelScope.launch {
            repository.syncFromRemote()
        }
    }

    private fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            when (val result = deleteTransactionUseCase(id)) {
                is ApiResult.Success -> {
                    sendSideEffect(TransactionsSideEffect.ShowToast("Transaction deleted"))
                }

                is ApiResult.Error -> {
                    sendSideEffect(TransactionsSideEffect.ShowToast(result.message))
                }
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
                is ApiResult.Success -> {
                    sendSideEffect(TransactionsSideEffect.ShowToast("Transaction updated"))
                }

                is ApiResult.Error -> {
                    sendSideEffect(TransactionsSideEffect.ShowToast(result.message))
                }
            }
        }
    }
}

@Stable
data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
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
    data object ClearSearch : TransactionsEvent
}

sealed interface TransactionsSideEffect {
    data class ShowToast(val message: String) : TransactionsSideEffect
}
