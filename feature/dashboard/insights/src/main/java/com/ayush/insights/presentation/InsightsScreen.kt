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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.common.models.TimePeriod
import com.ayush.common.utils.formatAmount
import com.ayush.insights.domain.models.CategorySpend
import com.ayush.insights.domain.models.IncomeExpenseBucket
import com.ayush.insights.domain.models.SpendBucket
import com.ayush.ui.animation.rememberOneShotAnimationTracker
import com.ayush.ui.animation.rememberOneShotFlag
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.components.charts.BarChartData
import com.ayush.ui.components.charts.BarChartGroup
import com.ayush.ui.components.charts.LedgeBarChart
import com.ayush.ui.components.charts.LedgeGroupedBarChart
import com.ayush.ui.components.charts.LedgeLineChart
import com.ayush.ui.components.charts.LedgePieChart
import com.ayush.ui.components.charts.LineChartPoint
import com.ayush.ui.components.charts.PieChartSegment
import com.ayush.ui.components.noRippleClickable
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

private val LocalEventSink = staticCompositionLocalOf<(InsightsEvent) -> Unit> { error {} }

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
    val onEvent = LocalEventSink.current

    var seeMore by remember { mutableStateOf(false) }

    val animationTracker = rememberOneShotAnimationTracker()

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
                    seeMore = seeMore,
                    onSeeMoreClicked = { seeMore = !seeMore },
                    animateInitialAppearance = rememberOneShotFlag(animationTracker, "insights_category_pie")
                )
            }
        } else if (!state.isLoading) {
            item { EmptyState() }
        }

        if (state.spendSeries.size >= 2) {
            item {
                DailySpendLineCard(
                    series = state.spendSeries,
                    period = state.selectedPeriod,
                    animateInitialAppearance = rememberOneShotFlag(animationTracker, "insights_spend_line"),
                )
            }
        }

        if (state.incomeExpenseHistory.isNotEmpty()) {
            item {
                IncomeVsExpenseCard(
                    history = state.incomeExpenseHistory,
                    animateInitialAppearance = rememberOneShotFlag(animationTracker, "insights_income_expense")
                )
            }
        }

        if (state.weeklyPace.isNotEmpty()) {
            item {
                WeeklyPaceCard(
                    series = state.weeklyPace,
                    animateInitialAppearance = rememberOneShotFlag(animationTracker, "insights_weekly_pace")
                )
            }
        }
    }
}


@Composable
private fun TimePeriodToggle(
    selectedPeriod: TimePeriod,
    onPeriodChanged: (TimePeriod) -> Unit
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
private fun ChartCard(
    capsLabel: String,
    title: String,
    headline: String? = null,
    trend: String? = null,
    trendColor: Color? = null,
    footer: String? = null,
    content: @Composable () -> Unit
) {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = capsLabel,
                style = LedgeTextStyle.LabelCaps,
                color = colors.textMuted
            )
            if (trend != null) {
                Text(
                    text = trend,
                    style = LedgeTextStyle.Caption,
                    color = trendColor ?: colors.gold
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = LedgeTextStyle.HeadingCard,
            color = colors.textPrimary
        )
        if (headline != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = headline,
                style = LedgeTextStyle.AmountLarge,
                color = colors.gold
            )
        }
        Spacer(Modifier.height(16.dp))
        content()
        if (footer != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = footer,
                style = LedgeTextStyle.Caption,
                color = colors.textMuted2
            )
        }
    }
}

@Composable
private fun DailySpendLineCard(
    series: List<SpendBucket>,
    period: TimePeriod,
    animateInitialAppearance: Boolean = true,
) {
    val colors = LedgeTheme.colors
    var selectedIndex by remember(series) { mutableStateOf<Int?>(null) }

    val defaultTitle = when (period) {
        TimePeriod.WEEK -> "Last 7 days"
        TimePeriod.MONTH -> "This month by week"
        TimePeriod.YEAR -> "This year by month"
    }
    val caps = when (period) {
        TimePeriod.WEEK -> "DAILY SPEND"
        TimePeriod.MONTH -> "WEEKLY SPEND"
        TimePeriod.YEAR -> "MONTHLY SPEND"
    }
    val total = series.sumOf { it.amount }
    val selected = selectedIndex?.let { series.getOrNull(it) }
    val title = selected?.label ?: defaultTitle
    val headlineAmount = selected?.amount ?: total

    ChartCard(
        capsLabel = caps,
        title = title,
        headline = "\u20B9${formatAmount(headlineAmount)}",
    ) {
        LedgeLineChart(
            points = series.map { LineChartPoint(label = it.label, value = it.amount.toFloat()) },
            lineColor = colors.gold,
            selectedIndex = selectedIndex,
            onPointTap = { idx -> selectedIndex = if (selectedIndex == idx) null else idx },
            animateInitialAppearance = animateInitialAppearance
        )
    }
}

@Composable
private fun IncomeVsExpenseCard(
    history: List<IncomeExpenseBucket>,
    animateInitialAppearance: Boolean = true,
) {
    val colors = LedgeTheme.colors
    val incomeTotal = history.sumOf { it.income }
    val expenseTotal = history.sumOf { it.expense }

    ChartCard(capsLabel = "INCOME VS EXPENSE", title = "6-month view") {
        LedgeGroupedBarChart(
            groups = history.map { bucket ->
                BarChartGroup(
                    label = bucket.label,
                    values = listOf(bucket.income.toFloat(), bucket.expense.toFloat()),
                    colors = listOf(colors.semanticGreen, colors.semanticRed)
                )
            },
            animateInitialAppearance = animateInitialAppearance
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(
                color = colors.semanticGreen,
                label = "Income",
                amount = "\u20B9${formatAmount(incomeTotal)}"
            )
            Spacer(Modifier.width(20.dp))
            LegendItem(
                color = colors.semanticRed,
                label = "Expense",
                amount = "\u20B9${formatAmount(expenseTotal)}"
            )
        }
    }
}

@Composable
private fun WeeklyPaceCard(
    series: List<SpendBucket>,
    animateInitialAppearance: Boolean = true,
) {
    val colors = LedgeTheme.colors
    var selectedIndex by remember(series) { mutableStateOf<Int?>(null) }
    val total = series.sumOf { it.amount }

    ChartCard(
        capsLabel = "THIS MONTH BY WEEK",
        title = "Weekly pace",
        headline = "\u20B9${formatAmount(total)}",
    ) {
        LedgeBarChart(
            bars = series.map { BarChartData(it.label, it.amount.toFloat(), colors.gold) },
            selectedIndex = selectedIndex,
            onBarTap = { idx -> selectedIndex = if (selectedIndex == idx) null else idx },
            showValues = true,
            valueFormatter = { compactAmount(it.toDouble()) },
            animateInitialAppearance = animateInitialAppearance
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String, amount: String? = null) {
    val colors = LedgeTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = LedgeTextStyle.BodySmall,
            color = colors.textMuted2
        )
        if (amount != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = amount,
                style = LedgeTextStyle.AmountMono,
                color = color
            )
        }
    }
}

/**
 * Compact Indian-currency formatter for in-chart labels.
 *   1,200    → ₹1.2k
 *   12,500   → ₹12k   (no decimal once the scaled value is ≥ 10 — keeps the label narrow)
 *   1,25,000 → ₹1.2L
 *   1.2 Cr   → ₹1.2Cr
 *
 * formatAmount() is used in the headline where full precision matters; this is for the
 * per-bar labels where pixel budget per slot is ~50–70dp.
 */
private fun compactAmount(value: Double): String {
    if (value < 1000) return "\u20B9${value.toLong()}"
    val (divisor, suffix) = when {
        value >= 10_000_000 -> 10_000_000.0 to "Cr"
        value >= 100_000 -> 100_000.0 to "L"
        else -> 1_000.0 to "k"
    }
    val scaled = value / divisor
    val body = if (scaled < 10) "%.1f".format(scaled) else scaled.toLong().toString()
    return "\u20B9$body$suffix"
}

@Composable
private fun SpendingByCategoryCard(
    categories: List<CategorySpend>,
    seeMore: Boolean,
    onSeeMoreClicked: () -> Unit,
    animateInitialAppearance: Boolean = true
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
                animateInitialAppearance = animateInitialAppearance,
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
                },
            )
        }

        Spacer(Modifier.height(16.dp))

        categories.take(if (seeMore) categories.size else 5).forEachIndexed { index, category ->
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

        if (categories.size > 5) {
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
}
