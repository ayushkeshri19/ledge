package com.ayush.insights.presentation

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.common.models.TimePeriod
import com.ayush.common.utils.formatAmount
import com.ayush.insights.domain.models.CategorySpend
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.components.charts.LedgePieChart
import com.ayush.ui.components.charts.PieChartSegment
import com.ayush.ui.components.noRippleClickable
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

private val LocalEventSink = staticCompositionLocalOf<(InsightsEvent) -> Unit> { error { } }

@Composable
fun InsightsScreen() {
    val viewModel: InsightsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalEventSink provides viewModel::onEvent) {
        InsightsContent(state)
    }
}

@Composable
private fun InsightsContent(state: InsightsState) {
    val colors = LedgeTheme.colors

    var seeMore by remember { mutableStateOf(false) }

    val onEvent = LocalEventSink.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Insights",
                style = LedgeTextStyle.HeadingScreen,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        item {
            TimePeriodToggle(
                selectedPeriod = state.selectedPeriod,
                onPeriodChanged = { onEvent(InsightsEvent.PeriodChanged(it)) }
            )
        }

        if (state.categorySpending.isNotEmpty()) {
            item {
                SpendingByCategoryCard(
                    categories = state.categorySpending,
                    seeMore = seeMore
                ) {
                    seeMore = !seeMore
                }
            }
        } else if (!state.isLoading) {
            item { EmptyState() }
        }
    }
}

@Composable
private fun TimePeriodToggle(
    selectedPeriod: TimePeriod,
    onPeriodChanged: (TimePeriod) -> Unit,
) {
    val gold = LedgeTheme.colors.gold
    val options = remember(gold) {
        TimePeriod.entries.map { period ->
            SegmentOption(
                value = period,
                label = period.label,
                selectedColor = gold
            )
        }
    }

    LedgeSegmentedToggle(
        options = options,
        selectedValue = selectedPeriod,
        onSelect = onPeriodChanged
    )
}

@Composable
private fun EmptyState() {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "No spending this month",
            style = LedgeTextStyle.HeadingCard,
            color = colors.textPrimary
        )
        Text(
            text = "Log a transaction to see your category breakdown.",
            style = LedgeTextStyle.BodySmall,
            color = colors.textMuted2
        )
    }
}

@Composable
private fun SpendingByCategoryCard(
    categories: List<CategorySpend>,
    seeMore: Boolean,
    onSeeMoreClicked: () -> Unit
) {
    val colors = LedgeTheme.colors
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val totalExpense = categories.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
            .animateContentSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Spending by Category",
            style = LedgeTextStyle.HeadingCard,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            LedgePieChart(
                segments = categories.map { cat ->
                    PieChartSegment(
                        value = cat.amount.toFloat(),
                        color = cat.color,
                        label = cat.categoryName
                    )
                },
                modifier = Modifier.size(200.dp),
                strokeWidth = 24.dp,
                selectedIndex = selectedIndex,
                onSegmentTap = { index ->
                    selectedIndex = if (selectedIndex == index) null else index
                },
                centerContent = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOTAL",
                            style = LedgeTextStyle.LabelCaps,
                            color = colors.textMuted
                        )
                        Text(
                            text = "\u20B9${formatAmount(totalExpense)}",
                            style = LedgeTextStyle.AmountLarge,
                            color = colors.textPrimary
                        )
                    }
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        categories.take(
            if (seeMore) categories.size else 5
        ).forEachIndexed { index, category ->
            val isSelected = index == selectedIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected) Modifier.background(category.color.copy(alpha = 0.08f))
                        else Modifier
                    )
                    .clickable {
                        selectedIndex = if (selectedIndex == index) null else index
                    }
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(category.color)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = category.categoryName,
                    style = LedgeTextStyle.BodySmall,
                    color = if (isSelected) colors.textPrimary else colors.textMuted2,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "\u20B9${formatAmount(category.amount)}",
                    style = LedgeTextStyle.BodySmall,
                    color = if (isSelected) colors.textPrimary else colors.textMuted2
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${(category.percentage * 100).toInt()}%",
                    style = LedgeTextStyle.Caption,
                    color = colors.textMuted
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (seeMore) "See Less" else "See More",
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
                .noRippleClickable(
                    enabled = true,
                    onClick = onSeeMoreClicked
                ),
            style = LedgeTextStyle.BodySmall,
            color = colors.gold
        )
    }
}
