package com.ayush.home.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.home.domain.usecase.HomeUserDetailsUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDetailsUseCase: HomeUserDetailsUseCase,
) : BaseMviViewModel<HomeUiEvent, HomeState, HomeSideEffect>(
    initialState = HomeState()
) {

    init {
        loadUserDetails()
    }

    override fun onEvent(event: HomeUiEvent) {
        when (event) {
            else -> {}
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
}

@Stable
data class HomeState(
    val userDetails: UserDetailsState = UserDetailsState(),
    val isLoading: Boolean = false,
    val balanceSection: BalanceSection = BalanceSection()
)

@Stable
data class UserDetailsState(
    val name: String = "",
    val initials: String = "",
    val greeting: String = "",
    val hasNotification: Boolean = false
)

@Stable
data class BalanceSection(
    val unsettledBalance: Float? = null,
    val totalMembers: Int? = null
)

sealed interface HomeUiEvent {

}

sealed interface HomeSideEffect {

}