package com.ayush.onboarding.presentation

sealed interface OnboardingEvent {
    data object Completed : OnboardingEvent
}

data object OnboardingState

sealed interface OnboardingSideEffect {
    data object NavigateToAuth : OnboardingSideEffect
}
