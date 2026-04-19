package com.ayush.ledge.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.ayush.security.domain.models.BiometricResult
import com.ayush.security.domain.repository.BiometricAuthenticator
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import kotlinx.coroutines.launch

@Composable
fun LockScreen(
    biometricAuthenticator: BiometricAuthenticator,
    onUnlock: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = LedgeTheme.colors
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    val view = LocalView.current

    val authenticate: () -> Unit = authenticate@{
        if (isAuthenticating) return@authenticate
        isAuthenticating = true
        errorMessage = null
        scope.launch {
            val activity = context as FragmentActivity
            val result = biometricAuthenticator.authenticate(
                activity = activity,
                title = "Unlock Ledge",
                subtitle = "Confirm it's you to continue"
            )
            isAuthenticating = false
            when (result) {
                is BiometricResult.Success -> {
                    onUnlock()
                    view.postInvalidateOnAnimation()
                }
                is BiometricResult.UserCancelled -> {
                    /** stay locked */
                }

                is BiometricResult.Error -> errorMessage = result.message
                is BiometricResult.Failed -> Unit
            }
        }
    }

    LaunchedEffect(Unit) { authenticate() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = colors.gold,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Ledge is locked",
                style = LedgeTextStyle.HeadingScreen,
                color = colors.textPrimary
            )
            Text(
                text = "Authenticate to continue",
                style = LedgeTextStyle.BodySmall,
                color = colors.textMuted2
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = LedgeTextStyle.Caption,
                    color = colors.semanticRed
                )
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = authenticate,
                enabled = !isAuthenticating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.gold,
                    contentColor = colors.bgDeep
                )
            ) {
                Text("Authenticate", style = LedgeTextStyle.Button)
            }
            TextButton(onClick = onSignOut) {
                Text(
                    "Sign out",
                    style = LedgeTextStyle.BodySmall,
                    color = colors.semanticRed
                )
            }
        }
    }
}