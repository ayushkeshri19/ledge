package com.ayush.datastore.domain.usecase

import com.ayush.datastore.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class SetBiometricsEnabledUseCase @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository
) {
    suspend operator fun invoke(enabled: Boolean) =
        appDataStoreRepository.setBiometricsEnabled(enabled)
}
