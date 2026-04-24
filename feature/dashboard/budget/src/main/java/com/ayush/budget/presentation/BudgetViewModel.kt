package com.ayush.budget.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.budget.domain.models.BudgetWithSpent
import com.ayush.budget.domain.models.Category
import com.ayush.budget.domain.usecase.DeleteBudgetUseCase
import com.ayush.budget.domain.usecase.GetBudgetsUseCase
import com.ayush.budget.domain.usecase.GetCategoriesUseCase
import com.ayush.budget.domain.usecase.SaveBudgetUseCase
import com.ayush.common.sync.SyncOrchestrator
import com.ayush.common.sync.SyncState
import com.ayush.common.sync.SyncStateHolder
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MANUAL_REFRESH_DEBOUNCE_MS = 2_000L

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val saveBudgetUseCase: SaveBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val syncStateHolder: SyncStateHolder,
    private val syncOrchestrator: SyncOrchestrator
) : BaseMviViewModel<BudgetEvent, BudgetState, BudgetSideEffect>(
    initialState = BudgetState()
) {

    private var budgetsJob: Job? = null
    private var categoriesJob: Job? = null
    private var lastManualRefreshAt: Long = 0L

    init {
        observeBudgets()
        loadCategories()
    }

    private fun observeBudgets() {
        budgetsJob?.cancel()
        budgetsJob = viewModelScope.launch {
            combine(
                getBudgetsUseCase(),
                syncStateHolder.state,
            ) { budgets, syncState -> budgets to syncState }
                .collect { (budgets, syncState) ->
                    val overall = budgets.find { it.budget.categoryId == null }
                    val categoryBudgets = budgets.filter { it.budget.categoryId != null }
                    setState {
                        copy(
                            isLoading = syncState is SyncState.Syncing,
                            hasSyncError = syncState is SyncState.Failed,
                            overallBudget = overall,
                            categoryBudgets = categoryBudgets
                        )
                    }
                }
        }
    }

    private fun loadCategories() {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                setState { copy(categories = categories) }
            }
        }
    }

    override fun onEvent(event: BudgetEvent) {
        when (event) {
            BudgetEvent.ShowAddSheet -> {
                setState { copy(showSheet = true, editingBudget = null) }
            }

            is BudgetEvent.ShowEditSheet -> {
                setState { copy(showSheet = true, editingBudget = event.budget) }
            }

            BudgetEvent.DismissSheet -> {
                setState { copy(showSheet = false, editingBudget = null) }
            }

            is BudgetEvent.SaveBudget -> saveBudget(event)
            is BudgetEvent.DeleteBudget -> deleteBudget(event.id)
            BudgetEvent.RefreshRequested -> triggerManualRefresh()
            BudgetEvent.DismissSyncError -> syncStateHolder.onSyncErrorDismissed()
        }
    }

    private fun triggerManualRefresh() {
        val now = System.currentTimeMillis()
        if (now - lastManualRefreshAt < MANUAL_REFRESH_DEBOUNCE_MS) return
        lastManualRefreshAt = now
        viewModelScope.launch { syncOrchestrator.syncAll() }
    }

    private fun saveBudget(event: BudgetEvent.SaveBudget) {
        viewModelScope.launch {
            val result = saveBudgetUseCase(
                categoryId = event.categoryId,
                amount = event.amount,
                warningThreshold = event.warningThreshold,
            )
            result.fold(
                onSuccess = {
                    setState { copy(showSheet = false, editingBudget = null) }
                    sendSideEffect(BudgetSideEffect.ShowToast("Budget saved"))
                },
                onFailure = {
                    sendSideEffect(BudgetSideEffect.ShowToast(it.message ?: "Failed to save"))
                },
            )
        }
    }

    private fun deleteBudget(id: Long) {
        viewModelScope.launch {
            deleteBudgetUseCase(id)
            setState { copy(showSheet = false, editingBudget = null) }
            sendSideEffect(BudgetSideEffect.ShowToast("Budget deleted"))
        }
    }
}

@Stable
data class BudgetState(
    val isLoading: Boolean = true,
    val hasSyncError: Boolean = false,
    val overallBudget: BudgetWithSpent? = null,
    val categoryBudgets: List<BudgetWithSpent> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showSheet: Boolean = false,
    val editingBudget: BudgetWithSpent? = null,
)

sealed interface BudgetEvent {
    data object ShowAddSheet : BudgetEvent
    data class ShowEditSheet(val budget: BudgetWithSpent) : BudgetEvent
    data object DismissSheet : BudgetEvent
    data class SaveBudget(
        val categoryId: Long?,
        val amount: Double,
        val warningThreshold: Int,
    ) : BudgetEvent

    data class DeleteBudget(val id: Long) : BudgetEvent
    data object RefreshRequested : BudgetEvent
    data object DismissSyncError : BudgetEvent
}

sealed interface BudgetSideEffect {
    data class ShowToast(val message: String) : BudgetSideEffect
}
