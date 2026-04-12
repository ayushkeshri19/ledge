package com.ayush.auth.domain.usecase

import com.ayush.auth.data.repository.AuthRepository
import com.ayush.database.LedgeDatabase
import com.ayush.datastore.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val database: LedgeDatabase,
    private val dataStore: AppDataStoreRepository
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
            dataStore.clearData()
            authRepository.signOut()
        }
    }
}
