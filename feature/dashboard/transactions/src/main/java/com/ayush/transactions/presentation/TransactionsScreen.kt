package com.ayush.transactions.presentation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.common.utils.toast
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
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

@Composable
fun TransactionsScreen() {
    val viewModel: TransactionsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is TransactionsSideEffect.ShowToast -> effect.message.toast(context)
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
                                onEvent(
                                    TransactionsEvent.ApplyFilters(
                                        state.filterState.copy(dateRange = DateRangeOption.ALL_TIME),
                                    ),
                                )
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
                                        state.filterState.copy(categoryId = null, categoryName = null),
                                    ),
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
                val grouped = remember(state.transactions) {
                    state.transactions.groupBy { formatDateHeader(it.date) }
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    grouped.forEach { (dateHeader, txns) ->
                        stickyHeader(key = "header_$dateHeader") {
                            DateSectionHeader(label = dateHeader)
                        }
                        items(
                            items = txns,
                            key = { it.id },
                        ) { transaction ->
                            SwipeableTransactionItem(
                                transaction = transaction,
                                onEdit = { transactionToEdit = transaction },
                                onDelete = { transactionToDelete = transaction },
                            )
                        }
                    }
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
private fun DateSectionHeader(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDeep)
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = label,
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
        )
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
                    text = formatTime(transaction.date),
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
