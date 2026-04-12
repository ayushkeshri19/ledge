package com.ayush.transactions.presentation

import com.ayush.transactions.domain.models.TransactionType
import java.util.Calendar

enum class DateRangeOption(val label: String) {
    ALL_TIME("All Time"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year");
}

data class FilterState(
    val type: TransactionType? = null,
    val dateRange: DateRangeOption = DateRangeOption.ALL_TIME,
    val categoryId: Long? = null,
    val categoryName: String? = null
) {
    val isActive: Boolean
        get() = type != null || dateRange != DateRangeOption.ALL_TIME || categoryId != null

    val activeCount: Int
        get() = listOf(
            type != null,
            dateRange != DateRangeOption.ALL_TIME,
            categoryId != null,
        ).count { it }

    fun resolvedDateRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (dateRange) {
            DateRangeOption.ALL_TIME -> Pair(0L, Long.MAX_VALUE)

            DateRangeOption.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.startOfDay()
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.endOfDay()
                Pair(start, cal.timeInMillis)
            }

            DateRangeOption.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.startOfDay()
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.endOfDay()
                Pair(start, cal.timeInMillis)
            }

            DateRangeOption.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.startOfDay()
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.endOfDay()
                Pair(start, cal.timeInMillis)
            }

            DateRangeOption.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.startOfDay()
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                cal.endOfDay()
                Pair(start, cal.timeInMillis)
            }
        }
    }
}

private fun Calendar.startOfDay() {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}

private fun Calendar.endOfDay() {
    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
}
