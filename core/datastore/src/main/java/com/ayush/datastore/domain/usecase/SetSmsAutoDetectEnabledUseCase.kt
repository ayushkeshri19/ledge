package com.ayush.datastore.domain.usecase

import com.ayush.datastore.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class SetSmsAutoDetectEnabledUseCase @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository
) {
    suspend operator fun invoke(enabled: Boolean) =
        appDataStoreRepository.setSmsAutoDetectEnabled(enabled)
}
