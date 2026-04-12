package com.ayush.transactions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.ui.theme.BgCard
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.SemanticRed
import com.ayush.ui.theme.TextMuted
import com.ayush.ui.theme.TextPrimary

@Composable
internal fun DeleteConfirmationSheet(
    transaction: Transaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountPrefix = if (isExpense) "-" else "+"
    val amountColor = if (isExpense) SemanticRed else com.ayush.ui.theme.SemanticGreen

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Delete Transaction",
            style = LedgeTextStyle.HeadingScreen,
            color = TextPrimary,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "This action cannot be undone.",
            style = LedgeTextStyle.BodySmall,
            color = TextMuted,
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LedgeRadius.medium))
                .background(BgCard)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note,
                    style = LedgeTextStyle.HeadingCard,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${formatDate(transaction.date)}, ${formatTime(transaction.date)}",
                    style = LedgeTextStyle.Caption,
                    color = TextMuted,
                )
            }
            Text(
                text = "$amountPrefix\u20B9${formatAmount(transaction.amount)}",
                style = LedgeTextStyle.AmountMono,
                color = amountColor,
            )
        }

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(color = BgCard)

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
        ) {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextMuted),
            ) {
                Text(text = "Cancel", style = LedgeTextStyle.Button)
            }

            Spacer(Modifier.width(8.dp))

            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = SemanticRed),
            ) {
                Text(text = "Delete", style = LedgeTextStyle.Button)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
