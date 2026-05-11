package com.ayush.ledge.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ayush.auth.presentation.SetNewPasswordScreen
import com.ayush.common.auth.AuthState
import com.ayush.common.auth.RecoveryState
import com.ayush.onboarding.presentation.OnboardingScreen
import com.ayush.profile.presentation.profile.UserProfileScreen
import com.ayush.sms.presentation.review.SmsReviewScreen

@Composable
internal fun LedgeNavGraph(mainViewModel: MainViewModel) {

    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.authState == AuthState.Loading || uiState.recoveryState == RecoveryState.Loading || uiState.hasSeenOnboarding == null) {
        return
    }

    val startDestination: LedgeRoute = when {
        uiState.recoveryState == RecoveryState.Active -> AuthRoute.SetNewPassword
        uiState.authState == AuthState.Authenticated -> DashboardRoute.Dashboard
        uiState.hasSeenOnboarding == false -> AuthRoute.Onboarding
        else -> AuthRoute.Auth
    }

    val backStack = rememberNavBackStack(startDestination)

    var hasAutoNavigatedToReview by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.authState, uiState.pendingReviewCount, hasAutoNavigatedToReview) {
        if (!hasAutoNavigatedToReview &&
            uiState.authState == AuthState.Authenticated &&
            uiState.pendingReviewCount > 0 &&
            backStack.lastOrNull() != LedgeRoute.SmsReview
        ) {
            hasAutoNavigatedToReview = true
            backStack.add(LedgeRoute.SmsReview)
        }
    }

    LaunchedEffect(uiState.authState, uiState.recoveryState) {
        when {
            uiState.recoveryState == RecoveryState.Active -> {
                if (backStack.lastOrNull() !is AuthRoute.SetNewPassword) {
                    backStack.clear()
                    backStack.add(AuthRoute.SetNewPassword)
                }
            }

            uiState.authState == AuthState.Authenticated -> {
                if (backStack.lastOrNull() !is DashboardRoute) {
                    backStack.clear()
                    backStack.add(DashboardRoute.Dashboard)
                }
            }

            uiState.authState == AuthState.NotAuthenticated -> {
                if (backStack.lastOrNull() !is AuthRoute) {
                    backStack.clear()
                    backStack.add(AuthRoute.Auth)
                }
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<AuthRoute.Auth> {
                AuthNavGraph(
                    onAuthSuccess = {
                        backStack.clear()
                        backStack.add(DashboardRoute.Dashboard)
                    }
                )
            }

            entry<AuthRoute.SetNewPassword> {
                SetNewPasswordScreen(
                    onComplete = {
                        backStack.clear()
                        backStack.add(AuthRoute.Auth)
                    }
                )
            }

            entry<DashboardRoute.Dashboard> {
                DashboardNavGraph(
                    onSignOut = {
                        backStack.clear()
                        backStack.add(AuthRoute.Auth)
                        mainViewModel.onEvent(MainEvent.SignOut)
                    },
                    onNavigateToProfile = {
                        backStack.add(LedgeRoute.Profile)
                    },
                    onNavigateToSmsReview = {
                        if (backStack.lastOrNull() != LedgeRoute.SmsReview) {
                            backStack.add(LedgeRoute.SmsReview)
                        }
                    },
                    pendingReviewCount = uiState.pendingReviewCount
                )
            }

            entry<LedgeRoute.Profile> {
                UserProfileScreen(
                    onBack = { backStack.remove(LedgeRoute.Profile) },
                    onSignOut = {
                        backStack.clear()
                        backStack.add(AuthRoute.Auth)
                        mainViewModel.onEvent(MainEvent.SignOut)
                    }
                )
            }

            entry<AuthRoute.Onboarding> {
                OnboardingScreen {
                    backStack.clear()
                    backStack.add(AuthRoute.Auth)
                }
            }

            entry<LedgeRoute.SmsReview> {
                SmsReviewScreen(
                    onBack = { backStack.remove(LedgeRoute.SmsReview) }
                )
            }
        },
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    )
}
