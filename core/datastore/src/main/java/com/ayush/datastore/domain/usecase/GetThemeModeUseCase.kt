package com.ayush.datastore.domain.usecase

import com.ayush.common.theme.ThemeMode
import com.ayush.datastore.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeModeUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    operator fun invoke(): Flow<ThemeMode> = repository.observeThemeMode()
}
