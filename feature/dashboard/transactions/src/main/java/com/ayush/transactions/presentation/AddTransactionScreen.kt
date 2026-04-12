package com.ayush.transactions.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.common.utils.toast
import com.ayush.transactions.domain.models.RecurrenceType
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.LedgeSelectableChip
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.theme.BgCard
import com.ayush.ui.theme.BgDeep
import com.ayush.ui.theme.BgSurface
import com.ayush.ui.theme.BorderSubtle
import com.ayush.ui.theme.Gold
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.SemanticGreen
import com.ayush.ui.theme.SemanticRed
import com.ayush.ui.theme.TextMuted
import com.ayush.ui.theme.TextMuted2
import com.ayush.ui.theme.TextPrimary

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
) {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is AddTransactionSideEffect.TransactionAdded -> {
                    "Transaction added".toast(context)
                    currentOnBack()
                }

                is AddTransactionSideEffect.ShowToast -> {
                    effect.message.toast(context)
                }
            }
        }
    }

    AddTransactionContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = currentOnBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionContent(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onBack: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .background(BgDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                )
            }
            Text(
                text = "Add Transaction",
                style = LedgeTextStyle.HeadingScreen,
                color = TextPrimary,
            )
        }

        LedgeSegmentedToggle(
            options = listOf(
                SegmentOption(TransactionType.EXPENSE, "Expense", SemanticRed),
                SegmentOption(TransactionType.INCOME, "Income", SemanticGreen),
            ),
            selectedValue = state.type,
            onSelect = { onEvent(AddTransactionEvent.TypeChanged(it)) },
            containerColor = BgSurface,
        )

        Spacer(Modifier.height(16.dp))

        LedgeTextField(
            value = state.amount,
            onValueChange = { onEvent(AddTransactionEvent.AmountChanged(it)) },
            label = "AMOUNT",
            placeholder = "0.00",
            isError = state.amountError != null,
            errorMessage = state.amountError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = {
                Text(
                    text = "\u20B9",
                    style = LedgeTextStyle.AmountMedium,
                    color = Gold,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        LedgeTextField(
            value = state.note,
            onValueChange = { onEvent(AddTransactionEvent.NoteChanged(it)) },
            label = "NOTE",
            placeholder = "What was this for?",
            isError = state.noteError != null,
            errorMessage = state.noteError,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "DATE & TIME",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(BgCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatDate(state.dateMillis, showYear = true),
                        style = LedgeTextStyle.BodySmall,
                        color = TextPrimary,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(BgCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .clickable { showTimePicker = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatTime(state.hour, state.minute),
                        style = LedgeTextStyle.BodySmall,
                        color = TextPrimary,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "CATEGORY",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.categories) { category ->
                LedgeSelectableChip(
                    label = category.name,
                    isSelected = state.selectedCategory?.id == category.id,
                    onClick = {
                        onEvent(
                            AddTransactionEvent.CategorySelected(
                                if (state.selectedCategory?.id == category.id) null else category
                            )
                        )
                    },
                    leadingDotColor = category.color,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LedgeRadius.medium))
                .background(BgCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.medium))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "REPEATS",
                    style = LedgeTextStyle.Caption.copy(color = TextMuted2),
                )
                Text(
                    text = if (state.isRecurring) "Recurring transaction" else "One-time transaction",
                    style = LedgeTextStyle.BodySmall,
                    color = TextPrimary,
                )
            }
            Switch(
                checked = state.isRecurring,
                onCheckedChange = { onEvent(AddTransactionEvent.RecurringToggled(it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BgDeep,
                    checkedTrackColor = Gold,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = BgSurface,
                    uncheckedBorderColor = BorderSubtle,
                ),
            )
        }

        AnimatedVisibility(
            visible = state.isRecurring,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecurrenceType.entries.forEach { type ->
                        LedgeSelectableChip(
                            label = type.value.replaceFirstChar { it.uppercase() },
                            isSelected = state.recurrenceType == type,
                            onClick = { onEvent(AddTransactionEvent.RecurrenceTypeChanged(type)) },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = "Add Transaction",
            onClick = { onEvent(AddTransactionEvent.Submit) },
            enabled = !state.isSubmitting,
            isLoading = state.isSubmitting,
        )

        Spacer(Modifier.height(24.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onEvent(AddTransactionEvent.DateChanged(it))
                        }
                        showDatePicker = false
                    },
                ) { Text("OK", color = Gold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.hour,
            initialMinute = state.minute,
            is24Hour = false,
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(LedgeRadius.xxl))
                    .background(BgCard)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Select Time",
                    style = LedgeTextStyle.HeadingCard,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                )
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                    TextButton(
                        onClick = {
                            onEvent(
                                AddTransactionEvent.TimeChanged(
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                )
                            )
                            showTimePicker = false
                        },
                    ) { Text("OK", color = Gold) }
                }
            }
        }
    }
}


