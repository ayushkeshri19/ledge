package com.ayush.ledge.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface LedgeRoute : NavKey

sealed interface AuthRoute : LedgeRoute {
    @Serializable data object Auth : AuthRoute
    @Serializable data object SignIn : AuthRoute
    @Serializable data object SignUp : AuthRoute
    @Serializable data object ForgotPassword : AuthRoute
}

sealed interface MainRoute : LedgeRoute {
    @Serializable data object MainScreen : MainRoute
    @Serializable
    data object Home : MainRoute
    @Serializable
    data object Transactions : MainRoute
    @Serializable
    data object Budget : MainRoute
    @Serializable
    data object More : MainRoute
}