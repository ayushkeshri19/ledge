package com.ayush.transactions.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ayush.common.utils.formatAmount
import com.ayush.common.utils.toast
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionListItem
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.ui.components.LedgeFilterChip
import com.ayush.ui.components.LedgeSyncErrorBanner
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import kotlinx.coroutines.launch

private val LocalEventSink = staticCompositionLocalOf<(TransactionsEvent) -> Unit> { error { } }

@Composable
fun TransactionsScreen() {
    val viewModel: TransactionsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyPagingItems = viewModel.transactionsPagingFlow.collectAsLazyPagingItems()
    val context = LocalContext.current

    var stopSeriesParentId by remember { mutableStateOf<Long?>(null) }

    CompositionLocalProvider(LocalEventSink provides viewModel::onEvent) {
        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect { effect ->
                when (effect) {
                    is TransactionsSideEffect.ShowToast -> effect.message.toast(context)
                    is TransactionsSideEffect.ShowStopSeriesConfirmation -> {
                        stopSeriesParentId = effect.parentId
                    }
                }
            }
        }

        TransactionsContent(
            state = state,
            lazyPagingItems = lazyPagingItems,
            stopSeriesParentId = stopSeriesParentId,
            onStopSeriesDismiss = { stopSeriesParentId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionsContent(
    state: TransactionsState,
    lazyPagingItems: LazyPagingItems<TransactionListItem>,
    stopSeriesParentId: Long?,
    onStopSeriesDismiss: () -> Unit
) {
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val stopSeriesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val focusManager = LocalFocusManager.current

    val isInitialLoad = lazyPagingItems.loadState.refresh is LoadState.Loading
    val isEmpty = lazyPagingItems.itemCount == 0
            && lazyPagingItems.loadState.refresh is LoadState.NotLoading

    val onEvent = LocalEventSink.current
    val colors = LedgeTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Transactions",
                style = LedgeTextStyle.HeadingScreen,
                color = colors.textPrimary
            )
            Box {
                IconButton(
                    onClick = {
                        showFilterSheet = true
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = "Filter",
                        tint = if (state.filterState.isActive) colors.gold else colors.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (state.filterState.isActive) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(colors.gold)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.filterState.activeCount.toString(),
                            style = LedgeTextStyle.Caption.copy(color = colors.bgDeep)
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
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (state.searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onEvent(TransactionsEvent.ClearSearch) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        if (state.filterState.isActive) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.filterState.type?.let { type ->
                    item {
                        LedgeFilterChip(
                            label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            onRemove = {
                                onEvent(TransactionsEvent.ApplyFilters(state.filterState.copy(type = null)))
                            }
                        )
                    }
                }
                if (state.filterState.dateRange != DateRangeOption.ALL_TIME) {
                    item {
                        LedgeFilterChip(
                            label = state.filterState.dateRange.label,
                            onRemove = {
                                onEvent(
                                    TransactionsEvent.ApplyFilters(
                                        state.filterState.copy(dateRange = DateRangeOption.ALL_TIME),
                                    ),
                                )
                            }
                        )
                    }
                }
                state.filterState.categoryName?.let { name ->
                    item {
                        LedgeFilterChip(
                            label = name,
                            onRemove = {
                                onEvent(
                                    TransactionsEvent.ApplyFilters(
                                        state.filterState.copy(categoryId = null, categoryName = null),
                                    ),
                                )
                            }
                        )
                    }
                }
                if (state.filterState.activeCount > 1) {
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(LedgeRadius.pill))
                                .background(colors.bgSurface)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(LedgeRadius.pill))
                                .clickable { onEvent(TransactionsEvent.ClearFilters) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Clear all",
                                style = LedgeTextStyle.Caption,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(Modifier.height(16.dp))
        }

        if (state.hasSyncError) {
            LedgeSyncErrorBanner(
                message = "Couldn't refresh data. Check your connection and try again.",
                onRetry = { onEvent(TransactionsEvent.RefreshRequested) },
                onDismiss = { onEvent(TransactionsEvent.DismissSyncError) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        PullToRefreshBox(
            isRefreshing = state.isSyncing,
            onRefresh = { onEvent(TransactionsEvent.RefreshRequested) },
            modifier = Modifier.fillMaxSize()
        ) {
        when {
            // Paging's own initial-load signal. Don't also gate on state.isSyncing
            // or the list would disappear every time the user pulls to refresh —
            // PullToRefreshBox already shows its own top indicator for that case.
            isInitialLoad -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colors.gold,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            isEmpty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
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
                        Text(text = heading, style = LedgeTextStyle.HeadingCard, color = colors.textMuted)
                        Spacer(Modifier.height(8.dp))
                        Text(text = sub, style = LedgeTextStyle.BodySmall, color = colors.textMuted)
                        if (state.filterState.isActive) {
                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(LedgeRadius.pill))
                                    .background(colors.bgCard)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(LedgeRadius.pill))
                                    .clickable { onEvent(TransactionsEvent.ClearFilters) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(text = "Clear filters", style = LedgeTextStyle.BodySmall, color = colors.gold)
                            }
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        count = lazyPagingItems.itemCount,
                        key = { index ->
                            when (val item = lazyPagingItems.peek(index)) {
                                is TransactionListItem.Header -> "header_${item.dateLabel}"
                                is TransactionListItem.Item -> "txn_${item.transaction.id}"
                                null -> "placeholder_$index"
                            }
                        },
                    ) { index ->
                        when (val item = lazyPagingItems[index]) {
                            is TransactionListItem.Header -> {
                                DateSectionHeader(label = item.dateLabel)
                            }
                            is TransactionListItem.Item -> {
                                SwipeableTransactionItem(
                                    transaction = item.transaction,
                                    onEdit = {
                                        transactionToEdit = item.transaction
                                        focusManager.clearFocus()
                                    },
                                    onDelete = {
                                        transactionToDelete = item.transaction
                                        focusManager.clearFocus()
                                    }
                                )
                            }

                            null -> {}
                        }
                    }

                    if (lazyPagingItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colors.gold,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
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
            containerColor = colors.bgSurface
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
            containerColor = colors.bgSurface
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
                onStopSeries = { parentId ->
                    scope.launch {
                        editSheetState.hide()
                        transactionToEdit = null
                        onEvent(TransactionsEvent.StopSeriesRequested(parentId))
                    }
                },
                onDismiss = {
                    scope.launch {
                        editSheetState.hide()
                        transactionToEdit = null
                    }
                }
            )
        }
    }

    stopSeriesParentId?.let { parentId ->
        ModalBottomSheet(
            onDismissRequest = onStopSeriesDismiss,
            sheetState = stopSeriesSheetState,
            containerColor = colors.bgSurface
        ) {
            StopSeriesConfirmationSheet(
                onConfirm = {
                    scope.launch {
                        stopSeriesSheetState.hide()
                        onEvent(TransactionsEvent.StopSeriesConfirmed(parentId))
                        onStopSeriesDismiss()
                    }
                },
                onDismiss = {
                    scope.launch {
                        stopSeriesSheetState.hide()
                        onStopSeriesDismiss()
                    }
                }
            )
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState,
            containerColor = colors.bgSurface
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
                }
            )
        }
    }
}

@Composable
private fun DateSectionHeader(label: String) {
    val colors = LedgeTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgDeep)
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Text(
            text = label,
            style = LedgeTextStyle.Caption.copy(color = colors.textMuted2)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
            positionalThreshold = { totalDistance -> totalDistance * 0.35f }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val colors = LedgeTheme.colors
            val direction = dismissState.dismissDirection
            val bgColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> colors.semanticRed.copy(alpha = 0.12f)
                    SwipeToDismissBoxValue.StartToEnd -> colors.goldDim
                    else -> Color.Transparent
                },
                animationSpec = tween(150),
                label = "swipeBg"
            )
            val iconTint by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.EndToStart -> colors.semanticRed
                    SwipeToDismissBoxValue.StartToEnd -> colors.gold
                    else -> Color.Transparent
                },
                animationSpec = tween(150),
                label = "swipeIcon"
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
                        modifier = Modifier.size(22.dp)
                    )
                    SwipeToDismissBoxValue.StartToEnd -> Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                    else -> {}
                }
            }
        }
    ) {
        TransactionItem(transaction = transaction)
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val colors = LedgeTheme.colors
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) colors.semanticRed else colors.semanticGreen
    val amountPrefix = if (isExpense) "-" else "+"
    val indicatorColor = if (isExpense) colors.redDim else colors.greenDim

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LedgeRadius.medium))
            .background(colors.bgCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(indicatorColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(transaction.category?.color ?: colors.textMuted)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = transaction.note,
                    style = LedgeTextStyle.HeadingCard,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (transaction.isRecurring || transaction.parentId != null) {
                    Icon(
                        imageVector = Icons.Filled.Autorenew,
                        contentDescription = "Recurring",
                        tint = colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                transaction.category?.let { cat ->
                    Text(
                        text = cat.name,
                        style = LedgeTextStyle.Caption,
                        color = colors.textMuted
                    )
                    Text(
                        text = "\u00B7",
                        style = LedgeTextStyle.Caption,
                        color = colors.textMuted
                    )
                }
                Text(
                    text = formatTime(transaction.date),
                    style = LedgeTextStyle.Caption,
                    color = colors.textMuted
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountPrefix\u20B9${formatAmount(transaction.amount)}",
                style = LedgeTextStyle.AmountMono,
                color = amountColor
            )
            if (transaction.isRecurring) {
                Text(
                    text = transaction.recurrenceType?.value?.replaceFirstChar { it.uppercase() } ?: "Recurring",
                    style = LedgeTextStyle.Caption,
                    color = colors.gold
                )
            }
        }
    }
}
