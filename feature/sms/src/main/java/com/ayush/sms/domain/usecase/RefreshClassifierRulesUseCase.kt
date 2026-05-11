package com.ayush.sms.domain.usecase

import com.ayush.sms.domain.repository.ClassifierRulesRepository
import javax.inject.Inject

class RefreshClassifierRulesUseCase @Inject constructor(
    private val classifierRulesRepository: ClassifierRulesRepository
) {
    suspend operator fun invoke() = classifierRulesRepository.refresh()
}