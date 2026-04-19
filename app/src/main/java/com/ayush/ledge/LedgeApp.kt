package com.ayush.ledge

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ayush.common.utils.Workers
import com.ayush.security.data.repository.AppLockLifecycleObserver
import com.ayush.transactions.data.sync.RecurringTransactionWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltAndroidApp
class LedgeApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appLockLifecycleObserver: AppLockLifecycleObserver

    @Inject
    lateinit var workManager: WorkManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupTimber()
        appLockLifecycleObserver.register()

        scheduleRecurringTransactionWorker()
    }

    private fun scheduleRecurringTransactionWorker() {
        val request = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        ).build()

        workManager.enqueueUniquePeriodicWork(
            Workers.RECURRING_TRANSACTION,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}