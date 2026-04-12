package com.ayush.transactions.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
    onDismiss: () -> Unit,
) {
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
            Text(text = "Edit Transaction", style = LedgeTextStyle.HeadingScreen, color = TextPrimary)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Type toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LedgeRadius.medium))
                .background(BgDeep)
                .padding(4.dp),
        ) {
            TransactionType.entries.forEach { t ->
                val isSelected = form.type == t
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) BgCard else BgDeep,
                    animationSpec = tween(200),
                    label = "editTypeBg",
                )
                val textColor = when {
                    isSelected && t == TransactionType.EXPENSE -> SemanticRed
                    isSelected && t == TransactionType.INCOME -> SemanticGreen
                    else -> TextMuted
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(LedgeRadius.small))
                        .background(bgColor)
                        .clickable { form = form.copy(type = t) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = t.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = LedgeTextStyle.Button,
                        color = textColor,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LedgeTextField(
            value = form.amount,
            onValueChange = { form = form.copy(amount = it, amountError = null) },
            label = "AMOUNT",
            placeholder = "0.00",
            isError = form.amountError != null,
            errorMessage = form.amountError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Text(text = "\u20B9", style = LedgeTextStyle.AmountMedium, color = Gold) },
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
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(BgCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(text = formatDate(form.dateMillis), style = LedgeTextStyle.BodySmall, color = TextMuted)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(BgCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(text = formatTime(form.hour, form.minute), style = LedgeTextStyle.BodySmall, color = TextMuted)
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "CATEGORY",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                val isSelected = form.selectedCategory?.id == category.id
                val borderColor by animateColorAsState(
                    if (isSelected) Gold else BorderSubtle, tween(200), label = "editCatBorder",
                )
                val bgColor by animateColorAsState(
                    if (isSelected) GoldDim else BgCard, tween(200), label = "editCatBg",
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(LedgeRadius.pill))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(LedgeRadius.pill))
                        .clickable { form = form.copy(selectedCategory = if (isSelected) null else category) }
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
        }

        Spacer(Modifier.height(12.dp))

        // Recurring toggle
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
                Text(text = "REPEATS", style = LedgeTextStyle.Caption.copy(color = TextMuted2))
                Text(
                    text = if (form.isRecurring) "Recurring transaction" else "One-time transaction",
                    style = LedgeTextStyle.BodySmall,
                    color = TextPrimary,
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
                    checkedThumbColor = BgDeep,
                    checkedTrackColor = Gold,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = BgSurface,
                    uncheckedBorderColor = BorderSubtle,
                ),
            )
        }

        if (form.isRecurring) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecurrenceType.entries.forEach { rt ->
                    val isSelected = form.recurrenceType == rt
                    val borderColor by animateColorAsState(
                        if (isSelected) Gold else BorderSubtle, tween(200), label = "editRecurrenceBorder",
                    )
                    val bgColor by animateColorAsState(
                        if (isSelected) GoldDim else BgCard, tween(200), label = "editRecurrenceBg",
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(LedgeRadius.pill))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(LedgeRadius.pill))
                            .clickable { form = form.copy(recurrenceType = rt) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = rt.value.replaceFirstChar { it.uppercase() },
                            style = LedgeTextStyle.BodySmall,
                            color = if (isSelected) Gold else TextPrimary,
                        )
                    }
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
                        recurrenceType = form.recurrenceType?.value,
                    )
                )
            },
        )

        Spacer(Modifier.height(16.dp))
    }
}
