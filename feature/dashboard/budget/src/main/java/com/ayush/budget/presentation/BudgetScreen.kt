package com.ayush.budget.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.budget.domain.models.BudgetStatus
import com.ayush.budget.domain.models.BudgetWithSpent
import com.ayush.common.utils.formatAmount
import com.ayush.common.utils.toast
import com.ayush.ui.components.charts.LedgeBudgetProgressBar
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import kotlinx.coroutines.launch

private val LocalEventSink = staticCompositionLocalOf<(BudgetEvent) -> Unit> { error {} }

@Composable
fun BudgetScreen() {
    val viewModel: BudgetViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CompositionLocalProvider(LocalEventSink provides viewModel::onEvent) {
        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect { effect ->
                when (effect) {
                    is BudgetSideEffect.ShowToast -> effect.message.toast(context)
                }
            }
        }

        BudgetContent(state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetContent(state: BudgetState) {
    val colors = LedgeTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val onEvent = LocalEventSink.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Budgets",
                style = LedgeTextStyle.HeadingScreen,
                color = colors.textPrimary
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.gold.copy(alpha = 0.12f))
                    .clickable { onEvent(BudgetEvent.ShowAddSheet) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add budget",
                    tint = colors.gold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = colors.gold,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            state.overallBudget == null && state.categoryBudgets.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No budgets yet",
                            style = LedgeTextStyle.HeadingCard,
                            color = colors.textMuted
                        )
                        Text(
                            text = "Set spending limits to stay on track",
                            style = LedgeTextStyle.BodySmall,
                            color = colors.textMuted
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap + to create one",
                            style = LedgeTextStyle.BodySmall,
                            color = colors.gold
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.overallBudget?.let { overall ->
                        item(key = "overall") {
                            OverallBudgetCard(
                                budget = overall,
                                onClick = { onEvent(BudgetEvent.ShowEditSheet(overall)) }
                            )
                        }
                    }

                    if (state.categoryBudgets.isNotEmpty()) {
                        item(key = "header") {
                            Text(
                                text = "CATEGORY BUDGETS",
                                style = LedgeTextStyle.LabelCaps,
                                color = colors.textMuted,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(
                            items = state.categoryBudgets,
                            key = { it.budget.id },
                        ) { budget ->
                            CategoryBudgetCard(
                                budget = budget,
                                onClick = { onEvent(BudgetEvent.ShowEditSheet(budget)) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showSheet) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(BudgetEvent.DismissSheet) },
            sheetState = sheetState,
            containerColor = colors.bgSurface
        ) {
            AddEditBudgetSheet(
                categories = state.categories,
                editingBudget = state.editingBudget,
                onSave = { event ->
                    scope.launch {
                        sheetState.hide()
                        onEvent(event)
                    }
                },
                onDelete = if (state.editingBudget != null) { id ->
                    scope.launch {
                        sheetState.hide()
                        onEvent(BudgetEvent.DeleteBudget(id))
                    }
                } else null,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        onEvent(BudgetEvent.DismissSheet)
                    }
                }
            )
        }
    }
}

@Composable
private fun OverallBudgetCard(
    budget: BudgetWithSpent,
    onClick: () -> Unit,
) {
    val colors = LedgeTheme.colors
    val statusColor = when (budget.status) {
        BudgetStatus.NORMAL -> colors.gold
        BudgetStatus.WARNING -> colors.gold
        BudgetStatus.EXCEEDED -> colors.semanticRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Text(
            text = "MONTHLY BUDGET",
            style = LedgeTextStyle.LabelCaps,
            color = colors.textMuted
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "\u20B9${formatAmount(budget.spent)}",
                    style = LedgeTextStyle.AmountLarge,
                    color = statusColor
                )
                Text(
                    text = "of \u20B9${formatAmount(budget.budget.amount)}",
                    style = LedgeTextStyle.BodySmall,
                    color = colors.textMuted
                )
            }
            StatusBadge(budget = budget)
        }

        Spacer(Modifier.height(12.dp))

        LedgeBudgetProgressBar(
            progress = budget.ratio,
            warningThreshold = budget.warningRatio,
            height = 10.dp
        )
    }
}

@Composable
private fun CategoryBudgetCard(
    budget: BudgetWithSpent,
    onClick: () -> Unit
) {
    val colors = LedgeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bgCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(budget.budget.categoryColor ?: colors.textMuted)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = budget.budget.categoryName ?: "Unknown",
                    style = LedgeTextStyle.HeadingCard,
                    color = colors.textPrimary
                )
                StatusBadge(budget = budget)
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "\u20B9${formatAmount(budget.spent)} / \u20B9${formatAmount(budget.budget.amount)}",
                    style = LedgeTextStyle.Caption,
                    color = colors.textMuted2
                )
                Text(
                    text = "${(budget.ratio * 100).toInt()}%",
                    style = LedgeTextStyle.Caption,
                    color = when (budget.status) {
                        BudgetStatus.NORMAL -> colors.textMuted2
                        BudgetStatus.WARNING -> colors.gold
                        BudgetStatus.EXCEEDED -> colors.semanticRed
                    },
                )
            }

            Spacer(Modifier.height(8.dp))

            LedgeBudgetProgressBar(
                progress = budget.ratio,
                warningThreshold = budget.warningRatio,
                height = 6.dp
            )
        }
    }
}

@Composable
private fun StatusBadge(budget: BudgetWithSpent) {
    val colors = LedgeTheme.colors
    when (budget.status) {
        BudgetStatus.EXCEEDED -> {
            Text(
                text = "Over by \u20B9${formatAmount(budget.overBy)}",
                style = LedgeTextStyle.Caption,
                color = colors.semanticRed,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.semanticRed.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        BudgetStatus.WARNING -> {
            Text(
                text = "\u20B9${formatAmount(budget.remaining)} left",
                style = LedgeTextStyle.Caption,
                color = colors.gold
            )
        }

        BudgetStatus.NORMAL -> {
            Text(
                text = "\u20B9${formatAmount(budget.remaining)} left",
                style = LedgeTextStyle.Caption,
                color = colors.semanticGreen
            )
        }
    }
}
