package com.ayush.profile.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ayush.common.theme.ThemeMode
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
internal fun ThemeModeRow(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val colors = LedgeTheme.colors
    val gold = colors.gold
    val options = remember(gold) {
        listOf(
            SegmentOption(ThemeMode.LIGHT, "Light", gold),
            SegmentOption(ThemeMode.DARK, "Dark", gold),
            SegmentOption(ThemeMode.SYSTEM, "System", gold)
        )
    }

    Column(modifier = Modifier.padding(vertical = 14.dp)) {
        Text(
            text = "Theme",
            style = LedgeTextStyle.BodySmall,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(8.dp))
        LedgeSegmentedToggle(
            options = options,
            selectedValue = selected,
            onSelect = onSelect
        )
    }
}
