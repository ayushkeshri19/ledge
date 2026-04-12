package com.ayush.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.Gold
import com.ayush.ui.theme.GoldDim
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle

@Composable
fun LedgeFilterChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(LedgeRadius.pill))
            .background(GoldDim)
            .border(1.dp, Gold, RoundedCornerShape(LedgeRadius.pill))
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = LedgeTextStyle.Caption, color = Gold)
        IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove filter",
                tint = Gold,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
