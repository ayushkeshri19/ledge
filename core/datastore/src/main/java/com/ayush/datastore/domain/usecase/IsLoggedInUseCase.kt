package com.ayush.datastore.domain.usecase

import com.ayush.datastore.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository
) {
    suspend operator fun invoke(): Boolean {
        return appDataStoreRepository.isSignedIn()
    }
}