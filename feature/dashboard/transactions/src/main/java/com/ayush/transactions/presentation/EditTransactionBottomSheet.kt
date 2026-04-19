package com.ayush.transactions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.RecurrenceType
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.LedgeSelectableChip
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import java.util.Calendar

private data class EditFormState(
    val amount: String,
    val note: String,
    val type: TransactionType,
    val selectedCategory: Category?,
    val dateMillis: Long,
    val hour: Int,
    val minute: Int,
    val isRecurring: Boolean,
    val recurrenceType: RecurrenceType?,
    val amountError: String? = null,
    val noteError: String? = null,
) {
    val combinedDateMillis: Long
        get() = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    companion object {
        fun from(transaction: Transaction): EditFormState {
            val cal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            return EditFormState(
                amount = formatAmountForInput(transaction.amount),
                note = transaction.note,
                type = transaction.type,
                selectedCategory = transaction.category,
                dateMillis = transaction.date,
                hour = cal.get(Calendar.HOUR_OF_DAY),
                minute = cal.get(Calendar.MINUTE),
                isRecurring = transaction.isRecurring,
                recurrenceType = transaction.recurrenceType,
            )
        }
    }
}

@Composable
internal fun EditTransactionSheet(
    transaction: Transaction,
    categories: List<Category>,
    onSave: (TransactionsEvent.UpdateTransaction) -> Unit,
    onStopSeries: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LedgeTheme.colors
    var form by remember { mutableStateOf(EditFormState.from(transaction)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "Edit Transaction", style = LedgeTextStyle.HeadingScreen, color = colors.textPrimary)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.textMuted)
            }
        }

        Spacer(Modifier.height(16.dp))

        LedgeSegmentedToggle(
            options = listOf(
                SegmentOption(TransactionType.EXPENSE, "Expense", colors.semanticRed),
                SegmentOption(TransactionType.INCOME, "Income", colors.semanticGreen),
            ),
            selectedValue = form.type,
            onSelect = { form = form.copy(type = it) },
        )

        Spacer(Modifier.height(16.dp))

        LedgeTextField(
            value = form.amount,
            onValueChange = { form = form.copy(amount = it, amountError = null) },
            label = "AMOUNT",
            placeholder = "0.00",
            isError = form.amountError != null,
            errorMessage = form.amountError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Text(text = "\u20B9", style = LedgeTextStyle.AmountMedium, color = colors.gold) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        LedgeTextField(
            value = form.note,
            onValueChange = { form = form.copy(note = it, noteError = null) },
            label = "NOTE",
            placeholder = "What was this for?",
            isError = form.noteError != null,
            errorMessage = form.noteError,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "DATE & TIME",
            style = LedgeTextStyle.Caption.copy(color = colors.textMuted2),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(colors.bgCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(text = formatDate(form.dateMillis), style = LedgeTextStyle.BodySmall, color = colors.textMuted)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(colors.bgCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = formatTime(form.hour, form.minute),
                    style = LedgeTextStyle.BodySmall,
                    color = colors.textMuted
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "CATEGORY",
            style = LedgeTextStyle.Caption.copy(color = colors.textMuted2),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                val isSelected = form.selectedCategory?.id == category.id
                LedgeSelectableChip(
                    label = category.name,
                    isSelected = isSelected,
                    onClick = { form = form.copy(selectedCategory = if (isSelected) null else category) },
                    leadingDotColor = category.color,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LedgeRadius.medium))
                .background(colors.bgCard)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(LedgeRadius.medium))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(text = "REPEATS", style = LedgeTextStyle.Caption.copy(color = colors.textMuted2))
                Text(
                    text = if (form.isRecurring) "Recurring transaction" else "One-time transaction",
                    style = LedgeTextStyle.BodySmall,
                    color = colors.textPrimary,
                )
            }
            Switch(
                checked = form.isRecurring,
                onCheckedChange = { enabled ->
                    form = form.copy(
                        isRecurring = enabled,
                        recurrenceType = if (enabled) form.recurrenceType ?: RecurrenceType.MONTHLY else null,
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.bgDeep,
                    checkedTrackColor = colors.gold,
                    uncheckedThumbColor = colors.textMuted,
                    uncheckedTrackColor = colors.bgSurface,
                    uncheckedBorderColor = colors.borderSubtle,
                ),
            )
        }

        if (form.isRecurring) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecurrenceType.entries.forEach { rt ->
                    LedgeSelectableChip(
                        label = rt.value.replaceFirstChar { it.uppercase() },
                        isSelected = form.recurrenceType == rt,
                        onClick = { form = form.copy(recurrenceType = rt) },
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = "Save Changes",
            onClick = {
                val amountValue = form.amount.toDoubleOrNull()
                if (amountValue == null || amountValue <= 0) {
                    form = form.copy(amountError = "Enter a valid amount")
                    return@LedgePrimaryButton
                }
                if (form.note.isBlank()) {
                    form = form.copy(noteError = "Add a note")
                    return@LedgePrimaryButton
                }
                onSave(
                    TransactionsEvent.UpdateTransaction(
                        id = transaction.id,
                        amount = amountValue,
                        type = form.type,
                        categoryId = form.selectedCategory?.id,
                        note = form.note.trim(),
                        date = form.combinedDateMillis,
                        isRecurring = form.isRecurring,
                        recurrenceType = form.recurrenceType?.value
                    )
                )
            },
        )

        transaction.parentId?.let { parentId ->
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { onStopSeries(parentId) },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.semanticRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Stop recurring series", style = LedgeTextStyle.Button)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
