package com.ayush.sms.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.sms.domain.model.SmsPermissionStatus

@Composable
fun SmsPermissionRationaleScreen(
    onComplete: () -> Unit
) {
    val viewModel = hiltViewModel<SmsPermissionViewModel>()
    val context = LocalContext.current
    val activity = context as Activity
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        viewModel.onEvent(SmsPermissionEvent.PermissionResult(activity, granted))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(SmsPermissionEvent.Resumed(activity))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                SmsPermissionSideEffect.RequestPermission -> {
                    launcher.launch(
                        arrayOf(
                            Manifest.permission.READ_SMS,
                            Manifest.permission.RECEIVE_SMS
                        )
                    )
                }

                SmsPermissionSideEffect.OpenAppSettings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    }
                    runCatching { activity.startActivity(intent) }
                }

                SmsPermissionSideEffect.Complete -> onComplete()
            }
        }
    }

    SmsPermissionContent(
        status = state.status,
        onAllow = { viewModel.onEvent(SmsPermissionEvent.AllowClicked(activity)) },
        onOpenSettings = { viewModel.onEvent(SmsPermissionEvent.OpenAppSettingsClicked) },
        onSkip = { viewModel.onEvent(SmsPermissionEvent.SkipClicked) }
    )
}

@Composable
private fun SmsPermissionContent(
    status: SmsPermissionStatus,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Auto-detect transactions")
            Spacer(Modifier.height(12.dp))
            Text(text = bodyCopyFor(status))
            Spacer(Modifier.height(32.dp))

            when (status) {
                SmsPermissionStatus.PermanentlyDenied -> {
                    Button(onClick = onOpenSettings) { Text("Open settings") }
                }

                else -> {
                    Button(onClick = onAllow) { Text("Allow") }
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onSkip) { Text("Not now") }
        }
    }
}

private fun bodyCopyFor(status: SmsPermissionStatus): String = when (status) {
    SmsPermissionStatus.NotAsked,
    SmsPermissionStatus.Granted ->
        "Ledge reads incoming bank SMS to auto-detect transactions. " +
                "Nothing is sent to any server."

    SmsPermissionStatus.Denied ->
        "We need SMS access to detect transactions automatically. " +
                "Without it, you can still add transactions manually."

    SmsPermissionStatus.PermanentlyDenied ->
        "SMS permission is blocked. Open settings to grant it manually, " +
                "or skip — you can always enable this later."
}