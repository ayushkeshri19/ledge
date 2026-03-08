package com.ayush.auth.domain.usecase

import com.ayush.auth.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.IO) { authRepository.signOut() }
    }
}