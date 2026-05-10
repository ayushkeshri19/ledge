package com.ayush.sms.presentation.review

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SmsReviewScreen(state: SmsReviewState) {

    val viewModel = hiltViewModel<SmsReviewViewModel>()

}