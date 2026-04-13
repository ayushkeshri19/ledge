package com.ayush.home.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.common.utils.formatAmount
import com.ayush.home.domain.models.CategorySpend
import com.ayush.home.domain.models.RecentTransaction
import com.ayush.home.domain.models.TimePeriod
import com.ayush.ui.components.AnimatedAmount
import com.ayush.ui.components.DashboardShimmer
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.components.charts.LedgePieChart
import com.ayush.ui.components.charts.PieChartSegment
import com.ayush.ui.theme.BgCard
import com.ayush.ui.theme.BgDeep
import com.ayush.ui.theme.BgSurface
import com.ayush.ui.theme.DmSansFontFamily
import com.ayush.ui.theme.Gold
import com.ayush.ui.theme.GoldGlow
import com.ayush.ui.theme.GoldLight
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.SemanticGreen
import com.ayush.ui.theme.SemanticRed
import com.ayush.ui.theme.TextMuted
import com.ayush.ui.theme.TextMuted2
import com.ayush.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                HomeSideEffect.NavigateToTransactions -> onNavigateToTransactions()
            }
        }
    }

    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            UserDetailsRow(
                greeting = state.userDetails.greeting,
                name = state.userDetails.name,
                initials = state.userDetails.initials,
            )
        }

        item {
            TimePeriodToggle(
                selectedPeriod = state.selectedPeriod,
                onPeriodChanged = { onEvent(HomeUiEvent.PeriodChanged(it)) },
            )
        }

        if (state.isDashboardLoading) {
            item {
                DashboardShimmer()
            }
        } else {
            item {
                BalanceOverviewCard(state = state.summaryState)
            }

            if (state.categorySpending.isNotEmpty()) {
                item {
                    SpendingByCategoryCard(spending = state.categorySpending)
                }
            }

            if (state.recentTransactions.isNotEmpty()) {
                item {
                    RecentTransactionsCard(
                        transactions = state.recentTransactions,
                        onSeeAll = { onEvent(HomeUiEvent.SeeAllTransactionsClicked) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePeriodToggle(
    selectedPeriod: TimePeriod,
    onPeriodChanged: (TimePeriod) -> Unit,
) {
    val options = remember {
        TimePeriod.entries.map { period ->
            SegmentOption(
                value = period,
                label = period.label,
                selectedColor = Gold,
            )
        }
    }

    LedgeSegmentedToggle(
        options = options,
        selectedValue = selectedPeriod,
        onSelect = onPeriodChanged,
    )
}

@Composable
private fun BalanceOverviewCard(state: SummaryState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(20.dp),
    ) {
        Text(
            text = "NET BALANCE",
            style = LedgeTextStyle.LabelCaps,
            color = TextMuted,
        )
        Spacer(Modifier.height(4.dp))
        AnimatedAmount(
            targetAmount = state.netBalance,
            style = LedgeTextStyle.BalanceHero,
            color = if (state.netBalance >= 0) Gold else SemanticRed,
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SemanticGreen),
                    )
                    Text(
                        text = "INCOME",
                        style = LedgeTextStyle.LabelCaps,
                        color = TextMuted,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "+\u20B9${formatAmount(state.totalIncome)}",
                    style = LedgeTextStyle.AmountMedium,
                    color = SemanticGreen,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SemanticRed),
                    )
                    Text(
                        text = "EXPENSE",
                        style = LedgeTextStyle.LabelCaps,
                        color = TextMuted,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "-\u20B9${formatAmount(state.totalExpense)}",
                    style = LedgeTextStyle.AmountMedium,
                    color = SemanticRed,
                )
            }
        }
    }
}

@Composable
private fun SpendingByCategoryCard(spending: List<CategorySpend>) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(20.dp),
    ) {
        Text(
            text = "Spending by Category",
            style = LedgeTextStyle.HeadingCard,
            color = TextPrimary,
        )
        Spacer(Modifier.height(16.dp))

        val totalExpense = spending.sumOf { it.amount }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            LedgePieChart(
                segments = spending.map { cat ->
                    PieChartSegment(
                        value = cat.amount.toFloat(),
                        color = cat.color,
                        label = cat.categoryName,
                    )
                },
                modifier = Modifier.size(180.dp),
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
                            color = TextMuted,
                        )
                        Text(
                            text = "\u20B9${formatAmount(totalExpense)}",
                            style = LedgeTextStyle.AmountLarge,
                            color = TextPrimary,
                        )
                    }
                },
            )
        }

        Spacer(Modifier.height(16.dp))

        spending.forEachIndexed { index, category ->
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(category.color),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = category.categoryName,
                    style = LedgeTextStyle.BodySmall,
                    color = if (isSelected) TextPrimary else TextMuted2,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "\u20B9${formatAmount(category.amount)}",
                    style = LedgeTextStyle.BodySmall,
                    color = if (isSelected) TextPrimary else TextMuted2,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${(category.percentage * 100).toInt()}%",
                    style = LedgeTextStyle.Caption,
                    color = TextMuted,
                )
            }
        }
    }
}

@Composable
private fun RecentTransactionsCard(
    transactions: List<RecentTransaction>,
    onSeeAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recent Transactions",
                style = LedgeTextStyle.HeadingCard,
                color = TextPrimary,
            )
            Text(
                text = "See all",
                style = LedgeTextStyle.BodySmall,
                color = Gold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(4.dp),
            )
        }
        Spacer(Modifier.height(12.dp))

        transactions.forEachIndexed { index, transaction ->
            val offsetY = remember { Animatable(30f) }
            val alpha = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                delay(index * 60L)
                launch { offsetY.animateTo(0f, tween(300, easing = EaseOutCubic)) }
                launch { alpha.animateTo(1f, tween(300)) }
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = offsetY.value
                        this.alpha = alpha.value
                    }
            ) {
                RecentTransactionItem(transaction = transaction)
            }

            if (index < transactions.lastIndex) {
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun RecentTransactionItem(transaction: RecentTransaction) {
    val amountColor = if (transaction.isExpense) SemanticRed else SemanticGreen
    val amountPrefix = if (transaction.isExpense) "-" else "+"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    (transaction.categoryColor ?: TextMuted).copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(transaction.categoryColor ?: TextMuted),
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
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                transaction.categoryName?.let { name ->
                    Text(
                        text = name,
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
                    text = formatDate(transaction.date),
                    style = LedgeTextStyle.Caption,
                    color = TextMuted,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = "$amountPrefix\u20B9${formatAmount(transaction.amount)}",
            style = LedgeTextStyle.AmountMono,
            color = amountColor,
        )
    }
}

@Composable
private fun UserDetailsRow(
    greeting: String,
    name: String,
    initials: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "$greeting,",
                color = TextMuted,
                style = LedgeTextStyle.BodySmall,
            )
            Text(
                text = name,
                color = TextPrimary,
                style = LedgeTextStyle.HeadingScreen,
            )
        }

        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GoldGlow),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BgCard),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GoldLight, Color(0xFF8B6914)),
                                    start = Offset.Zero,
                                    end = Offset(
                                        Float.POSITIVE_INFINITY,
                                        Float.POSITIVE_INFINITY,
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            color = BgDeep,
                            style = TextStyle(
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            ),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(BgSurface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Gold),
            )
        }
    }
}