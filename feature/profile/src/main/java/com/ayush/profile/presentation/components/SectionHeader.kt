package com.ayush.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
internal fun SectionHeader(title: String, background: Color) {
    val colors = LedgeTheme.colors
    Text(
        text = title,
        style = LedgeTextStyle.LabelCaps,
        color = colors.textMuted,
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(start = 4.dp, top = 8.dp, bottom = 10.dp)
    )
}
