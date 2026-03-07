package com.ayush.ledge.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay

@Composable
internal fun LedgeNavGraph(
    startDestination: LedgeRoute = AuthRoute.Auth,
) {
    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<AuthRoute.Auth> {
                AuthNavGraph(
                    startDestination = startDestination,
                    onAuthSuccess = {
                        backStack.clear()
                        backStack.add(MainRoute.MainScreen)
                    },
                )
            }

            entry<MainRoute.MainScreen> {
                MainScreenStub(
                    title = "Add Transaction",
                    onNavigate = { route -> backStack.add(route) },
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

@Composable
private fun MainScreenStub(
    title: String,
    onNavigate: (LedgeRoute) -> Unit,
    onSignOut: () -> Unit,
) {
    androidx.compose.material3.Text("$title — stub")
}
