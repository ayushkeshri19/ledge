package com.ayush.network.domain.repository

import com.ayush.common.models.User
import com.ayush.common.result.AuthResult

interface AuthRepository {

    suspend fun signInWithGoogleIdToken(idToken: String): AuthResult<User>
    suspend fun signInWithEmail(email: String, password: String): AuthResult<User>
    suspend fun signUpWithEmail(email: String, password: String): AuthResult<User>
    suspend fun getCurrentUser(): User?
    suspend fun resetPasswordForEmail(email: String): AuthResult<Unit>
    suspend fun signOut()
}