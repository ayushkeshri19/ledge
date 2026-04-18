package com.ayush.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.common.theme.ThemeMode
import com.ayush.ui.components.LedgeSegmentedToggle
import com.ayush.ui.components.SegmentOption
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

private val LocalEventSink = staticCompositionLocalOf<(ProfileEvent) -> Unit> { error { } }

@Composable
fun UserProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSignOut: () -> Unit
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

        ProfileContent(state = state, onBack = onBack, onSignOut = onSignOut)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileContent(
    state: ProfileState,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val onEvent = LocalEventSink.current
    val colors = LedgeTheme.colors

    Scaffold(
        topBar = {
            ProfileTopBar(onBack = onBack)
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            stickyHeader {
                SectionHeader(title = "APPEARANCE", background = colors.bgDeep)
            }
            item {
                SectionCard {
                    ThemeModeRow(
                        selected = state.themeMode,
                        onSelect = { onEvent(ProfileEvent.ThemeModeChanged(it)) }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                SignOut(onSignOut = onSignOut)
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
            .statusBarsPadding()
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
private fun SectionHeader(title: String, background: Color) {
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

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bgCard)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
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

@Composable
private fun SignOut(onSignOut: () -> Unit) {

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
