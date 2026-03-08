package com.ayush.auth.domain.usecase

import com.ayush.auth.data.repository.AuthRepository
import com.ayush.common.result.ApiResult
import javax.inject.Inject

class SendPasswordResetEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): ApiResult<Unit> {
        return authRepository.resetPasswordForEmail(email)
    }
}
