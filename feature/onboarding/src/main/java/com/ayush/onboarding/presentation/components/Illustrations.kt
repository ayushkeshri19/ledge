package com.ayush.onboarding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
internal fun BalanceIllustration() {
    val colors = LedgeTheme.colors
    Box(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(LedgeRadius.xxl))
            .background(colors.bgCard2)
            .border(1.dp, colors.goldDim, RoundedCornerShape(LedgeRadius.xxl))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.goldGlow, Color.Transparent),
                        radius = 180f
                    )
                )
        )
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                text = "NET BALANCE",
                style = LedgeTextStyle.LabelCaps.copy(color = colors.textMuted)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "₹12,480",
                style = LedgeTextStyle.BalanceHero.copy(
                    color = colors.gold,
                    fontSize = 38.sp,
                    lineHeight = 38.sp
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "+₹820 this week",
                style = LedgeTextStyle.BodySmall.copy(
                    color = colors.semanticGreen,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
internal fun CategoriesIllustration() {
    val colors = LedgeTheme.colors
    val rows = listOf(
        Triple("Food", 64, colors.gold),
        Triple("Transport", 41, colors.semanticBlue),
        Triple("Shopping", 112, colors.semanticRed)
    )
    Column(
        modifier = Modifier.width(260.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { (name, pct, color) ->
            val over = pct > 100
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LedgeRadius.medium))
                    .background(colors.bgCard)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = name,
                            style = LedgeTextStyle.HeadingCard.copy(
                                color = colors.textPrimary,
                                fontSize = 13.sp
                            )
                        )
                    }
                    Text(
                        text = "$pct%",
                        style = LedgeTextStyle.BodySmall.copy(
                            color = if (over) colors.semanticRed else colors.textMuted2,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(LedgeRadius.pill))
                        .background(colors.bgDeep)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = minOf(pct, 100) / 100f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(LedgeRadius.pill))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
internal fun AlertsIllustration() {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier.width(260.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AlertCard(
            label = "BUDGET ALERT",
            labelColor = colors.semanticRed,
            tint = colors.redDim,
            title = "Shopping is over by ₹600",
            subtitle = "3 days left in this cycle"
        )
        AlertCard(
            label = "ON TRACK",
            labelColor = colors.semanticGreen,
            tint = colors.greenDim,
            title = "Food budget pacing well",
            subtitle = "₹1,800 left to spend"
        )
    }
}

@Composable
internal fun AlertCard(
    label: String,
    labelColor: Color,
    tint: Color,
    title: String,
    subtitle: String
) {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LedgeRadius.medium))
            .background(tint)
            .border(1.dp, labelColor.copy(alpha = 0.2f), RoundedCornerShape(LedgeRadius.medium))
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = LedgeTextStyle.LabelCaps.copy(
                color = labelColor,
                fontSize = 9.sp,
                letterSpacing = 2.sp
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            style = LedgeTextStyle.HeadingCard.copy(
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = LedgeTextStyle.BodySmall.copy(color = colors.textMuted2, fontSize = 11.sp)
        )
    }
}
