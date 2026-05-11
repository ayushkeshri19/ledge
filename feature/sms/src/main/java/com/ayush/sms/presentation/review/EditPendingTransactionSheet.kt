package com.ayush.sms.presentation.review

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.ayush.common.models.Category
import com.ayush.sms.domain.classifier.SmsCategorySlugs
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.parser.TransactionType
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.LedgeSelectableChip
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.extension.color
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditPendingTransactionSheet(
    item: PendingTransaction,
    categories: List<Category>,
    onConfirm: (SmsReviewEvent.ConfirmEdit) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = LedgeTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgSheet
    ) {
        EditFormContent(
            item = item,
            categories = categories,
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun EditFormContent(
    item: PendingTransaction,
    categories: List<Category>,
    onConfirm: (SmsReviewEvent.ConfirmEdit) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LedgeTheme.colors

    var amount by remember(item.id) { mutableStateOf(amountToInput(item.amount)) }
    var note by remember(item.id) {
        mutableStateOf(item.merchant?.takeIf { it.isNotBlank() && it != "Unknown merchant" } ?: "")
    }
    var type by remember(item.id) { mutableStateOf(item.type) }
    var selectedCategoryId by remember(item.id, categories) {
        mutableStateOf(initialCategoryId(item.suggestedCategoryId, categories))
    }
    var amountError by remember { mutableStateOf<String?>(null) }
    var noteError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Edit & confirm",
                style = LedgeTextStyle.HeadingScreen,
                color = colors.textPrimary
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.textMuted)
            }
        }

        Spacer(Modifier.height(16.dp))

        LedgeSegmentedToggle(
            options = listOf(
                SegmentOption(TransactionType.DEBIT, "Expense", colors.semanticRed),
                SegmentOption(TransactionType.CREDIT, "Income", colors.semanticGreen)
            ),
            selectedValue = type,
            onSelect = { type = it }
        )

        Spacer(Modifier.height(16.dp))

        LedgeTextField(
            value = amount,
            onValueChange = {
                amount = it
                amountError = null
            },
            label = "AMOUNT",
            placeholder = "0.00",
            isError = amountError != null,
            errorMessage = amountError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = {
                Text(text = "₹", style = LedgeTextStyle.AmountMedium, color = colors.gold)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        LedgeTextField(
            value = note,
            onValueChange = {
                note = it
                noteError = null
            },
            label = "NOTE",
            placeholder = "What was this for?",
            isError = noteError != null,
            errorMessage = noteError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        ReadOnlyDateRow(timestamp = item.smsTimestamp)

        Spacer(Modifier.height(12.dp))

        Text(
            text = "CATEGORY",
            style = LedgeTextStyle.Caption.copy(color = colors.textMuted2),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                val isSelected = selectedCategoryId == category.id
                LedgeSelectableChip(
                    label = category.name,
                    isSelected = isSelected,
                    onClick = {
                        selectedCategoryId = if (isSelected) null else category.id
                    },
                    leadingDotColor = category.color
                )
            }
        }

        if (item.rawSnippet.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "ORIGINAL SMS",
                style = LedgeTextStyle.Caption.copy(color = colors.textMuted2),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(colors.bgCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = item.rawSnippet,
                    style = LedgeTextStyle.BodySmall,
                    color = colors.textMuted
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        LedgePrimaryButton(
            text = "Confirm",
            onClick = {
                val amountValue = amount.toDoubleOrNull()
                if (amountValue == null || amountValue <= 0) {
                    amountError = "Enter a valid amount"
                    return@LedgePrimaryButton
                }
                if (note.isBlank()) {
                    noteError = "Add a note"
                    return@LedgePrimaryButton
                }
                onConfirm(
                    SmsReviewEvent.ConfirmEdit(
                        id = item.id,
                        amount = amountValue,
                        type = type,
                        categoryId = selectedCategoryId,
                        note = note.trim(),
                        date = item.smsTimestamp
                    )
                )
            }
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ReadOnlyDateRow(timestamp: Long) {
    val colors = LedgeTheme.colors
    Text(
        text = "DATE",
        style = LedgeTextStyle.Caption.copy(color = colors.textMuted2),
        modifier = Modifier.padding(bottom = 6.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LedgeRadius.medium))
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(LedgeRadius.medium))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = formatSmsDateTime(timestamp),
            style = LedgeTextStyle.BodySmall,
            color = colors.textMuted
        )
    }
}

private val sheetDateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())

private fun formatSmsDateTime(timestamp: Long): String = sheetDateFormat.format(Date(timestamp))

private fun amountToInput(amount: Double): String =
    if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()

private fun initialCategoryId(slug: String?, categories: List<Category>): Long? {
    val name = SmsCategorySlugs.nameFor(slug) ?: return null
    return categories.firstOrNull { it.name == name }?.id
}
