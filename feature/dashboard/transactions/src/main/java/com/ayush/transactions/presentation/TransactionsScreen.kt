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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import java.util.Calendar

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
    var showFilterSheet by remember { mutableStateOf(false) }
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Transactions",
                style = LedgeTextStyle.HeadingScreen,
                color = TextPrimary,
            )
            Box {
                IconButton(onClick = { showFilterSheet = true }) {
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = "Filter",
                        tint = if (state.filterState.isActive) Gold else TextMuted,
                        modifier = Modifier.size(22.dp),
                    )
                }

                if (state.filterState.isActive) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Gold)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = state.filterState.activeCount.toString(),
                            style = LedgeTextStyle.Caption.copy(color = BgDeep),
                        )
                    }
                }
            }
        }

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

        if (state.filterState.isActive) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.filterState.type?.let { type ->
                    item {
                        ActiveFilterChip(
                            label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            onRemove = {
                                onEvent(TransactionsEvent.ApplyFilters(state.filterState.copy(type = null)))
                            },
                        )
                    }
                }
                if (state.filterState.dateRange != DateRangeOption.ALL_TIME) {
                    item {
                        ActiveFilterChip(
                            label = state.filterState.dateRange.label,
                            onRemove = {
                                onEvent(TransactionsEvent.ApplyFilters(state.filterState.copy(dateRange = DateRangeOption.ALL_TIME)))
                            },
                        )
                    }
                }
                state.filterState.categoryName?.let { name ->
                    item {
                        ActiveFilterChip(
                            label = name,
                            onRemove = {
                                onEvent(
                                    TransactionsEvent.ApplyFilters(
                                        state.filterState.copy(categoryId = null, categoryName = null)
                                    )
                                )
                            },
                        )
                    }
                }
                if (state.filterState.activeCount > 1) {
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(LedgeRadius.pill))
                                .background(BgSurface)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.pill))
                                .clickable { onEvent(TransactionsEvent.ClearFilters) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "Clear all",
                                style = LedgeTextStyle.Caption,
                                color = TextMuted,
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(Modifier.height(16.dp))
        }

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
                        val heading = when {
                            state.searchQuery.isNotEmpty() -> "No results found"
                            state.filterState.isActive -> "No matching transactions"
                            else -> "No transactions yet"
                        }
                        val sub = when {
                            state.searchQuery.isNotEmpty() -> "Try a different search"
                            state.filterState.isActive -> "Try adjusting your filters"
                            else -> "Tap + to add your first transaction"
                        }
                        Text(text = heading, style = LedgeTextStyle.HeadingCard, color = TextMuted)
                        Spacer(Modifier.height(8.dp))
                        Text(text = sub, style = LedgeTextStyle.BodySmall, color = TextMuted)
                        if (state.filterState.isActive) {
                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(LedgeRadius.pill))
                                    .background(BgCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(LedgeRadius.pill))
                                    .clickable { onEvent(TransactionsEvent.ClearFilters) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Text(text = "Clear filters", style = LedgeTextStyle.BodySmall, color = Gold)
                            }
                        }
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

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState,
            containerColor = BgSurface,
        ) {
            FilterSheet(
                currentFilters = state.filterState,
                categories = state.categories,
                onApply = { newFilters ->
                    scope.launch {
                        filterSheetState.hide()
                        onEvent(TransactionsEvent.ApplyFilters(newFilters))
                        showFilterSheet = false
                    }
                },
                onDismiss = {
                    scope.launch {
                        filterSheetState.hide()
                        showFilterSheet = false
                    }
                },
            )
        }
    }
}

@Composable
private fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(LedgeRadius.pill))
            .background(GoldDim)
            .border(1.dp, Gold, RoundedCornerShape(LedgeRadius.pill))
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = LedgeTextStyle.Caption, color = Gold)
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(18.dp),
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove filter",
                tint = Gold,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun FilterSheet(
    currentFilters: FilterState,
    categories: List<Category>,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit,
) {
    var tempFilters by remember { mutableStateOf(currentFilters) }

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
            Text(text = "Filters", style = LedgeTextStyle.HeadingScreen, color = TextPrimary)
            TextButton(
                onClick = { tempFilters = FilterState() },
                enabled = tempFilters.isActive,
            ) {
                Text(
                    text = "Reset",
                    style = LedgeTextStyle.Button,
                    color = if (tempFilters.isActive) SemanticRed else TextMuted,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "TYPE",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LedgeRadius.medium))
                .background(BgDeep)
                .padding(4.dp),
        ) {
            val allSelected = tempFilters.type == null
            val allBg by animateColorAsState(
                if (allSelected) BgCard else BgDeep, tween(200), label = "allBg",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.small))
                    .background(allBg)
                    .clickable { tempFilters = tempFilters.copy(type = null) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "All",
                    style = LedgeTextStyle.Button,
                    color = if (allSelected) TextPrimary else TextMuted,
                )
            }
            TransactionType.entries.forEach { type ->
                val isSelected = tempFilters.type == type
                val bg by animateColorAsState(
                    if (isSelected) BgCard else BgDeep, tween(200), label = "typeBg${type.name}",
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
                        .background(bg)
                        .clickable { tempFilters = tempFilters.copy(type = type) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = LedgeTextStyle.Button,
                        color = textColor,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "DATE RANGE",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 10.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DateRangeOption.entries) { option ->
                val isSelected = tempFilters.dateRange == option
                val borderColor by animateColorAsState(
                    if (isSelected) Gold else BorderSubtle, tween(200), label = "dateChipBorder",
                )
                val bgColor by animateColorAsState(
                    if (isSelected) GoldDim else BgCard, tween(200), label = "dateChipBg",
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(LedgeRadius.pill))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(LedgeRadius.pill))
                        .clickable { tempFilters = tempFilters.copy(dateRange = option) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = option.label,
                        style = LedgeTextStyle.BodySmall,
                        color = if (isSelected) Gold else TextPrimary,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (categories.isNotEmpty()) {
            Text(
                text = "CATEGORY",
                style = LedgeTextStyle.Caption.copy(color = TextMuted2),
                modifier = Modifier.padding(bottom = 10.dp),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = tempFilters.categoryId == category.id
                    val borderColor by animateColorAsState(
                        if (isSelected) Gold else BorderSubtle, tween(200), label = "catBorder",
                    )
                    val bgColor by animateColorAsState(
                        if (isSelected) GoldDim else BgCard, tween(200), label = "catBg",
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(LedgeRadius.pill))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(LedgeRadius.pill))
                            .clickable {
                                tempFilters = if (isSelected) {
                                    tempFilters.copy(categoryId = null, categoryName = null)
                                } else {
                                    tempFilters.copy(categoryId = category.id, categoryName = category.name)
                                }
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
            Spacer(Modifier.height(20.dp))
        }

        LedgePrimaryButton(
            text = if (tempFilters.isActive) "Apply Filters (${tempFilters.activeCount})" else "Apply Filters",
            onClick = { onApply(tempFilters) },
        )

        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val density = LocalDensity.current
    val dismissState = remember {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = density,
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        onDelete()
                        false
                    }

                    SwipeToDismissBoxValue.StartToEnd -> {
                        onEdit()
                        false
                    }

                    else -> false
                }
            },
            positionalThreshold = { totalDistance -> totalDistance * 0.35f },
        )
    }

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
private fun EditTransactionSheet(
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
            leadingIcon = {
                Text(text = "\u20B9", style = LedgeTextStyle.AmountMedium, color = Gold)
            },
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
                    text = formatDate(form.dateMillis),
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
                    text = formatTime(form.hour, form.minute),
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
                val isSelected = form.selectedCategory?.id == category.id
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
                            form = form.copy(
                                selectedCategory = if (isSelected) null else category
                            )
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

