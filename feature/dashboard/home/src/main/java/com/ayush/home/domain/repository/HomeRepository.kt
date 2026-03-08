package com.ayush.home.domain.repository

import com.ayush.common.models.User

interface HomeRepository {
    suspend fun getCurrentUser(): User?
}
