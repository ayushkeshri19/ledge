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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
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
import com.ayush.home.domain.models.RecentTransaction
import com.ayush.ui.components.AnimatedAmount
import com.ayush.ui.components.DashboardShimmer
import com.ayush.ui.components.noRippleClickable
import com.ayush.ui.theme.DmSansFontFamily
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val LocalEventSink = staticCompositionLocalOf<(HomeUiEvent) -> Unit> {
    error {}
}

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalEventSink provides viewModel::onEvent) {
        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect { sideEffect ->
                when (sideEffect) {
                    HomeSideEffect.NavigateToTransactions -> onNavigateToTransactions()
                    HomeSideEffect.NavigateToProfile -> onNavigateToProfile()
                }
            }
        }

        HomeContent(state)
    }

}

@Composable
private fun HomeContent(state: HomeState) {

    val onEvent = LocalEventSink.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UserDetailsRow(
                greeting = state.userDetails.greeting,
                name = state.userDetails.name,
                initials = state.userDetails.initials,
                showDot = state.showDot
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

            if (state.recentTransactions.isNotEmpty()) {
                item {
                    RecentTransactionsCard(
                        transactions = state.recentTransactions,
                        onSeeAll = { onEvent(HomeUiEvent.SeeAllTransactionsClicked) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceOverviewCard(state: SummaryState) {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
            .padding(20.dp)
    ) {
        Text(
            text = "NET BALANCE",
            style = LedgeTextStyle.LabelCaps,
            color = colors.textMuted
        )
        Spacer(Modifier.height(4.dp))
        AnimatedAmount(
            targetAmount = state.netBalance,
            style = LedgeTextStyle.BalanceHero,
            color = if (state.netBalance >= 0) colors.gold else colors.semanticRed
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.semanticGreen)
                    )
                    Text(
                        text = "INCOME",
                        style = LedgeTextStyle.LabelCaps,
                        color = colors.textMuted
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "+\u20B9${formatAmount(state.totalIncome)}",
                    style = LedgeTextStyle.AmountMedium,
                    color = colors.semanticGreen
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors.semanticRed)
                    )
                    Text(
                        text = "EXPENSE",
                        style = LedgeTextStyle.LabelCaps,
                        color = colors.textMuted
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "-\u20B9${formatAmount(state.totalExpense)}",
                    style = LedgeTextStyle.AmountMedium,
                    color = colors.semanticRed
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = LedgeTextStyle.HeadingCard,
                color = colors.textPrimary
            )
            Text(
                text = "See all",
                style = LedgeTextStyle.BodySmall,
                color = colors.gold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(4.dp)
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
    val colors = LedgeTheme.colors
    val amountColor = if (transaction.isExpense) colors.semanticRed else colors.semanticGreen
    val amountPrefix = if (transaction.isExpense) "-" else "+"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    (transaction.categoryColor ?: colors.textMuted).copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(transaction.categoryColor ?: colors.textMuted)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note,
                style = LedgeTextStyle.HeadingCard,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                transaction.categoryName?.let { name ->
                    Text(
                        text = name,
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
                    text = formatDate(transaction.date),
                    style = LedgeTextStyle.Caption,
                    color = colors.textMuted
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = "$amountPrefix\u20B9${formatAmount(transaction.amount)}",
            style = LedgeTextStyle.AmountMono,
            color = amountColor
        )
    }
}

@Composable
private fun UserDetailsRow(
    greeting: String,
    name: String,
    initials: String,
    showDot: Boolean
) {
    val colors = LedgeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$greeting,",
                color = colors.textMuted,
                style = LedgeTextStyle.BodySmall
            )
            Text(
                text = name,
                color = colors.textPrimary,
                style = LedgeTextStyle.HeadingScreen
            )
        }

        UserProfile(
            showDot = showDot,
            initials = initials
        )
    }
}

@Composable
private fun UserProfile(
    showDot: Boolean,
    initials: String
) {

    val colors = LedgeTheme.colors
    val onEvent = LocalEventSink.current

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .noRippleClickable(
                enabled = true,
                onClick = { onEvent(HomeUiEvent.ProfileClicked) }
            )
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.goldGlow),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colors.bgCard),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(colors.goldAccent, Color(0xFF8B6914)),
                                start = Offset.Zero,
                                end = Offset(
                                    Float.POSITIVE_INFINITY,
                                    Float.POSITIVE_INFINITY
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = colors.bgDeep,
                        style = TextStyle(
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                    )
                }
            }
        }

        if (showDot) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(colors.bgSurface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(colors.gold)
            )
        }
    }
}
