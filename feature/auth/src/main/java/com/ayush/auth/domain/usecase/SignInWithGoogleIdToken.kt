package com.ayush.auth.domain.usecase

import com.ayush.auth.data.repository.AuthRepository
import com.ayush.common.models.User
import com.ayush.common.result.ApiResult
import javax.inject.Inject

class SignInWithGoogleIdToken @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(googleIdToken: String): ApiResult<User> {
        return authRepository.signInWithGoogleIdToken(googleIdToken)
    }
}
