package com.ayush.transactions.domain.usecase

import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<Category>> = repository.getAllCategories()
}
