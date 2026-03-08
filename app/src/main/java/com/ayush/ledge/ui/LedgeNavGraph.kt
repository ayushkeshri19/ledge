package com.ayush.ledge.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ayush.common.auth.AuthState

@Composable
internal fun LedgeNavGraph(
    rootViewModel: RootViewModel = hiltViewModel(),
) {
    val authState by rootViewModel.authState.collectAsState()

    if (authState == AuthState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination: LedgeRoute = when (authState) {
        AuthState.Authenticated -> MainRoute.MainScreen
        else -> AuthRoute.Auth
    }

    val backStack = rememberNavBackStack(startDestination)

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Authenticated -> {
                if (backStack.lastOrNull() !is MainRoute) {
                    backStack.clear()
                    backStack.add(MainRoute.MainScreen)
                }
            }

            AuthState.NotAuthenticated -> {
                if (backStack.lastOrNull() !is AuthRoute) {
                    backStack.clear()
                    backStack.add(AuthRoute.Auth)
                }
            }

            else -> {}
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<AuthRoute.Auth> {
                AuthNavGraph(
                    onAuthSuccess = {
                        backStack.clear()
                        backStack.add(MainRoute.MainScreen)
                    },
                )
            }

            entry<MainRoute.MainScreen> {
                MainNavGraph(
                    onSignOut = {
                        backStack.clear()
                        backStack.add(AuthRoute.Auth)
                    },
                )
            }
        },
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    )
}