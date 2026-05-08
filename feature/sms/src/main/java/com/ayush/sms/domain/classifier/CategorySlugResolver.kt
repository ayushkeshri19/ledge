package com.ayush.sms.domain.classifier

import com.ayush.database.dao.CategoryDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorySlugResolver @Inject constructor(
    private val categoryDao: CategoryDao
) {
    private val slugToName = mapOf(
        "FOOD" to "Food & Dining",
        "TRANSPORT" to "Transport",
        "ENTERTAINMENT" to "Entertainment",
        "SHOPPING" to "Shopping",
        "HEALTH" to "Healthcare",
        "BILLS" to "Utilities"
    )

    suspend fun resolve(slug: String?): Long? {
        val name = slug?.let { slugToName[it] } ?: return null
        return categoryDao.getCategoryByName(name)?.id
    }
}
