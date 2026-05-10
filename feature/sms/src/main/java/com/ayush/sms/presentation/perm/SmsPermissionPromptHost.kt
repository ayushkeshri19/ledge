package com.ayush.sms.presentation.perm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SmsPermissionPromptHost() {
    val viewModel: SmsPermissionPromptHostViewModel = hiltViewModel()
    val shouldShow by viewModel.shouldShow.collectAsStateWithLifecycle()

    if (shouldShow == true) {
        SmsPermissionDialog(onDismiss = {})
    }
}
