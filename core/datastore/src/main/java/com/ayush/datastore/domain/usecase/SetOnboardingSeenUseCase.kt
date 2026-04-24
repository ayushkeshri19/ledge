package com.ayush.datastore.domain.usecase

import com.ayush.datastore.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class SetOnboardingSeenUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke() = repository.setOnboardingSeen()
}