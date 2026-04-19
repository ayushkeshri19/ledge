package com.ayush.profile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
internal fun SignOut(onSignOut: () -> Unit) {

    val colors = LedgeTheme.colors

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.redDim,
            contentColor = colors.semanticRed
        ),
        onClick = onSignOut,
        shape = RoundedCornerShape(LedgeRadius.large),
        border = BorderStroke(
            width = 1.dp,
            color = colors.semanticRed
        ),
        interactionSource = null
    ) {
        Text(
            text = "Sign out",
            style = LedgeTextStyle.Button,
            color = colors.semanticRed
        )
    }
}