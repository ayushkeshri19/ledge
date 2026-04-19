package com.ayush.profile.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.profile.presentation.components.AccountDetailsRow
import com.ayush.profile.presentation.components.ProfileTopBar
import com.ayush.profile.presentation.components.SectionCard
import com.ayush.profile.presentation.components.SectionHeader
import com.ayush.profile.presentation.components.SecurityRow
import com.ayush.profile.presentation.components.SignOut
import com.ayush.profile.presentation.components.ThemeModeRow
import com.ayush.ui.components.LedgeDivider
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
                .padding(horizontal = 20.dp),
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

            stickyHeader {
                SectionHeader(title = "ACCOUNT", background = colors.bgDeep)
            }

            item {
                SectionCard {
                    AccountDetailsRow()

                    LedgeDivider()

                    SecurityRow()
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                SignOut(onSignOut = onSignOut)
            }
        }
    }
}