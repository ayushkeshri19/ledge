package com.ayush.auth.data.repository

import com.ayush.common.models.User
import com.ayush.common.result.ApiResult

interface AuthRepository {

    suspend fun signInWithGoogleIdToken(idToken: String, rawNonce: String): ApiResult<User>
    suspend fun signInWithEmail(email: String, password: String): ApiResult<User>
    suspend fun signUpWithEmail(email: String, password: String, name: String): ApiResult<User>
    suspend fun getCurrentUser(): User?
    suspend fun resetPasswordForEmail(email: String): ApiResult<Unit>
    suspend fun updatePassword(newPassword: String): ApiResult<Unit>
    suspend fun signOut()
}
