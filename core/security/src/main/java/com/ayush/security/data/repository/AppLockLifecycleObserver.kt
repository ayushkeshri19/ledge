package com.ayush.security.data.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ayush.security.domain.repository.AppLockManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockLifecycleObserver @Inject constructor(
    private val appLockManager: AppLockManager
) : DefaultLifecycleObserver {
    fun register() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        appLockManager.onAppBackgrounded()
    }

    override fun onStart(owner: LifecycleOwner) {
        appLockManager.onAppForegrounded()
    }
}