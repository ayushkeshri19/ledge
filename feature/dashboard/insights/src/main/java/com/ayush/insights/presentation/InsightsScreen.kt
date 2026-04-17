package com.ayush.insights.presentation

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ayush.common.utils.formatAmount
import com.ayush.ui.components.charts.LedgePieChart
import com.ayush.ui.components.charts.PieChartSegment
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

private data class InsightsCategory(
    val name: String,
    val amount: Double,
    val color: Color,
)

private val MOCK_CATEGORIES = listOf(
    InsightsCategory("Food & Dining", 3200.0, Color(0xFFC9A84C)),
    InsightsCategory("Transport", 1240.0, Color(0xFF5B8DEF)),
    InsightsCategory("Shopping", 5600.0, Color(0xFFE05A5A)),
    InsightsCategory("Bills & Utilities", 2269.0, Color(0xFF9B72CF)),
    InsightsCategory("Entertainment", 340.0, Color(0xFF4ECBA4)),
)

@Composable
fun InsightsScreen() {
    val colors = LedgeTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Insights",
                style = LedgeTextStyle.HeadingScreen,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
        }
        item {
            SpendingByCategoryCard(categories = MOCK_CATEGORIES)
        }
    }
}

@Composable
private fun SpendingByCategoryCard(categories: List<InsightsCategory>) {
    val colors = LedgeTheme.colors
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val totalExpense = categories.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.bgCard)
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
                        label = cat.name,
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

        categories.forEachIndexed { index, category ->
            val isSelected = index == selectedIndex
            val percent = if (totalExpense > 0) category.amount / totalExpense else 0.0
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
                    text = category.name,
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
                    text = "${(percent * 100).toInt()}%",
                    style = LedgeTextStyle.Caption,
                    color = colors.textMuted
                )
            }
        }
    }
}
