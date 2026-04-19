package com.ayush.security.domain.models

sealed interface BiometricResult {
    data object Success : BiometricResult
    data object UserCancelled : BiometricResult
    data object Failed : BiometricResult
    data class Error(val code: Int, val message: String) : BiometricResult
}