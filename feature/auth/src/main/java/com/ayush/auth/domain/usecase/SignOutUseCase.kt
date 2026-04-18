package com.ayush.auth.domain.usecase

import androidx.work.WorkManager
import com.ayush.auth.data.repository.AuthRepository
import com.ayush.common.utils.Workers
import com.ayush.database.LedgeDatabase
import com.ayush.datastore.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val database: LedgeDatabase,
    private val dataStore: AppDataStoreRepository,
    private val workManager: WorkManager,
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            workManager.cancelUniqueWork(Workers.TRANSACTION_SYNC)
            database.clearAllTables()
            dataStore.clearUserData()
            authRepository.signOut()
        }
    }
}
