package com.ayush.common.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import java.text.NumberFormat
import java.util.Locale
import android.R as AndroidR

class BudgetNotificationHelper(
    private val context: Context,
) {
    companion object {
        private const val CHANNEL_ID = "budget_alerts"
        private const val CHANNEL_NAME = "Budget Alerts"
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Alerts when you approach or exceed your budget"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun notifyWarning(
        budgetId: Long,
        categoryName: String?,
        thresholdPercent: Int,
        spent: Double,
        limit: Double,
    ) {
        val label = categoryName ?: "monthly"
        val title = "Budget Warning"
        val body = "You've used ${(spent / limit * 100).toInt()}% of your $label budget " +
                "(${formatCurrency(spent)} / ${formatCurrency(limit)})"

        notify(budgetId.toInt(), title, body)
    }

    fun notifyExceeded(
        budgetId: Long,
        categoryName: String?,
        overBy: Double,
        limit: Double,
    ) {
        val label = categoryName ?: "monthly"
        val title = "Budget Exceeded"
        val body = "You've exceeded your $label budget by ${formatCurrency(overBy)}"

        notify(budgetId.toInt() + 10000, title, body)
    }

    private fun notify(id: Int, title: String, body: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(AndroidR.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }
}
