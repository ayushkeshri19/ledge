package com.ayush.onboarding.presentation

import androidx.lifecycle.viewModelScope
import com.ayush.datastore.domain.usecase.SetOnboardingSeenUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setOnboardingSeenUseCase: SetOnboardingSeenUseCase
) : BaseMviViewModel<OnboardingEvent, OnboardingState, OnboardingSideEffect>(OnboardingState) {

    override fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.Completed -> complete()
        }
    }

    private fun complete() {
        viewModelScope.launch {
            setOnboardingSeenUseCase()
            sendSideEffect(OnboardingSideEffect.NavigateToAuth)
        }
    }
}