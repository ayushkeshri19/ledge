package com.ayush.ledge.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ayush.auth.presentation.AuthViewModel
import com.ayush.auth.presentation.ResetPasswordScreen
import com.ayush.auth.presentation.SignInScreen
import com.ayush.auth.presentation.SignUpScreen

@Composable
internal fun AuthNavGraph(
    onAuthSuccess: () -> Unit,
) {
    val viewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<AuthViewModel>()
    val backStack = rememberNavBackStack(AuthRoute.SignUp)

    fun popAuth() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun swapTo(route: AuthRoute) {
        backStack[backStack.lastIndex] = route
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<AuthRoute.SignIn> {
                SignInScreen(
                    viewModel = viewModel,
                    onAuthSuccess = onAuthSuccess,
                    onNavigateToSignUp = { swapTo(AuthRoute.SignUp) },
                    onForgotPassword = { backStack.add(AuthRoute.ForgotPassword) },
                )
            }

            entry<AuthRoute.SignUp> {
                SignUpScreen(
                    viewModel = viewModel,
                    onAuthSuccess = onAuthSuccess,
                    onNavigateToSignIn = { swapTo(AuthRoute.SignIn) },
                )
            }

            entry<AuthRoute.ForgotPassword> {
                ResetPasswordScreen(::popAuth)
            }
        },
        onBack = ::popAuth
    )
}
