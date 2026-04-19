package com.ayush.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgeDivider(
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LedgeTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = colors.borderSubtle,
            thickness = 1.dp
        )
        label?.let {
            Text(
                text = label,
                style = LedgeTextStyle.Caption.copy(
                    color = colors.textMuted,
                    letterSpacing = 0.4.sp
                ),
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = colors.borderSubtle,
                thickness = 1.dp
            )
        }
    }
}
