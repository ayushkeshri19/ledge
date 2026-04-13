package com.ayush.home.domain.models

import com.ayush.common.utils.endOfDay
import com.ayush.common.utils.startOfDay
import java.util.Calendar

enum class TimePeriod(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year");

    fun dateRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (this) {
            WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.startOfDay()
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.endOfDay()
                Pair(start, cal.timeInMillis)
            }

            MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.startOfDay()
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.endOfDay()
                Pair(start, cal.timeInMillis)
            }

            YEAR -> {
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
