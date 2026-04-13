package com.ayush.home.data.repository

import com.ayush.common.models.User
import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.CategorySpendTuple
import com.ayush.database.data.TransactionWithCategory
import com.ayush.home.domain.repository.HomeRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val transactionDao: TransactionDao
) : HomeRepository {

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        supabaseClient.auth.currentUserOrNull()?.let { currentUser ->
            val metadata = currentUser.userMetadata
            User(
                id = currentUser.id,
                email = currentUser.email ?: "",
                fullName = metadata?.get("full_name")?.toString()?.trim('"') ?: "User",
                avatarUrl = metadata?.get("avatar_url")?.toString()?.trim('"')?.takeIf { it != "null" },
                isEmailVerified = currentUser.emailConfirmedAt != null,
            )
        }
    }

    override suspend fun getTotalIncome(startDate: Long, endDate: Long): Double {
        return withContext(Dispatchers.IO) {
            transactionDao.getTotalByTypeAndDateRange(
                type = INCOME,
                startDate = startDate,
                endDate = endDate
            ) ?: 0.0
        }
    }

    override suspend fun getTotalExpense(startDate: Long, endDate: Long): Double {
        return withContext(Dispatchers.IO) {
            transactionDao.getTotalByTypeAndDateRange(
                type = EXPENSE,
                startDate = startDate,
                endDate = endDate
            ) ?: 0.0
        }
    }

    override suspend fun getExpensesByCategory(
        startDate: Long,
        endDate: Long
    ): List<CategorySpendTuple> {
        return withContext(Dispatchers.IO) {
            transactionDao.getExpensesByCategory(startDate, endDate)
        }
    }

    override suspend fun getRecentTransactions(limit: Int): List<TransactionWithCategory> {
        return withContext(Dispatchers.IO) {
            transactionDao.getRecentTransactions(limit)
        }
    }

    companion object {
        const val INCOME: String = "income"
        const val EXPENSE: String = "expense"
    }
}
