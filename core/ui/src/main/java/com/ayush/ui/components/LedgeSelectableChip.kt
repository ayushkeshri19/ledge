package com.ayush.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgeSelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingDotColor: Color? = null
) {
    val colors = LedgeTheme.colors
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.gold else colors.borderSubtle,
        animationSpec = tween(200),
        label = "chipBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.goldDim else colors.bgCard,
        animationSpec = tween(200),
        label = "chipBg"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(LedgeRadius.pill))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(LedgeRadius.pill))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        leadingDotColor?.let { dotColor ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Text(
            text = label,
            style = LedgeTextStyle.BodySmall,
            color = if (isSelected) colors.gold else colors.textPrimary
        )
    }
}
