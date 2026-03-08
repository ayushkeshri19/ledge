package com.ayush.auth.domain.usecase

import com.ayush.auth.data.repository.AuthRepository
import com.ayush.common.models.User
import com.ayush.common.result.ApiResult
import javax.inject.Inject

class SignUpWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        name: String,
        password: String
    ): ApiResult<User> {
        return authRepository.signUpWithEmail(
            email = email,
            password = password,
            name = name
        )
    }
}
