package com.ayush.home.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.home.domain.models.CategorySpend
import com.ayush.home.domain.models.RecentTransaction
import com.ayush.home.domain.models.TimePeriod
import com.ayush.home.domain.usecase.GetCategorySpendingUseCase
import com.ayush.home.domain.usecase.GetDashboardSummaryUseCase
import com.ayush.home.domain.usecase.GetRecentTransactionsUseCase
import com.ayush.home.domain.usecase.HomeUserDetailsUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDetailsUseCase: HomeUserDetailsUseCase,
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase
) : BaseMviViewModel<HomeUiEvent, HomeState, HomeSideEffect>(
    initialState = HomeState()
) {

    init {
        loadUserDetails()
        loadDashboardData(showLoading = true)
    }

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.PeriodChanged -> {
                setState { copy(selectedPeriod = event.period) }
                loadDashboardData(showLoading = false)
            }

            HomeUiEvent.SeeAllTransactionsClicked -> {
                sendSideEffect(HomeSideEffect.NavigateToTransactions)
            }

            HomeUiEvent.Refresh -> {
                loadDashboardData(showLoading = false)
            }
        }
    }

    private fun loadUserDetails() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val details = userDetailsUseCase()
            if (details != null) {
                setState {
                    copy(
                        isLoading = false,
                        userDetails = UserDetailsState(
                            name = details.name,
                            initials = details.initials,
                            greeting = details.greeting,
                        ),
                    )
                }
            } else {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun loadDashboardData(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) setState { copy(isDashboardLoading = true) }
            setState { copy(isRefreshing = true) }
            val period = currentState().selectedPeriod

            val summaryDeferred = async { getDashboardSummaryUseCase(period) }
            val categorySpendingDeferred = async { getCategorySpendingUseCase(period) }
            val recentTransactionsDeferred = async { getRecentTransactionsUseCase() }

            val summary = summaryDeferred.await()
            val categorySpending = categorySpendingDeferred.await()
            val recentTransactions = recentTransactionsDeferred.await()

            setState {
                copy(
                    isDashboardLoading = false,
                    isRefreshing = false,
                    summaryState = SummaryState(
                        totalIncome = summary.totalIncome,
                        totalExpense = summary.totalExpense,
                        netBalance = summary.netBalance,
                    ),
                    categorySpending = categorySpending,
                    recentTransactions = recentTransactions,
                )
            }
        }
    }
}

@Stable
data class HomeState(
    val userDetails: UserDetailsState = UserDetailsState(),
    val isLoading: Boolean = false,
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val isDashboardLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val summaryState: SummaryState = SummaryState(),
    val categorySpending: List<CategorySpend> = emptyList(),
    val recentTransactions: List<RecentTransaction> = emptyList(),
)

@Stable
data class UserDetailsState(
    val name: String = "",
    val initials: String = "",
    val greeting: String = "",
    val hasNotification: Boolean = false
)

@Stable
data class SummaryState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
)

sealed interface HomeUiEvent {
    data class PeriodChanged(val period: TimePeriod) : HomeUiEvent
    data object SeeAllTransactionsClicked : HomeUiEvent
    data object Refresh : HomeUiEvent
}

sealed interface HomeSideEffect {
    data object NavigateToTransactions : HomeSideEffect
}