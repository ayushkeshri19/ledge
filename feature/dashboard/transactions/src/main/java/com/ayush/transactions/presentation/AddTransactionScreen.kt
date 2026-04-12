package com.ayush.transactions.presentation

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.theme.BgCard
import com.ayush.ui.theme.BgDeep
import com.ayush.ui.theme.BgSurface
import com.ayush.ui.theme.BorderSubtle
import com.ayush.ui.theme.Gold
import com.ayush.ui.theme.GoldDim
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.SemanticGreen
import com.ayush.ui.theme.SemanticRed
import com.ayush.ui.theme.TextMuted
import com.ayush.ui.theme.TextMuted2
import com.ayush.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
) {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is AddTransactionSideEffect.TransactionAdded -> {
                    Toast.makeText(context, "Transaction added", Toast.LENGTH_SHORT).show()
                    onBack()
                }

                is AddTransactionSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AddTransactionContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddTransactionContent(
    state: AddTransactionState,
    onEvent: (AddTransactionEvent) -> Unit,
    onBack: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
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

        // Type toggle
        TypeToggle(
            selected = state.type,
            onTypeChanged = { onEvent(AddTransactionEvent.TypeChanged(it)) },
        )

        Spacer(Modifier.height(24.dp))

        // Amount
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

        Spacer(Modifier.height(16.dp))

        // Note
        LedgeTextField(
            value = state.note,
            onValueChange = { onEvent(AddTransactionEvent.NoteChanged(it)) },
            label = "NOTE",
            placeholder = "What was this for?",
            isError = state.noteError != null,
            errorMessage = state.noteError,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        // Date
        Text(
            text = "DATE",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LedgeRadius.medium))
                .background(BgCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.medium))
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = formatDate(state.dateMillis),
                    style = LedgeTextStyle.Body,
                    color = TextPrimary,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Categories
        Text(
            text = "CATEGORY",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 10.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            state.categories.forEach { category ->
                CategoryChip(
                    category = category,
                    isSelected = state.selectedCategory?.id == category.id,
                    onClick = {
                        onEvent(
                            AddTransactionEvent.CategorySelected(
                                if (state.selectedCategory?.id == category.id) null else category
                            )
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Submit
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
}

@Composable
private fun TypeToggle(
    selected: TransactionType,
    onTypeChanged: (TransactionType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LedgeRadius.medium))
            .background(BgSurface)
            .padding(4.dp),
    ) {
        TransactionType.entries.forEach { type ->
            val isSelected = selected == type
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) BgCard else BgSurface,
                animationSpec = tween(200),
                label = "typeBg",
            )
            val textColor = when {
                isSelected && type == TransactionType.EXPENSE -> SemanticRed
                isSelected && type == TransactionType.INCOME -> SemanticGreen
                else -> TextMuted
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.small))
                    .background(bgColor)
                    .clickable { onTypeChanged(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = type.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = LedgeTextStyle.Button,
                    color = textColor,
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Gold else BorderSubtle,
        animationSpec = tween(200),
        label = "chipBorder",
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) GoldDim else BgCard,
        animationSpec = tween(200),
        label = "chipBg",
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(LedgeRadius.pill))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(LedgeRadius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(category.color),
        )
        Text(
            text = category.name,
            style = LedgeTextStyle.BodySmall,
            color = if (isSelected) Gold else TextPrimary,
        )
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}
