package com.ayush.insights.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.common.models.TimePeriod
import com.ayush.insights.domain.models.CategorySpend
import com.ayush.insights.domain.usecase.GetCategorySpendingUseCase
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
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase
) : BaseMviViewModel<InsightsEvent, InsightsState, InsightsSideEffect>(
    initialState = InsightsState()
) {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTH)

    init {
        observeCategorySpending()
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
}

@Stable
data class InsightsState(
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val isLoading: Boolean = true,
    val categorySpending: List<CategorySpend> = emptyList()
)

sealed interface InsightsEvent {
    data class PeriodChanged(val period: TimePeriod) : InsightsEvent
}

sealed interface InsightsSideEffect {

}
