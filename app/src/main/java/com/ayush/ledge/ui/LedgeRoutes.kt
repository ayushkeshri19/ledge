package com.ayush.ledge.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface LedgeRoute : NavKey {
    @Serializable
    data object Profile : LedgeRoute
}

sealed interface AuthRoute : LedgeRoute {
    @Serializable data object Auth : AuthRoute

    @Serializable data object SignIn : AuthRoute
    @Serializable data object SignUp : AuthRoute
    @Serializable data object ForgotPassword : AuthRoute
}

sealed interface DashboardRoute : LedgeRoute {
    @Serializable
    data object Dashboard : DashboardRoute

    @Serializable
    data object Home : DashboardRoute
    @Serializable
    data object Transactions : DashboardRoute
    @Serializable
    data object Budget : DashboardRoute
    @Serializable
    data object More : DashboardRoute

    @Serializable
    data object AddTransaction : DashboardRoute
}