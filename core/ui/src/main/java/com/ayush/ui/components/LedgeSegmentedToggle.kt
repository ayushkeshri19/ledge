package com.ayush.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

data class SegmentOption<T>(
    val value: T,
    val label: String,
    val selectedColor: Color
)

@Composable
fun <T> LedgeSegmentedToggle(
    options: List<SegmentOption<T>>,
    selectedValue: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LedgeTheme.colors.bgDeep,
    selectedContainerColor: Color = LedgeTheme.colors.bgCard,
    unselectedTextColor: Color = LedgeTheme.colors.textMuted
) {
    if (options.isEmpty()) return

    val selectedIndex = options.indexOfFirst { it.value == selectedValue }.coerceAtLeast(0)

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "segmentIndex"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LedgeRadius.medium))
            .background(containerColor)
            .padding(4.dp)
    ) {
        val itemWidth = maxWidth / options.size

        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .offset(x = itemWidth * animatedIndex)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(LedgeRadius.small))
                    .background(selectedContainerColor)
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) option.selectedColor else unselectedTextColor,
                    animationSpec = tween(200),
                    label = "segmentTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .noRippleClickable(
                            onClick = { onSelect(option.value) },
                            enabled = true
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.label,
                        style = LedgeTextStyle.Button,
                        color = textColor
                    )
                }
            }
        }
    }
}
