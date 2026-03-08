package com.ayush.auth.domain.usecase

enum class AuthFlow { SIGN_IN, SIGN_UP }

sealed interface AuthEligibilityResult {
    data object Nothing : AuthEligibilityResult

    data object Eligible : AuthEligibilityResult

    data class Error(val message: String) : AuthEligibilityResult
}

fun AuthEligibilityResult.emailError(): String? =
    (this as? AuthEligibilityResult.Error)
        ?.message
        ?.takeIf { it.contains("email", ignoreCase = true) }

fun AuthEligibilityResult.passwordError(): String? =
    (this as? AuthEligibilityResult.Error)
        ?.message
        ?.takeIf { it.contains("password", ignoreCase = true)
                || it.contains("uppercase", ignoreCase = true)
                || it.contains("special",   ignoreCase = true)
                || it.contains("character", ignoreCase = true) }

class AuthEligibilityUseCase {

    private val specialCharRegex = Regex("[^A-Za-z0-9]")

    fun canProceed(
        email: String,
        password: String,
        authFlow: AuthFlow,
    ): AuthEligibilityResult {

        if (email.isBlank()) {
            return AuthEligibilityResult.Error("Email cannot be empty")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AuthEligibilityResult.Error("Invalid email format")
        }
        if (authFlow == AuthFlow.SIGN_UP) {
            if (password.isBlank()) {
                return AuthEligibilityResult.Error("Password cannot be empty")
            }
            if (password.length < 8) {
                return AuthEligibilityResult.Error(
                    "Password must be at least 8 characters long"
                )
            }
            if (!password.any { it.isUpperCase() }) {
                return AuthEligibilityResult.Error(
                    "Password must contain at least one uppercase letter"
                )
            }
            if (!specialCharRegex.containsMatchIn(password)) {
                return AuthEligibilityResult.Error(
                    "Password must contain at least one special character"
                )
            }
        }

        return AuthEligibilityResult.Eligible
    }
}
