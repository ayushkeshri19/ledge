package com.ayush.datastore.domain.usecase

import com.ayush.datastore.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHasSeenOnboardingUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.observeHasSeenOnboarding()
}