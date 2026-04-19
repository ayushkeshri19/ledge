package com.ayush.profile.presentation.profile

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.profile.presentation.components.AccountDetailsRow
import com.ayush.profile.presentation.components.BiometricToggleRow
import com.ayush.profile.presentation.components.ProfileTopBar
import com.ayush.profile.presentation.components.SectionCard
import com.ayush.profile.presentation.components.SectionHeader
import com.ayush.profile.presentation.components.SignOut
import com.ayush.profile.presentation.components.ThemeModeRow
import com.ayush.security.data.di.BiometricAuthenticatorEntryPoint
import com.ayush.security.domain.models.BiometricResult
import com.ayush.ui.theme.LedgeTheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

private val LocalEventSink = staticCompositionLocalOf<(ProfileEvent) -> Unit> { error { } }

@Composable
fun UserProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val authenticator = remember(context) {
        EntryPointAccessors
            .fromApplication(context.applicationContext, BiometricAuthenticatorEntryPoint::class.java)
            .biometricAuthenticator()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onEvent(ProfileEvent.Resumed)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CompositionLocalProvider(LocalEventSink provides viewModel::onEvent) {
        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect { effect ->
                when (effect) {
                    is ProfileSideEffect.RequestBiometricAuth -> {
                        val title = if (effect.intendedEnable) "Enable biometric lock" else "Disable biometric lock"
                        scope.launch {
                            val result = authenticator.authenticate(activity, title)
                            viewModel.onEvent(
                                ProfileEvent.BiometricAuthResult(
                                    success = result is BiometricResult.Success,
                                    intendedEnable = effect.intendedEnable
                                )
                            )
                        }
                    }

                    ProfileSideEffect.OpenBiometricEnrollment -> {
                        val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                            )
                        }
                        runCatching { activity.startActivity(enrollIntent) }
                            .onFailure {
                                activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                            }
                    }
                }
            }
        }

        ProfileContent(state = state, onBack = onBack, onSignOut = onSignOut)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileContent(
    state: ProfileState,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val onEvent = LocalEventSink.current
    val colors = LedgeTheme.colors

    Scaffold(
        topBar = { ProfileTopBar(onBack = onBack) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            stickyHeader {
                SectionHeader(title = "APPEARANCE", background = colors.bgDeep)
            }
            item {
                SectionCard {
                    ThemeModeRow(
                        selected = state.themeMode,
                        onSelect = { onEvent(ProfileEvent.ThemeModeChanged(it)) }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            stickyHeader {
                SectionHeader(title = "APP LOCK", background = colors.bgDeep)
            }
            item {
                SectionCard {
                    BiometricToggleRow(
                        status = state.biometricStatus,
                        enabled = state.biometricEnabled,
                        onToggle = { onEvent(ProfileEvent.BiometricToggleRequested(it)) },
                        onEnrollClick = { onEvent(ProfileEvent.EnrollmentRequested) }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            stickyHeader {
                SectionHeader(title = "ACCOUNT", background = colors.bgDeep)
            }
            item {
                SectionCard {
                    AccountDetailsRow { }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                SignOut(onSignOut = onSignOut)
            }
        }
    }
}
