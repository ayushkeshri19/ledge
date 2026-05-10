package com.ayush.sms.presentation.perm

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.theme.LedgeTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsPermissionDialog(
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel<SmsPermissionViewModel>()
    val context = LocalContext.current
    val activity = context as Activity
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                SmsPermissionSideEffect.Dismiss -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onEvent(SmsPermissionEvent.SkipClicked) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Auto-detect transactions", style = LedgeTextStyle.HeadingScreen)
            Text(text = bodyCopyFor(state.status))
            Spacer(Modifier.height(16.dp))

            when (state.status) {
                SmsPermissionStatus.PermanentlyDenied -> {
                    LedgePrimaryButton(
                        text = "Open settings",
                        onClick = { viewModel.onEvent(SmsPermissionEvent.OpenAppSettingsClicked) }
                    )
                }
                else -> {
                    LedgePrimaryButton(
                        text = "Allow",
                        onClick = { viewModel.onEvent(SmsPermissionEvent.AllowClicked(activity)) }
                    )
                }
            }

            TextButton(
                onClick = { viewModel.onEvent(SmsPermissionEvent.SkipClicked) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Not now") }

            Spacer(Modifier.height(8.dp))
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
