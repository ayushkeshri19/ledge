package com.ayush.transactions.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.common.result.ApiResult
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.usecase.AddTransactionUseCase
import com.ayush.transactions.domain.usecase.GetCategoriesUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : BaseMviViewModel<AddTransactionEvent, AddTransactionState, AddTransactionSideEffect>(
    initialState = AddTransactionState()
) {

    init {
        loadCategories()
    }

    override fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.AmountChanged -> {
                setState { copy(amount = event.amount, amountError = null) }
            }

            is AddTransactionEvent.NoteChanged -> {
                setState { copy(note = event.note, noteError = null) }
            }

            is AddTransactionEvent.TypeChanged -> {
                setState { copy(type = event.type) }
            }

            is AddTransactionEvent.CategorySelected -> {
                setState { copy(selectedCategory = event.category) }
            }

            is AddTransactionEvent.DateChanged -> {
                setState { copy(dateMillis = event.dateMillis) }
            }
            is AddTransactionEvent.TimeChanged -> {
                setState { copy(hour = event.hour, minute = event.minute) }
            }

            is AddTransactionEvent.Submit -> submitTransaction()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                setState { copy(categories = categories) }
            }
        }
    }

    private fun submitTransaction() {
        val state = currentState()

        val amountValue = state.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            setState { copy(amountError = "Enter a valid amount") }
            return
        }
        if (state.note.isBlank()) {
            setState { copy(noteError = "Add a note") }
            return
        }

        viewModelScope.launch {
            setState { copy(isSubmitting = true) }
            val result = addTransactionUseCase(
                amount = amountValue,
                type = state.type,
                categoryId = state.selectedCategory?.id,
                note = state.note.trim(),
                date = state.combinedDateTimeMillis(),
            )
            when (result) {
                is ApiResult.Success -> {
                    sendSideEffect(AddTransactionSideEffect.TransactionAdded)
                }

                is ApiResult.Error -> {
                    setState { copy(isSubmitting = false) }
                    sendSideEffect(AddTransactionSideEffect.ShowToast(result.message))
                }
            }
        }
    }
}

@Stable
data class AddTransactionState(
    val amount: String = "",
    val note: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val hour: Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
    val minute: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE),
    val categories: List<Category> = emptyList(),
    val isSubmitting: Boolean = false,
    val amountError: String? = null,
    val noteError: String? = null,
) {
    fun combinedDateTimeMillis(): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}

sealed interface AddTransactionEvent {
    data class AmountChanged(val amount: String) : AddTransactionEvent
    data class NoteChanged(val note: String) : AddTransactionEvent
    data class TypeChanged(val type: TransactionType) : AddTransactionEvent
    data class CategorySelected(val category: Category?) : AddTransactionEvent
    data class DateChanged(val dateMillis: Long) : AddTransactionEvent
    data class TimeChanged(val hour: Int, val minute: Int) : AddTransactionEvent
    data object Submit : AddTransactionEvent
}

sealed interface AddTransactionSideEffect {
    data object TransactionAdded : AddTransactionSideEffect
    data class ShowToast(val message: String) : AddTransactionSideEffect
}
