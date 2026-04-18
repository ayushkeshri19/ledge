package com.ayush.insights.utils

import com.ayush.common.utils.endOfDay
import com.ayush.common.utils.startOfDay
import com.ayush.database.data.TransactionWithCategory
import com.ayush.insights.domain.models.IncomeExpenseBucket
import com.ayush.insights.domain.models.SpendBucket
import java.util.Calendar
import kotlin.math.ceil

/**
 * 7 buckets, Monday → Sunday.
 *
 * Calendar.DAY_OF_WEEK is 1-indexed with Sunday = 1, Monday = 2, …, Saturday = 7.
 * I want zero-based indexing such that Monday = 0, Sunday = 6 so the labels read left-to-right as weekdays first.
 * The mapping `(dow + 5) % 7` does that:
 *   Sun(1) → 6, Mon(2) → 0, Tue(3) → 1, … Sat(7) → 5.
 */
internal fun bucketSpendByDayOfWeek(txns: List<TransactionWithCategory>): List<SpendBucket> {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val sums = DoubleArray(7)

    val cal = Calendar.getInstance()
    txns.forEach { t ->
        cal.timeInMillis = t.transaction.date
        val idx = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
        sums[idx] += t.transaction.amount
    }

    return labels.mapIndexed { i, label -> SpendBucket(label, sums[i]) }
}

/**
 * Variable number of buckets based on the month's length (4 for Feb, 5 for 29–31 day months).
 *
 * "Week of month" means: days 1–7 → W1, 8–14 → W2, 15–21 → W3, 22–28 → W4, 29–31 → W5.
 * This is a fixed 7-day stride from the 1st of the month — NOT Calendar.WEEK_OF_MONTH, which
 * depends on which day of the week the month starts on (so the first Monday is always W2)
 * and would give misleading results for "how far into the month were you".
 *
 * `startDate` is the first-of-month in millis (TimePeriod.MONTH.dateRange() returns that);
 * we use it to get `getActualMaximum(DAY_OF_MONTH)` so we emit the right bucket count.
 */
internal fun bucketSpendByWeekOfMonth(
    txns: List<TransactionWithCategory>,
    startDate: Long,
): List<SpendBucket> {
    val cal = Calendar.getInstance().apply { timeInMillis = startDate }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val bucketCount = ceil(daysInMonth / 7.0).toInt()    // 4 or 5

    val sums = DoubleArray(bucketCount)
    txns.forEach { t ->
        cal.timeInMillis = t.transaction.date
        val weekIdx = (cal.get(Calendar.DAY_OF_MONTH) - 1) / 7
        if (weekIdx in sums.indices) sums[weekIdx] += t.transaction.amount
    }

    return (0 until bucketCount).map { i ->
        val first = i * 7 + 1
        val last = minOf((i + 1) * 7, daysInMonth)
        SpendBucket(label = "$first\u2013$last", amount = sums[i])
    }
}

/**
 * 12 buckets, Jan → Dec.
 *
 * Calendar.MONTH is 0-indexed (Jan = 0, Dec = 11), which lines up directly with array indices.
 * Any transaction outside Jan–Dec of the range's year is a bug upstream; we don't guard against it.
 */
internal fun bucketSpendByMonth(txns: List<TransactionWithCategory>): List<SpendBucket> {
    val labels = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
    val sums = DoubleArray(12)

    val cal = Calendar.getInstance()
    txns.forEach { t ->
        cal.timeInMillis = t.transaction.date
        sums[cal.get(Calendar.MONTH)] += t.transaction.amount
    }

    return labels.mapIndexed { i, label -> SpendBucket(label, sums[i]) }
}

/**
 * Emits exactly [monthsBack] chronologically-ordered buckets ending with the current month,
 * each carrying separate income and expense totals.
 *
 * Bucket index is derived via `year * 12 + month` arithmetic — monotonically increasing
 * across any year boundary, so Dec → Jan crossings need no special case. Transactions
 * falling outside the window are silently dropped (shouldn't happen if the upstream range
 * matches, but it's defensive).
 */
internal fun bucketIncomeExpenseByMonth(
    txns: List<TransactionWithCategory>,
    monthsBack: Int,
): List<IncomeExpenseBucket> {
    val monthLabels = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )

    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, -(monthsBack - 1))
    }
    val originKey = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)

    data class Acc(val label: String, var income: Double = 0.0, var expense: Double = 0.0)

    val buckets = Array(monthsBack) {
        val acc = Acc(monthLabels[cal.get(Calendar.MONTH)])
        cal.add(Calendar.MONTH, 1)
        acc
    }

    val txnCal = Calendar.getInstance()
    txns.forEach { t ->
        txnCal.timeInMillis = t.transaction.date
        val idx = (txnCal.get(Calendar.YEAR) * 12 + txnCal.get(Calendar.MONTH)) - originKey
        if (idx in buckets.indices) {
            when (t.transaction.type) {
                "income" -> buckets[idx].income += t.transaction.amount
                "expense" -> buckets[idx].expense += t.transaction.amount
            }
        }
    }

    return buckets.map { IncomeExpenseBucket(it.label, it.income, it.expense) }
}

internal fun computeRange(monthsBack: Int): Pair<Long, Long> {
    require(monthsBack >= 1) { "monthsBack must be >= 1, got $monthsBack" }

    val endCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        endOfDay()
    }

    val startCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, -(monthsBack - 1))
        startOfDay()
    }
    return startCal.timeInMillis to endCal.timeInMillis
}
