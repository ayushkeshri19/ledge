package com.ayush.home.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.common.auth.AuthStateProvider
import com.ayush.common.utils.observeAuthState
import com.ayush.home.domain.models.CategorySpend
import com.ayush.home.domain.models.RecentTransaction
import com.ayush.home.domain.models.TimePeriod
import com.ayush.home.domain.usecase.GetCategorySpendingUseCase
import com.ayush.home.domain.usecase.GetDashboardSummaryUseCase
import com.ayush.home.domain.usecase.GetRecentTransactionsUseCase
import com.ayush.home.domain.usecase.HomeUserDetailsUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDetailsUseCase: HomeUserDetailsUseCase,
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getCategorySpendingUseCase: GetCategorySpendingUseCase,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase,
    private val authStateProvider: AuthStateProvider,
) : BaseMviViewModel<HomeUiEvent, HomeState, HomeSideEffect>(
    initialState = HomeState()
) {

    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTH)

    init {
        observeAuthState(authStateProvider) { loadUserDetails() }
        observeDashboardData()
    }

    @OptIn(FlowPreview::class)
    private fun observeDashboardData() {
        viewModelScope.launch {
            _selectedPeriod.flatMapLatest { period ->
                combine(
                    getDashboardSummaryUseCase(period),
                    getCategorySpendingUseCase(period),
                    getRecentTransactionsUseCase(),
                ) { summary, spending, recent ->
                    Triple(summary, spending, recent)
                }.debounce(100)
            }.collect { (summary, spending, recent) ->
                setState {
                    copy(
                        isDashboardLoading = false,
                        summaryState = SummaryState(
                            totalIncome = summary.totalIncome,
                            totalExpense = summary.totalExpense,
                            netBalance = summary.netBalance,
                        ),
                        categorySpending = spending,
                        recentTransactions = recent,
                    )
                }
            }
        }
    }

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.PeriodChanged -> {
                setState { copy(selectedPeriod = event.period) }
                _selectedPeriod.value = event.period
            }

            HomeUiEvent.SeeAllTransactionsClicked -> {
                sendSideEffect(HomeSideEffect.NavigateToTransactions)
            }

            HomeUiEvent.ProfileClicked -> {
                sendSideEffect(HomeSideEffect.NavigateToProfile)
            }
        }
    }

    private fun loadUserDetails() {
        viewModelScope.launch {
            val details = userDetailsUseCase()
            if (details != null) {
                setState {
                    copy(
                        userDetails = UserDetailsState(
                            name = details.name,
                            initials = details.initials,
                            greeting = details.greeting,
                        ),
                    )
                }
            }
        }
    }
}

@Stable
data class HomeState(
    val userDetails: UserDetailsState = UserDetailsState(),
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val isDashboardLoading: Boolean = true,
    val showDot: Boolean = false,
    val summaryState: SummaryState = SummaryState(),
    val categorySpending: List<CategorySpend> = emptyList(),
    val recentTransactions: List<RecentTransaction> = emptyList(),
)

@Stable
data class UserDetailsState(
    val name: String = "",
    val initials: String = "",
    val greeting: String = "",
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
    data object ProfileClicked : HomeUiEvent
}

sealed interface HomeSideEffect {
    data object NavigateToTransactions : HomeSideEffect
    data object NavigateToProfile : HomeSideEffect

}
