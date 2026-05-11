package com.ayush.sms.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSecondaryButton
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun SmsReviewScreen(
    onBack: () -> Unit
) {
    val viewModel: SmsReviewViewModel = hiltViewModel()

    val colors = LedgeTheme.colors
    val snackbarHost = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SmsReviewSideEffect.ShowError ->
                    snackbarHost.showSnackbar(effect.message)

                is SmsReviewSideEffect.ShowBulkUndo ->
                    showUndo(snackbarHost, label(effect.action, effect.ids.size)) {
                        viewModel.onEvent(SmsReviewEvent.Undo(effect.ids))
                    }

                is SmsReviewSideEffect.ShowSingleUndo ->
                    showUndo(snackbarHost, label(effect.action, 1)) {
                        viewModel.onEvent(SmsReviewEvent.Undo(setOf(effect.id)))
                    }
            }
        }
    }

    val editingItem = uiState.editingItem
    if (editingItem != null) {
        EditPendingTransactionSheet(
            item = editingItem,
            categories = uiState.categories,
            onConfirm = { event -> viewModel.onEvent(event) },
            onDismiss = { viewModel.onEvent(SmsReviewEvent.EditDismissed) }
        )
    }

    Scaffold(
        containerColor = colors.bgDeep,
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            if (uiState.canBulkAct) {
                BulkActionBar(
                    onConfirmAll = { viewModel.onEvent(SmsReviewEvent.ConfirmAll) },
                    onDismissAll = { viewModel.onEvent(SmsReviewEvent.DismissAll) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.bgDeep)
        ) {
            ReviewTopBar(onBack = onBack)

            when {
                uiState.isLoading -> LoadingState()
                uiState.visibleItems.isEmpty() -> EmptyState()
                else -> ReviewList(
                    items = uiState.visibleItems,
                    onConfirm = { viewModel.onEvent(SmsReviewEvent.Confirm(it)) },
                    onDismiss = { viewModel.onEvent(SmsReviewEvent.Dismiss(it)) },
                    onEdit = { viewModel.onEvent(SmsReviewEvent.Edit(it)) }
                )
            }
        }
    }
}

@Composable
private fun ReviewTopBar(onBack: () -> Unit) {
    val colors = LedgeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.textPrimary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(
            text = "Review transactions",
            style = LedgeTextStyle.HeadingScreen,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun ReviewList(
    items: List<PendingTransaction>,
    onConfirm: (Long) -> Unit,
    onDismiss: (Long) -> Unit,
    onEdit: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = items, key = { it.id }) { item ->
            SwipeablePendingTransactionCard(
                item = item,
                onConfirm = { onConfirm(item.id) },
                onDismiss = { onDismiss(item.id) },
                onEdit = { onEdit(item.id) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeablePendingTransactionCard(
    item: PendingTransaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onConfirm(); true
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDismiss(); true
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
    )

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = { SwipeBackground(swipeState.dismissDirection) },
        modifier = modifier
    ) {
        PendingTransactionCard(
            item = item,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            onEdit = onEdit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(direction: SwipeToDismissBoxValue) {
    val colors = LedgeTheme.colors
    val spec = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> SwipeBackgroundSpec(
            background = colors.greenDim,
            alignment = Alignment.CenterStart,
            icon = Icons.Default.Check,
            tint = colors.semanticGreen
        )

        SwipeToDismissBoxValue.EndToStart -> SwipeBackgroundSpec(
            background = colors.redDim,
            alignment = Alignment.CenterEnd,
            icon = Icons.Default.Close,
            tint = colors.semanticRed
        )

        SwipeToDismissBoxValue.Settled -> SwipeBackgroundSpec(
            background = Color.Transparent,
            alignment = Alignment.Center,
            icon = null,
            tint = colors.textMuted
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(spec.background)
            .padding(horizontal = 24.dp),
        contentAlignment = spec.alignment
    ) {
        if (spec.icon != null) {
            Icon(
                imageVector = spec.icon,
                contentDescription = null,
                tint = spec.tint
            )
        }
    }
}

private data class SwipeBackgroundSpec(
    val background: Color,
    val alignment: Alignment,
    val icon: ImageVector?,
    val tint: Color
)

@Composable
private fun BulkActionBar(
    onConfirmAll: () -> Unit,
    onDismissAll: () -> Unit
) {
    val colors = LedgeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgDeep)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LedgeSecondaryButton(
            text = "Dismiss all",
            onClick = onDismissAll,
            modifier = Modifier.weight(1f)
        )
        LedgePrimaryButton(
            text = "Confirm all",
            onClick = onConfirmAll,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LoadingState() {
    val colors = LedgeTheme.colors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colors.gold,
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun EmptyState() {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ALL CAUGHT UP",
            style = LedgeTextStyle.LabelCaps,
            color = colors.textMuted2
        )
        Text(
            text = "No transactions to review",
            style = LedgeTextStyle.HeadingCard,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "New bank SMS will land here for your approval.",
            style = LedgeTextStyle.BodySmall,
            color = colors.textMuted,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

private suspend fun showUndo(
    host: SnackbarHostState,
    message: String,
    onUndo: () -> Unit
) {
    val result = host.showSnackbar(
        message = message,
        actionLabel = "Undo",
        duration = SnackbarDuration.Short
    )
    if (result == SnackbarResult.ActionPerformed) onUndo()
}

private fun label(action: PendingAction, count: Int): String = when (action) {
    PendingAction.CONFIRM -> if (count == 1) "Confirmed" else "Confirmed $count"
    PendingAction.DISMISS -> if (count == 1) "Dismissed" else "Dismissed $count"
}
