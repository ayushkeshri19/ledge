package com.ayush.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
internal fun SmsAutoDetectToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val colors = LedgeTheme.colors

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(LedgeRadius.small))
                .background(colors.bgCard2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sms,
                contentDescription = null,
                tint = colors.textMuted3,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Auto-detect transactions",
                style = LedgeTextStyle.Body.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary
            )
            Text(
                text = "Read incoming bank SMS to log transactions",
                style = LedgeTextStyle.BodySmall,
                color = colors.textMuted2
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.bgDeep,
                checkedTrackColor = colors.gold,
                uncheckedThumbColor = colors.textMuted3,
                uncheckedTrackColor = colors.bgCard2,
                uncheckedBorderColor = colors.borderSubtle
            )
        )
    }
}
