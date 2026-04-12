package com.ayush.transactions.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.LedgeSelectableChip
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.SemanticGreen
import com.ayush.ui.theme.SemanticRed
import com.ayush.ui.theme.TextMuted
import com.ayush.ui.theme.TextMuted2
import com.ayush.ui.theme.TextPrimary

@Composable
internal fun FilterSheet(
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "TYPE",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 10.dp),
        )
        LedgeSegmentedToggle(
            options = listOf(
                SegmentOption<TransactionType?>(null, "All", TextPrimary),
                SegmentOption(TransactionType.EXPENSE, "Expense", SemanticRed),
                SegmentOption(TransactionType.INCOME, "Income", SemanticGreen),
            ),
            selectedValue = tempFilters.type,
            onSelect = { tempFilters = tempFilters.copy(type = it) },
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "DATE RANGE",
            style = LedgeTextStyle.Caption.copy(color = TextMuted2),
            modifier = Modifier.padding(bottom = 10.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(DateRangeOption.entries) { option ->
                LedgeSelectableChip(
                    label = option.label,
                    isSelected = tempFilters.dateRange == option,
                    onClick = { tempFilters = tempFilters.copy(dateRange = option) },
                )
            }
        }

        if (categories.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))

            Text(
                text = "CATEGORY",
                style = LedgeTextStyle.Caption.copy(color = TextMuted2),
                modifier = Modifier.padding(bottom = 10.dp),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = tempFilters.categoryId == category.id
                    LedgeSelectableChip(
                        label = category.name,
                        isSelected = isSelected,
                        onClick = {
                            tempFilters = if (isSelected) {
                                tempFilters.copy(categoryId = null, categoryName = null)
                            } else {
                                tempFilters.copy(categoryId = category.id, categoryName = category.name)
                            }
                        },
                        leadingDotColor = category.color,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = if (tempFilters.isActive) "Apply Filters (${tempFilters.activeCount})" else "Apply Filters",
            onClick = { onApply(tempFilters) },
        )

        Spacer(Modifier.height(16.dp))
    }
}
