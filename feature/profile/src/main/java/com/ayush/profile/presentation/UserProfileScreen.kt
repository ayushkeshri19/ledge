package com.ayush.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.common.theme.ThemeMode
import com.ayush.profile.presentation.components.ProfileSection
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

private val LocalEventSink = staticCompositionLocalOf<(ProfileEvent) -> Unit> { error { } }

@Composable
fun UserProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalEventSink provides viewModel::onEvent) {
        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect {
                when (it) {
                    else -> {}
                }
            }
        }

        ProfileContent(state) { onBack() }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    onBack: () -> Unit
) {
    val colors = LedgeTheme.colors
    val onEvent = LocalEventSink.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
            .statusBarsPadding()
    ) {
        ProfileTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            ProfileSection(title = "APPEARANCE") {
                ThemeModeRow(
                    selected = state.themeMode,
                    onSelect = { onEvent(ProfileEvent.ThemeModeChanged(it)) }
                )
            }
        }
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    val colors = LedgeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = colors.textPrimary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Spacer(Modifier.height(0.dp))
        Text(
            text = "Profile",
            style = LedgeTextStyle.HeadingScreen,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun ThemeModeRow(
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

    Column {
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
