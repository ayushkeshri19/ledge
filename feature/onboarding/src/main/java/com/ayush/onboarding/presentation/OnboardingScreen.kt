package com.ayush.onboarding.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ayush.onboarding.presentation.components.AlertsIllustration
import com.ayush.onboarding.presentation.components.BalanceIllustration
import com.ayush.onboarding.presentation.components.CategoriesIllustration
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.noRippleClickable
import com.ayush.ui.theme.DmSerifFontFamily
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 3

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {

    val viewModel = hiltViewModel<OnboardingViewModel>()

    val colors = LedgeTheme.colors
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                OnboardingSideEffect.NavigateToAuth -> onComplete()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(16.dp))

            StepIndicator(
                current = pagerState.currentPage,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingSlide(page = page)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LedgePrimaryButton(
                    text = if (pagerState.currentPage < PAGE_COUNT - 1) "Continue" else "Get started",
                    onClick = {
                        if (pagerState.currentPage < PAGE_COUNT - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            viewModel.onEvent(OnboardingEvent.Completed)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (pagerState.currentPage < PAGE_COUNT - 1) {
                    Text(
                        text = "Skip",
                        style = LedgeTextStyle.Body.copy(color = colors.textMuted),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .noRippleClickable { viewModel.onEvent(OnboardingEvent.Completed) },
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(Modifier.height(44.dp))
                }
            }
        }
    }
}

@Composable
private fun OnboardingSlide(page: Int) {
    val colors = LedgeTheme.colors
    val capsLabel = when (page) {
        0 -> "TRACK"
        1 -> "UNDERSTAND"
        else -> "STAY AHEAD"
    }
    val title = when (page) {
        0 -> "Every transaction, one place."
        1 -> "See where it actually goes."
        else -> "Never blown by surprise."
    }
    val body = when (page) {
        0 -> "Connect once. Ledge keeps a running tally of what comes in and what goes out, no spreadsheets required."
        1 -> "Category-level budgets and a weekly rhythm — so you know where your month is headed by the third."
        else -> "Set a ceiling per category. Ledge quietly nudges you when the pace looks off."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (page) {
                0 -> BalanceIllustration()
                1 -> CategoriesIllustration()
                else -> AlertsIllustration()
            }
        }

        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = capsLabel,
                style = LedgeTextStyle.LabelCaps.copy(
                    color = colors.gold,
                    fontSize = 10.sp,
                    letterSpacing = 2.5.sp
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                color = colors.textPrimary,
                fontFamily = DmSerifFontFamily,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = body,
                style = LedgeTextStyle.Body.copy(color = colors.textMuted2, lineHeight = 21.sp)
            )
        }
    }
}

@Composable
private fun StepIndicator(
    current: Int,
    modifier: Modifier = Modifier
) {
    val colors = LedgeTheme.colors
    Row(
        modifier = modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(PAGE_COUNT) { i ->
            val width by animateDpAsState(
                targetValue = if (i == current) 24.dp else 6.dp,
                animationSpec = tween(durationMillis = 300),
                label = "step-width"
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(6.dp)
                    .clip(RoundedCornerShape(LedgeRadius.pill))
                    .background(if (i == current) colors.gold else colors.borderMid)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewOnboardingScreen() {
    OnboardingScreen {

    }
}