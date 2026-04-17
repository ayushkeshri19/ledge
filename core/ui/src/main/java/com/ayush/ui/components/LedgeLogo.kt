package com.ayush.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayush.ui.theme.DmSerifFontFamily
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgeLogo(
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = LedgeTheme.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.goldGlow, Color.Transparent),
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(colors.bgCard2, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.borderFocus, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "L",
                    style = LedgeTextStyle.HeadingScreen.copy(
                        fontFamily = DmSerifFontFamily,
                        fontSize = 28.sp,
                        color = colors.gold
                    )
                )
            }
        }

        Text(
            text = "Ledge",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 26.sp,
                color = colors.textPrimary
            )
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = LedgeTextStyle.Caption.copy(
                    color = colors.textMuted2
                )
            )
        }
    }
}
