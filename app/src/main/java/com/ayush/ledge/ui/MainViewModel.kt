package com.ayush.ledge.ui

import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ayush.auth.domain.usecase.SignOutUseCase
import com.ayush.common.auth.AuthState
import com.ayush.common.auth.AuthStateProvider
import com.ayush.common.auth.PasswordRecoveryStateHolder
import com.ayush.common.auth.RecoveryState
import com.ayush.common.sync.SyncOrchestrator
import com.ayush.common.theme.ThemeMode
import com.ayush.common.transactions.PendingReviewCountSource
import com.ayush.common.utils.Workers
import com.ayush.datastore.domain.usecase.GetThemeModeUseCase
import com.ayush.datastore.domain.usecase.ObserveHasSeenOnboardingUseCase
import com.ayush.datastore.domain.usecase.SetBiometricsEnabledUseCase
import com.ayush.sms.domain.usecase.RefreshClassifierRulesUseCase
import com.ayush.transactions.data.sync.RecurringTransactionWorker
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainState(
    val authState: AuthState = AuthState.Loading,
    val recoveryState: RecoveryState = RecoveryState.Loading,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hasSeenOnboarding: Boolean? = null,
    val pendingReviewCount: Int = 0
)

sealed interface MainEvent {
    data object SignOut : MainEvent
    data object DisableBiometric : MainEvent
}

sealed interface MainSideEffect

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authStateProvider: AuthStateProvider,
    private val signOutUseCase: SignOutUseCase,
    private val setBiometricsEnabledUseCase: SetBiometricsEnabledUseCase,
    private val syncOrchestrator: SyncOrchestrator,
    private val workManager: WorkManager,
    observeHasSeenOnboardingUseCase: ObserveHasSeenOnboardingUseCase,
    getThemeModeUseCase: GetThemeModeUseCase,
    passwordRecoveryStateHolder: PasswordRecoveryStateHolder,
    pendingReviewCountSource: PendingReviewCountSource,
    refreshClassifierRulesUseCase: RefreshClassifierRulesUseCase
) : BaseMviViewModel<MainEvent, MainState, MainSideEffect>(MainState()) {

    init {
        viewModelScope.launch { refreshClassifierRulesUseCase() }

        viewModelScope.launch {
            var syncedForSession = false
            authStateProvider.authState.collect { authState ->
                setState { copy(authState = authState) }
                when (authState) {
                    AuthState.Authenticated -> {
                        if (!syncedForSession) {
                            syncedForSession = true
                            launch {
                                syncOrchestrator.syncAll()
                                workManager.enqueueUniqueWork(
                                    Workers.RECURRING_TRANSACTION_IMMEDIATE,
                                    ExistingWorkPolicy.KEEP,
                                    OneTimeWorkRequestBuilder<RecurringTransactionWorker>().build()
                                )
                            }
                        }
                    }
                    AuthState.NotAuthenticated -> syncedForSession = false
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            passwordRecoveryStateHolder.state.collect { recoveryState ->
                setState { copy(recoveryState = recoveryState) }
            }
        }

        viewModelScope.launch {
            getThemeModeUseCase().collect { themeMode ->
                setState { copy(themeMode = themeMode) }
            }
        }

        viewModelScope.launch {
            observeHasSeenOnboardingUseCase().collect { hasSeenOnboarding ->
                setState { copy(hasSeenOnboarding = hasSeenOnboarding) }
            }
        }

        viewModelScope.launch {
            pendingReviewCountSource.observe().collect { count ->
                setState { copy(pendingReviewCount = count) }
            }
        }
    }

    override fun onEvent(event: MainEvent) {
        when (event) {
            MainEvent.SignOut -> viewModelScope.launch { signOutUseCase.invoke() }
            MainEvent.DisableBiometric -> viewModelScope.launch { setBiometricsEnabledUseCase(false) }
        }
    }
}
