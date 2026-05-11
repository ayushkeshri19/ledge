package com.ayush.sms.domain.classifier

import com.ayush.database.dao.CategoryDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorySlugResolver @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun resolve(slug: String?): Long? {
        val name = SmsCategorySlugs.nameFor(slug) ?: return null
        return categoryDao.getCategoryByName(name)?.id
    }
}
