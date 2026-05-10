package com.ayush.sms.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ayush.common.utils.formatAmount
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.parser.TransactionType
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
internal fun PendingTransactionCard(
    item: PendingTransaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgeTheme.colors

    val amountBody = item.amountFormatted.takeIf { it.isNotBlank() }
        ?: "₹${formatAmount(item.amount)}"
    val signedAmount = when (item.type) {
        TransactionType.CREDIT -> "+$amountBody"
        TransactionType.DEBIT -> "−$amountBody"
    }
    val amountColor = when (item.type) {
        TransactionType.CREDIT -> colors.semanticGreen
        TransactionType.DEBIT -> colors.textPrimary
    }
    val merchant = item.merchant?.takeIf { it.isNotBlank() } ?: "Unknown merchant"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LedgeRadius.medium))
            .background(colors.bgCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(LedgeRadius.medium))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = signedAmount,
                style = LedgeTextStyle.AmountMedium,
                color = amountColor
            )
            Spacer(Modifier.weight(1f))
            if (item.dateFormatted.isNotBlank()) {
                Text(
                    text = item.dateFormatted,
                    style = LedgeTextStyle.Caption,
                    color = colors.textMuted
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = merchant,
            style = LedgeTextStyle.HeadingCard,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (item.categoryLabel != null || item.accountLabel != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item.categoryLabel?.let { CategoryChip(it) }
                item.accountLabel?.let {
                    Text(
                        text = it,
                        style = LedgeTextStyle.BodySmall,
                        color = colors.textMuted2
                    )
                }
            }
        }

        if (item.rawSnippet.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = item.rawSnippet,
                style = LedgeTextStyle.Caption,
                color = colors.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionIconButton(
                icon = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = colors.semanticRed,
                onClick = onDismiss
            )
            ActionIconButton(
                icon = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = colors.textMuted2,
                onClick = onEdit
            )
            ActionIconButton(
                icon = Icons.Default.Check,
                contentDescription = "Confirm",
                tint = colors.semanticGreen,
                onClick = onConfirm
            )
        }
    }
}

@Composable
private fun CategoryChip(label: String) {
    val colors = LedgeTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(LedgeRadius.pill))
            .background(colors.bgCard2)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = LedgeTextStyle.BodySmall,
            color = colors.textMuted2
        )
    }
}

@Composable
private fun ActionIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = tint
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}