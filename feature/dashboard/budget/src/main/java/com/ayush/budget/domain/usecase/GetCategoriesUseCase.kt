package com.ayush.budget.domain.usecase

import com.ayush.budget.domain.models.Category
import com.ayush.budget.domain.models.toBudgetCategory
import com.ayush.database.dao.CategoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryDao: CategoryDao
) {
    operator fun invoke(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { list -> list.map { it.toBudgetCategory() } }
}
