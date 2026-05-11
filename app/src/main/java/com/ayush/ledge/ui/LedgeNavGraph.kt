package com.ayush.ledge.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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

    val authState by mainViewModel.authState.collectAsState()
    val recoveryState by mainViewModel.recoveryState.collectAsState()
    val hasSeenOnboarding by mainViewModel.hasSeenOnboarding.collectAsStateWithLifecycle()

    if (authState == AuthState.Loading || recoveryState == RecoveryState.Loading || hasSeenOnboarding == null) {
        return
    }

    val startDestination: LedgeRoute = when {
        recoveryState == RecoveryState.Active -> AuthRoute.SetNewPassword
        authState == AuthState.Authenticated -> DashboardRoute.Dashboard
        hasSeenOnboarding == false -> AuthRoute.Onboarding
        else -> AuthRoute.Auth
    }

    val backStack = rememberNavBackStack(startDestination)

    val context = LocalContext.current

    val pendingReviewCount by mainViewModel.pendingReviewCount.collectAsStateWithLifecycle()
    var hasAutoNavigatedToReview by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(authState, pendingReviewCount, hasAutoNavigatedToReview) {
        if (!hasAutoNavigatedToReview &&
            authState == AuthState.Authenticated &&
            pendingReviewCount > 0 &&
            backStack.lastOrNull() != LedgeRoute.SmsReview
        ) {
            hasAutoNavigatedToReview = true
            backStack.add(LedgeRoute.SmsReview)
        }
    }

    LaunchedEffect(authState, recoveryState) {
        when {
            recoveryState == RecoveryState.Active -> {
                if (backStack.lastOrNull() !is AuthRoute.SetNewPassword) {
                    backStack.clear()
                    backStack.add(AuthRoute.SetNewPassword)
                }
            }

            authState == AuthState.Authenticated -> {
                if (backStack.lastOrNull() !is DashboardRoute) {
                    backStack.clear()
                    backStack.add(DashboardRoute.Dashboard)
                }
            }

            authState == AuthState.NotAuthenticated -> {
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
                        mainViewModel.signOut()
                    },
                    onNavigateToProfile = {
                        backStack.add(LedgeRoute.Profile)
                    },
                    onNavigateToSmsReview = {
                        if (backStack.lastOrNull() != LedgeRoute.SmsReview) {
                            backStack.add(LedgeRoute.SmsReview)
                        }
                    },
                    pendingReviewCount = pendingReviewCount
                )
            }

            entry<LedgeRoute.Profile> {
                UserProfileScreen(
                    onBack = { backStack.remove(LedgeRoute.Profile) },
                    onSignOut = {
                        backStack.clear()
                        backStack.add(AuthRoute.Auth)
                        mainViewModel.signOut()
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
                    onBack = { backStack.remove(LedgeRoute.SmsReview) },
                    onEditPending = {
                        Toast.makeText(context, "Edit coming soon", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        },
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    )
}
