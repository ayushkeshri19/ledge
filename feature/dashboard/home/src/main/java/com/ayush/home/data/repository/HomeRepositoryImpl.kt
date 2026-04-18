package com.ayush.home.data.repository

import com.ayush.common.models.User
import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.TransactionWithCategory
import com.ayush.home.domain.repository.HomeRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val transactionDao: TransactionDao,
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

    override fun observeTotalIncome(startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.observeTotalByTypeAndDateRange(INCOME, startDate, endDate)
            .map { it ?: 0.0 }

    override fun observeTotalExpense(startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.observeTotalByTypeAndDateRange(EXPENSE, startDate, endDate)
            .map { it ?: 0.0 }

    override fun observeRecentTransactions(limit: Int): Flow<List<TransactionWithCategory>> =
        transactionDao.observeRecentTransactions(limit)

    companion object {
        const val INCOME: String = "income"
        const val EXPENSE: String = "expense"
    }
}
