package com.ayush.insights.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.common.models.TimePeriod
import com.ayush.insights.domain.models.CategorySpend
import com.ayush.insights.domain.models.IncomeExpenseBucket
import com.ayush.insights.domain.models.SpendBucket
import com.ayush.insights.domain.usecase.GetCategorySpendingUseCase
import com.ayush.insights.domain.usecase.GetIncomeExpenseHistoryUseCase
import com.ayush.insights.domain.usecase.GetSpendTimeSeriesUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val getIncomeExpenseHistoryUseCase: GetIncomeExpenseHistoryUseCase,
    private val getSpendTimeSeriesUseCase: GetSpendTimeSeriesUseCase
) : BaseMviViewModel<InsightsEvent, InsightsState, InsightsSideEffect>(
    initialState = InsightsState()
) {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTH)

    init {
        observeCategorySpending()
        observeSpendSeries()
        observeWeeklyPace()
        observeIncomeExpenseHistory()
    }

    override fun onEvent(event: InsightsEvent) {
        when (event) {
            is InsightsEvent.PeriodChanged -> {
                setState { copy(selectedPeriod = event.period) }
                _selectedPeriod.value = event.period
            }
        }
    }

    private fun observeCategorySpending() {
        viewModelScope.launch {
            _selectedPeriod
                .flatMapLatest { period -> getCategorySpendingUseCase(period) }
                .collect { spending ->
                    setState {
                        copy(
                            isLoading = false,
                            categorySpending = spending
                        )
                    }
                }
        }
    }

    private fun observeSpendSeries() {
        viewModelScope.launch {
            _selectedPeriod
                .flatMapLatest { period -> getSpendTimeSeriesUseCase(period) }
                .collect { series ->
                    setState { copy(spendSeries = series) }
                }
        }
    }

    private fun observeWeeklyPace() {
        viewModelScope.launch {
            getSpendTimeSeriesUseCase(TimePeriod.MONTH).collect { series ->
                setState { copy(weeklyPace = series) }
            }
        }
    }

    private fun observeIncomeExpenseHistory() {
        viewModelScope.launch {
            getIncomeExpenseHistoryUseCase(monthsBack = 6).collect { history ->
                setState { copy(incomeExpenseHistory = history) }
            }
        }
    }
}

@Stable
data class InsightsState(
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val isLoading: Boolean = true,
    val categorySpending: List<CategorySpend> = emptyList(),
    val spendSeries: List<SpendBucket> = emptyList(),
    val weeklyPace: List<SpendBucket> = emptyList(),
    val incomeExpenseHistory: List<IncomeExpenseBucket> = emptyList()
)

sealed interface InsightsEvent {
    data class PeriodChanged(val period: TimePeriod) : InsightsEvent
}

sealed interface InsightsSideEffect {

}
