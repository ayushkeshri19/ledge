package com.ayush.budget.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ayush.budget.domain.models.BudgetWithSpent
import com.ayush.budget.domain.models.Category
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSecondaryButton
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.LedgeSelectableChip
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

private enum class BudgetType(val label: String) {
    OVERALL("Overall"),
    CATEGORY("Category"),
}

@Composable
fun AddEditBudgetSheet(
    categories: List<Category>,
    editingBudget: BudgetWithSpent?,
    onSave: (BudgetEvent.SaveBudget) -> Unit,
    onDelete: ((Long) -> Unit)?,
    onDismiss: () -> Unit,
) {
    val colors = LedgeTheme.colors
    val isEditing = editingBudget != null

    var budgetType by remember {
        mutableStateOf(
            if (editingBudget?.budget?.categoryId == null) BudgetType.OVERALL
            else BudgetType.CATEGORY
        )
    }
    var selectedCategoryId by remember {
        mutableLongStateOf(editingBudget?.budget?.categoryId ?: -1L)
    }
    var amountText by remember {
        mutableStateOf(
            editingBudget?.budget?.amount?.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    var threshold by remember {
        mutableFloatStateOf(editingBudget?.budget?.warningThreshold?.toFloat() ?: 80f)
    }

    val gold = colors.gold
    val typeOptions = remember(gold) {
        BudgetType.entries.map { SegmentOption(it, it.label, gold) }
    }

    val canSave = amountText.toDoubleOrNull()?.let { it > 0 } == true
            && (budgetType == BudgetType.OVERALL || selectedCategoryId >= 0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = if (isEditing) "Edit Budget" else "New Budget",
            style = LedgeTextStyle.HeadingScreen,
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(20.dp))

        if (!isEditing) {
            LedgeSegmentedToggle(
                options = typeOptions,
                selectedValue = budgetType,
                onSelect = { budgetType = it },
            )
            Spacer(Modifier.height(16.dp))
        }

        if (budgetType == BudgetType.CATEGORY) {
            Text(
                text = "CATEGORY",
                style = LedgeTextStyle.LabelCaps,
                color = colors.textMuted,
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp),
            ) {
                items(categories) { category ->
                    LedgeSelectableChip(
                        label = category.name,
                        isSelected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        leadingDotColor = category.color,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        LedgeTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
            label = "MONTHLY LIMIT",
            placeholder = "e.g. 5000",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "ALERT THRESHOLD",
            style = LedgeTextStyle.LabelCaps,
            color = colors.textMuted,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Alert me at ${threshold.toInt()}% of budget",
            style = LedgeTextStyle.BodySmall,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Slider(
            value = threshold,
            onValueChange = { threshold = it },
            valueRange = 50f..95f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = colors.gold,
                activeTrackColor = colors.gold,
                inactiveTrackColor = colors.gold.copy(alpha = 0.2f),
            ),
        )

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = if (isEditing) "Update Budget" else "Save Budget",
            enabled = canSave,
            onClick = {
                onSave(
                    BudgetEvent.SaveBudget(
                        categoryId = if (budgetType == BudgetType.OVERALL) null else selectedCategoryId,
                        amount = amountText.toDouble(),
                        warningThreshold = threshold.toInt(),
                    )
                )
            },
        )

        if (isEditing && onDelete != null) {
            Spacer(Modifier.height(12.dp))
            LedgeSecondaryButton(
                text = "Delete Budget",
                onClick = { onDelete(editingBudget!!.budget.id) },
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
