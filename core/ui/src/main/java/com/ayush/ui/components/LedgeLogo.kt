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
import com.ayush.ui.theme.BgCard2
import com.ayush.ui.theme.BorderFocus
import com.ayush.ui.theme.DmSerifFontFamily
import com.ayush.ui.theme.Gold
import com.ayush.ui.theme.GoldGlow
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.TextPrimary

@Composable
fun LedgeLogo(
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(GoldGlow, Color.Transparent),
                        ),
                        shape = RoundedCornerShape(50),
                    )
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(BgCard2, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderFocus, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "L",
                    style = LedgeTextStyle.HeadingScreen.copy(
                        fontFamily = DmSerifFontFamily,
                        fontSize = 28.sp,
                        color = Gold,
                    ),
                )
            }
        }

        Text(
            text = "Ledge",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 26.sp,
                color = TextPrimary,
            ),
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = LedgeTextStyle.LabelCaps.copy(
                    fontSize = 9.sp,
                    color = Gold,
                    letterSpacing = 2.5.sp,
                ),
            )
        }
    }
}