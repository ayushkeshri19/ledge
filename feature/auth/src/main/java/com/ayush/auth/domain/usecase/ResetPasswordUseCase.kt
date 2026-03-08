package com.ayush.auth.domain.usecase

import com.ayush.auth.data.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        authRepository.resetPasswordForEmail(email)
    }
}
