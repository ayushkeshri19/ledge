package com.ayush.datastore.domain.usecase

import com.ayush.datastore.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSmsAutoDetectEnabledUseCase @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository
) {
    operator fun invoke(): Flow<Boolean> =
        appDataStoreRepository.observeSmsAutoDetectEnabled()
}
