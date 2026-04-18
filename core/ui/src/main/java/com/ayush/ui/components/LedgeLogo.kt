package com.ayush.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayush.ui.R
import com.ayush.ui.theme.DmSerifFontFamily
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgeLogo() {

    val colors = LedgeTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_ledge),
            contentDescription = "app_logo",
            modifier = Modifier.size(80.dp)

        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "ledge",
                style = LedgeTextStyle.HeadingScreen.copy(
                    fontSize = 26.sp,
                    color = colors.textPrimary,
                    fontFamily = DmSerifFontFamily
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "know where it goes",
                style = LedgeTextStyle.Caption.copy(
                    color = colors.textMuted,
                    fontFamily = DmSerifFontFamily
                )
            )
        }
    }
}
