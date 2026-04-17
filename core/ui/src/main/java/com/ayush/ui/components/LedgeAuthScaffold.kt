package com.ayush.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.BgDeep

@Composable
fun LedgeAuthScaffold(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep),
    ) {
        Box(
            modifier = Modifier
                .size(width = 500.dp, height = 400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-160).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x12C9A84C),
                            Color.Transparent,
                        ),
                        radius = 700f,
                    )
                )
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}