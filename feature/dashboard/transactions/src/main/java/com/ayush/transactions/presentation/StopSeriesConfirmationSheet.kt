package com.ayush.transactions.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
internal fun StopSeriesConfirmationSheet(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LedgeTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Stop Recurring Series",
            style = LedgeTextStyle.HeadingScreen,
            color = colors.textPrimary
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "No new transactions will be generated. Past entries remain in your history.",
            style = LedgeTextStyle.BodySmall,
            color = colors.textMuted
        )

        Spacer(Modifier.height(20.dp))

        HorizontalDivider(color = colors.bgCard)

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.textMuted)
            ) {
                Text(text = "Cancel", style = LedgeTextStyle.Button)
            }

            Spacer(Modifier.width(8.dp))

            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.semanticRed)
            ) {
                Text(text = "Stop", style = LedgeTextStyle.Button)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
