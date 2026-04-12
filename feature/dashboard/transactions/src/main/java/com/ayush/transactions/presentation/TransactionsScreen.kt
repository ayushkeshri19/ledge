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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.ayush.ui.theme.GreenDim
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.RedDim
import com.ayush.ui.theme.SemanticGreen
import com.ayush.ui.theme.SemanticRed
import com.ayush.ui.theme.TextMuted
import com.ayush.ui.theme.TextMuted2
import com.ayush.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen() {
    val viewModel: TransactionsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is TransactionsSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    TransactionsContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionsContent(
    state: TransactionsState,
    onEvent: (TransactionsEvent) -> Unit,
) {
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Transactions",
            style = LedgeTextStyle.HeadingScreen,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )

        LedgeTextField(
            value = state.searchQuery,
            onValueChange = { onEvent(TransactionsEvent.SearchQueryChanged(it)) },
            label = "",
            placeholder = "Search transactions...",
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = if (state.searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onEvent(TransactionsEvent.ClearSearch) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }

            state.transactions.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "No results found" else "No transactions yet",
                            style = LedgeTextStyle.HeadingCard,
                            color = TextMuted,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "Try a different search" else "Tap + to add your first transaction",
                            style = LedgeTextStyle.BodySmall,
                            color = TextMuted,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = state.transactions,
                        key = { it.id },
                    ) { transaction ->
                        SwipeableTransactionItem(
                            transaction = transaction,
                            onEdit = { transactionToEdit = transaction },
                            onDelete = { transactionToDelete = transaction },
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    transactionToDelete?.let { transaction ->
        ModalBottomSheet(
            onDismissRequest = { transactionToDelete = null },
            sheetState = deleteSheetState,
            containerColor = BgSurface,
        ) {
            DeleteConfirmationSheet(
                transaction = transaction,
                onConfirm = {
                    scope.launch {
                        deleteSheetState.hide()
                        onEvent(TransactionsEvent.DeleteTransaction(transaction.id))
                        transactionToDelete = null
                    }
                },
                onDismiss = {
                    scope.launch {
                        deleteSheetState.hide()
                        transactionToDelete = null
                    }
                },
            )
        }
    }

    transactionToEdit?.let { transaction ->
        ModalBottomSheet(
            onDismissRequest = { transactionToEdit = null },
            sheetState = editSheetState,
            containerColor = BgSurface,
        ) {
            EditTransactionSheet(
                transaction = transaction,
                categories = state.categories,
                onSave = { event ->
                    scope.launch {
                        editSheetState.hide()
                        onEvent(event)
                        transactionToEdit = null
                    }
                },
                onDismiss = {
                    scope.launch {
                        editSheetState.hide()
                        transactionToEdit = null
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false // sheet handles it — don't auto-dismiss the card
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false // snap back after opening sheet
                }

                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.35f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bgColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> SemanticRed.copy(alpha = 0.12f)
                    SwipeToDismissBoxValue.StartToEnd -> GoldDim
                    else -> Color.Transparent
                },
                animationSpec = tween(150),
                label = "swipeBg",
            )
            val iconTint by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> SemanticRed
                    SwipeToDismissBoxValue.StartToEnd -> Gold
                    else -> Color.Transparent
                },
                animationSpec = tween(150),
                label = "swipeIcon",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(bgColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                },
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = iconTint,
                        modifier = Modifier.size(22.dp),
                    )

                    SwipeToDismissBoxValue.StartToEnd -> Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = iconTint,
                        modifier = Modifier.size(22.dp),
                    )

                    else -> {}
                }
            }
        },
    ) {
        TransactionItem(transaction = transaction)
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) SemanticRed else SemanticGreen
    val amountPrefix = if (isExpense) "-" else "+"
    val indicatorColor = if (isExpense) RedDim else GreenDim

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LedgeRadius.medium))
            .background(BgCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(indicatorColor),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(transaction.category?.color ?: TextMuted),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note,
                style = LedgeTextStyle.HeadingCard,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                transaction.category?.let { cat ->
                    Text(
                        text = cat.name,
                        style = LedgeTextStyle.Caption,
                        color = TextMuted,
                    )
                    Text(
                        text = "\u00B7",
                        style = LedgeTextStyle.Caption,
                        color = TextMuted,
                    )
                }
                Text(
                    text = "${formatDate(transaction.date)}, ${formatTime(transaction.date)}",
                    style = LedgeTextStyle.Caption,
                    color = TextMuted,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountPrefix\u20B9${formatAmount(transaction.amount)}",
                style = LedgeTextStyle.AmountMono,
                color = amountColor,
            )
            if (transaction.isRecurring) {
                Text(
                    text = transaction.recurrenceType?.value?.replaceFirstChar { it.uppercase() } ?: "Recurring",
                    style = LedgeTextStyle.Caption,
                    color = Gold,
                )
            }
        }
    }
}

@Composable
private fun EditTransactionSheet(
    transaction: Transaction,
    categories: List<Category>,
    onSave: (TransactionsEvent.UpdateTransaction) -> Unit,
    onDismiss: () -> Unit,
) {
    // Local editable state — pre-filled from the transaction
    var amount by rememberSaveable { mutableStateOf(formatAmountForInput(transaction.amount)) }
    var note by rememberSaveable { mutableStateOf(transaction.note) }
    var type by rememberSaveable { mutableStateOf(transaction.type) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var isRecurring by rememberSaveable { mutableStateOf(transaction.isRecurring) }
    var recurrenceType by remember { mutableStateOf(transaction.recurrenceType) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }
    var noteError by rememberSaveable { mutableStateOf<String?>(null) }

    // Date/time split from the stored millis
    val initialCal = remember { Calendar.getInstance().apply { timeInMillis = transaction.date } }
    var dateMillis by rememberSaveable { mutableStateOf(transaction.date) }
    var hour by rememberSaveable { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var minute by rememberSaveable { mutableStateOf(initialCal.get(Calendar.MINUTE)) }

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
            Text(
                text = "Edit Transaction",
                style = LedgeTextStyle.HeadingScreen,
                color = TextPrimary,
            )
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
                val isSelected = type == t
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
                        .clickable { type = t }
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
            value = amount,
            onValueChange = { amount = it; amountError = null },
            label = "AMOUNT",
            placeholder = "0.00",
            isError = amountError != null,
            errorMessage = amountError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = {
                Text(text = "\u20B9", style = LedgeTextStyle.AmountMedium, color = Gold)
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        LedgeTextField(
            value = note,
            onValueChange = { note = it; noteError = null },
            label = "NOTE",
            placeholder = "What was this for?",
            isError = noteError != null,
            errorMessage = noteError,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "DATE & TIME",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(BgCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = formatDate(dateMillis),
                    style = LedgeTextStyle.BodySmall,
                    color = TextMuted,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(BgCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.medium))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = formatTime(hour, minute),
                    style = LedgeTextStyle.BodySmall,
                    color = TextMuted,
                )
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
                val isSelected = selectedCategory?.id == category.id
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) Gold else BorderSubtle,
                    animationSpec = tween(200),
                    label = "editCatBorder",
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) GoldDim else BgCard,
                    animationSpec = tween(200),
                    label = "editCatBg",
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(LedgeRadius.pill))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(LedgeRadius.pill))
                        .clickable {
                            selectedCategory = if (selectedCategory?.id == category.id) null else category
                        }
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
                Text(
                    text = "REPEATS",
                    style = LedgeTextStyle.Caption.copy(color = TextMuted2),
                )
                Text(
                    text = if (isRecurring) "Recurring transaction" else "One-time transaction",
                    style = LedgeTextStyle.BodySmall,
                    color = TextPrimary,
                )
            }
            Switch(
                checked = isRecurring,
                onCheckedChange = {
                    isRecurring = it
                    recurrenceType = if (it) recurrenceType ?: RecurrenceType.MONTHLY else null
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

        if (isRecurring) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecurrenceType.entries.forEach { rt ->
                    val isSelected = recurrenceType == rt
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) Gold else BorderSubtle,
                        animationSpec = tween(200),
                        label = "editRecurrenceBorder",
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) GoldDim else BgCard,
                        animationSpec = tween(200),
                        label = "editRecurrenceBg",
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(LedgeRadius.pill))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(LedgeRadius.pill))
                            .clickable { recurrenceType = rt }
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
                val amountValue = amount.toDoubleOrNull()
                if (amountValue == null || amountValue <= 0) {
                    amountError = "Enter a valid amount"
                    return@LedgePrimaryButton
                }
                if (note.isBlank()) {
                    noteError = "Add a note"
                    return@LedgePrimaryButton
                }
                val combinedDate = Calendar.getInstance().apply {
                    timeInMillis = dateMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                onSave(
                    TransactionsEvent.UpdateTransaction(
                        id = transaction.id,
                        amount = amountValue,
                        type = type,
                        categoryId = selectedCategory?.id,
                        note = note.trim(),
                        date = combinedDate,
                        isRecurring = isRecurring,
                        recurrenceType = recurrenceType?.value,
                    )
                )
            },
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DeleteConfirmationSheet(
    transaction: Transaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountPrefix = if (isExpense) "-" else "+"
    val amountColor = if (isExpense) SemanticRed else SemanticGreen

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Delete Transaction",
            style = LedgeTextStyle.HeadingScreen,
            color = TextPrimary,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "This action cannot be undone.",
            style = LedgeTextStyle.BodySmall,
            color = TextMuted,
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LedgeRadius.medium))
                .background(BgCard)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note,
                    style = LedgeTextStyle.HeadingCard,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${formatDate(transaction.date)}, ${formatTime(transaction.date)}",
                    style = LedgeTextStyle.Caption,
                    color = TextMuted,
                )
            }
            Text(
                text = "$amountPrefix\u20B9${formatAmount(transaction.amount)}",
                style = LedgeTextStyle.AmountMono,
                color = amountColor,
            )
        }

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(color = BgCard)

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextMuted),
            ) {
                Text(text = "Cancel", style = LedgeTextStyle.Button)
            }

            Spacer(Modifier.width(8.dp))

            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = SemanticRed),
            ) {
                Text(text = "Delete", style = LedgeTextStyle.Button)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(millis))

private fun formatTime(millis: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))

private fun formatTime(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
}

private fun formatAmount(amount: Double): String =
    if (amount == amount.toLong().toDouble()) {
        String.format(Locale.getDefault(), "%,d", amount.toLong())
    } else {
        String.format(Locale.getDefault(), "%,.2f", amount)
    }

private fun formatAmountForInput(amount: Double): String =
    if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        amount.toString()
    }
