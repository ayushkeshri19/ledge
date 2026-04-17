package com.ayush.datastore.domain.usecase

import com.ayush.common.theme.ThemeMode
import com.ayush.datastore.domain.repository.AppDataStoreRepository
import javax.inject.Inject

class SetThemeModeUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}
