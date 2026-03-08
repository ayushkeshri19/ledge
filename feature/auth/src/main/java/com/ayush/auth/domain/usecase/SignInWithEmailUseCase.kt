package com.ayush.auth.domain.usecase

import com.ayush.auth.data.repository.AuthRepository
import com.ayush.common.models.User
import com.ayush.common.result.ApiResult
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ApiResult<User> {
        return authRepository.signInWithEmail(
            email = email,
            password = password
        )
    }
}
