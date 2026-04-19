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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ayush.security.domain.models.BiometricStatus
import com.ayush.ui.R
import com.ayush.ui.components.noRippleClickable
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
internal fun BiometricToggleRow(
    status: BiometricStatus,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onEnrollClick: () -> Unit,
    showDivider: Boolean = false
) {
    val colors = LedgeTheme.colors

    val subtitle = when (status) {
        BiometricStatus.AVAILABLE -> "Locks as you leave the app"
        BiometricStatus.NONE_ENROLLED -> "Set up fingerprint or face unlock in device settings"
        BiometricStatus.HARDWARE_UNAVAILABLE -> "Sensor temporarily unavailable"
        BiometricStatus.NO_HARDWARE,
        BiometricStatus.UNSUPPORTED -> "Not supported on this device"
    }

    val isEnrollPath = status == BiometricStatus.NONE_ENROLLED
    val rowModifier = Modifier
        .fillMaxWidth()
        .let { if (isEnrollPath) it.noRippleClickable { onEnrollClick() } else it }

    Column(modifier = rowModifier) {
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
                    painter = painterResource(R.drawable.ic_security),
                    contentDescription = null,
                    tint = colors.textMuted3,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unlock with biometric",
                    style = LedgeTextStyle.Body.copy(fontWeight = FontWeight.Medium),
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = LedgeTextStyle.BodySmall,
                    color = colors.textMuted2
                )
            }

            when (status) {
                BiometricStatus.AVAILABLE -> Switch(
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

                BiometricStatus.NONE_ENROLLED -> Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )

                BiometricStatus.HARDWARE_UNAVAILABLE,
                BiometricStatus.NO_HARDWARE,
                BiometricStatus.UNSUPPORTED -> Switch(
                    checked = false,
                    onCheckedChange = null,
                    enabled = false
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)
        }
    }
}
