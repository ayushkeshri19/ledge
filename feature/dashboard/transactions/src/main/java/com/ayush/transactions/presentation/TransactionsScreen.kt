package com.ayush.transactions.presentation

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.theme.BgCard
import com.ayush.ui.theme.GreenDim
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.RedDim
import com.ayush.ui.theme.SemanticGreen
import com.ayush.ui.theme.SemanticRed
import com.ayush.ui.theme.TextMuted
import com.ayush.ui.theme.TextPrimary
import java.text.SimpleDateFormat
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

@Composable
private fun TransactionsContent(
    state: TransactionsState,
    onEvent: (TransactionsEvent) -> Unit,
) {
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
                        color = com.ayush.ui.theme.Gold,
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
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { onEvent(TransactionsEvent.DeleteTransaction(transaction.id)) },
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit,
) {
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

        Text(
            text = "$amountPrefix\u20B9${formatAmount(transaction.amount)}",
            style = LedgeTextStyle.AmountMono,
            color = amountColor,
        )

        Spacer(Modifier.width(4.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun formatDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun formatTime(millis: Long): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        String.format(Locale.getDefault(), "%,d", amount.toLong())
    } else {
        String.format(Locale.getDefault(), "%,.2f", amount)
    }
}
