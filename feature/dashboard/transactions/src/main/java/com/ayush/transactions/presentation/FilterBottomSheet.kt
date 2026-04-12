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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.theme.BgCard
import com.ayush.ui.theme.BgDeep
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

@Composable
internal fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
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
        IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
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
        }

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = if (tempFilters.isActive) "Apply Filters (${tempFilters.activeCount})" else "Apply Filters",
            onClick = { onApply(tempFilters) },
        )

        Spacer(Modifier.height(16.dp))
    }
}
