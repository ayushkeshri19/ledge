package com.ayush.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.BgCard
import com.ayush.ui.theme.BgDeep
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.TextMuted

data class SegmentOption<T>(
    val value: T,
    val label: String,
    val selectedColor: Color,
)

@Composable
fun <T> LedgeSegmentedToggle(
    options: List<SegmentOption<T>>,
    selectedValue: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = BgDeep,
    selectedContainerColor: Color = BgCard,
    unselectedTextColor: Color = TextMuted,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LedgeRadius.medium))
            .background(containerColor)
            .padding(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = selectedValue == option.value
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) selectedContainerColor else containerColor,
                animationSpec = tween(200),
                label = "segmentBg",
            )
            val textColor = if (isSelected) option.selectedColor else unselectedTextColor

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(LedgeRadius.small))
                    .background(bgColor)
                    .clickable { onSelect(option.value) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.label,
                    style = LedgeTextStyle.Button,
                    color = textColor,
                )
            }
        }
    }
}
