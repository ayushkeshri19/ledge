package com.ayush.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgeSyncErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgeTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.semanticRed.copy(alpha = 0.10f),
                shape = RoundedCornerShape(LedgeRadius.medium)
            )
            .border(
                width = 1.dp,
                color = colors.semanticRed.copy(alpha = 0.35f),
                shape = RoundedCornerShape(LedgeRadius.medium)
            )
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = colors.semanticRed,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = message,
            style = LedgeTextStyle.BodySmall.copy(color = colors.textPrimary),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRetry) {
            Text(
                text = "Retry",
                style = LedgeTextStyle.BodySmall.copy(color = colors.gold)
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Dismiss",
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F)
@Composable
private fun LedgeSyncErrorBannerPreview() {
    LedgeTheme {
        LedgeSyncErrorBanner(
            message = "Couldn't refresh data. Check your connection and try again.",
            onRetry = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
