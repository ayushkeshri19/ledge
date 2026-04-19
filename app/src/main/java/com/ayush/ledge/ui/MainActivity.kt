package com.ayush.ledge.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ayush.common.auth.AuthStateProvider
import com.ayush.common.deeplink.DeepLinkHandler
import com.ayush.security.domain.repository.AppLockManager
import com.ayush.security.domain.repository.BiometricAuthenticator
import com.ayush.ui.theme.LedgeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler

    @Inject
    lateinit var authStateProvider: AuthStateProvider

    @Inject
    lateinit var biometricAuthenticator: BiometricAuthenticator

    @Inject
    lateinit var appLockManager: AppLockManager

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            appLockManager.locked.collect { isLocked ->
                if (isLocked) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        deepLinkHandler.handle(intent)
        requestNotificationPermission()
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()

            val isLocked by appLockManager.locked.collectAsStateWithLifecycle()

            LedgeTheme(themeMode = themeMode) {
                LedgeNavGraph(mainViewModel = mainViewModel)

                if (isLocked) {
                    LockScreen(
                        biometricAuthenticator = biometricAuthenticator,
                        onUnlock = { appLockManager.unlock() },
                        onSignOut = ::disableBiometricAndSignOut
                    )
                }
            }
        }
    }

    private fun disableBiometricAndSignOut() {
        appLockManager.onBiometricDisabled()
        with(mainViewModel) {
            disableBiometric()
            signOut()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { authStateProvider.validateSession() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkHandler.handle(intent)
    }
}
