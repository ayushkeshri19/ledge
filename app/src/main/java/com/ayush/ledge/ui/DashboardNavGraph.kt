package com.ayush.ledge.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ayush.budget.presentation.BudgetScreen
import com.ayush.home.presentation.HomeScreen
import com.ayush.transactions.presentation.AddTransactionScreen
import com.ayush.ui.R
import com.ayush.ui.components.FabButton
import com.ayush.ui.theme.LedgeTheme
import com.ayush.ui.theme.NavLabelStyle
import kotlinx.serialization.Serializable
import com.ayush.transactions.presentation.TransactionsScreen as TransactionsListScreen

sealed interface DashboardBottomNavItems {
    @get:DrawableRes
    val icon: Int
    val label: String
    val route: DashboardRoute

    @Serializable
    data object Home : DashboardBottomNavItems {
        override val label: String get() = "Home"
        override val icon: Int get() = R.drawable.ic_home
        override val route: DashboardRoute get() = DashboardRoute.Home
    }

    @Serializable
    data object Transactions : DashboardBottomNavItems {
        override val label: String get() = "Txns"
        override val icon: Int get() = R.drawable.ic_transaction
        override val route: DashboardRoute get() = DashboardRoute.Transactions
    }

    @Serializable
    data object Budget : DashboardBottomNavItems {
        override val label: String get() = "Budget"
        override val icon: Int get() = R.drawable.ic_budget
        override val route: DashboardRoute get() = DashboardRoute.Budget
    }

    @Serializable
    data object More : DashboardBottomNavItems {
        override val label: String get() = "More"
        override val icon: Int get() = R.drawable.ic_more
        override val route: DashboardRoute get() = DashboardRoute.More
    }

    companion object {
        val items: List<DashboardBottomNavItems> = listOf(Home, Transactions, Budget, More)
        val Saver: Saver<DashboardBottomNavItems, String> = Saver(
            save = { it::class.qualifiedName },
            restore = { name -> items.firstOrNull { it::class.qualifiedName == name } ?: Home }
        )
    }
}

@Composable
internal fun DashboardNavGraph(
    onSignOut: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val backStack = rememberNavBackStack(DashboardRoute.Home)

    val selectedTab = when (backStack.firstOrNull()) {
        DashboardRoute.Home -> DashboardBottomNavItems.Home
        DashboardRoute.Transactions -> DashboardBottomNavItems.Transactions
        DashboardRoute.Budget -> DashboardBottomNavItems.Budget
        DashboardRoute.More -> DashboardBottomNavItems.More
        else -> DashboardBottomNavItems.Home
    }

    fun selectTab(tab: DashboardBottomNavItems) {
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        if (backStack.lastOrNull() != tab.route) {
            backStack[backStack.lastIndex] = tab.route
        }
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    val showBottomBar = backStack.lastOrNull() != DashboardRoute.AddTransaction

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                LedgeBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = ::selectTab,
                    onFabClick = { backStack.add(DashboardRoute.AddTransaction) },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBottomBar) padding else PaddingValues())
        ) {
            NavDisplay(
                backStack = backStack,
                entryProvider = entryProvider {
                    entry<DashboardRoute.Home> {
                        HomeScreen(
                            onNavigateToProfile = onNavigateToProfile,
                            onNavigateToTransactions = {
                                selectTab(DashboardBottomNavItems.Transactions)
                            }
                        )
                    }
                    entry<DashboardRoute.Transactions> { TransactionsListScreen() }
                    entry<DashboardRoute.Budget> { BudgetScreen() }
                    entry<DashboardRoute.More> { MoreScreen(onSignOut = onSignOut) }
                    entry<DashboardRoute.AddTransaction> {
                        AddTransactionScreen(onBack = ::pop)
                    }
                },
                onBack = ::pop,
            )
        }
    }
}

@Composable
private fun LedgeBottomBar(
    selectedTab: DashboardBottomNavItems,
    onTabSelected: (DashboardBottomNavItems) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val leftTabs = DashboardBottomNavItems.items.take(2)
    val rightTabs = DashboardBottomNavItems.items.drop(2)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val colors = LedgeTheme.colors
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgSurface.copy(alpha = 0.95f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(top = 14.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top,
                ) {
                    leftTabs.forEach { tab ->
                        BottomNavItem(
                            tab = tab,
                            isSelected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    FabButton(
                        onClick = onFabClick,
                        modifier = Modifier
                            .weight(1f)
                            .offset(y = (-30).dp),
                    )

                    rightTabs.forEach { tab ->
                        BottomNavItem(
                            tab = tab,
                            isSelected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: DashboardBottomNavItems,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LedgeTheme.colors
    val tintColor = if (isSelected) colors.gold else colors.textMuted

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(
                    color = if (isSelected) colors.gold else Color.Transparent,
                    shape = RoundedCornerShape(2.dp),
                ),
        )

        Icon(
            painter = painterResource(id = tab.icon),
            contentDescription = tab.label,
            tint = tintColor,
            modifier = Modifier.size(22.dp),
        )

        Text(
            text = tab.label.uppercase(),
            color = tintColor,
            style = NavLabelStyle
        )
    }
}

@Composable
private fun MoreScreen(onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "More",
            style = MaterialTheme.typography.headlineLarge,
            color = LedgeTheme.colors.textPrimary,
        )

        Spacer(Modifier.height(32.dp))

        OutlinedButton(onClick = onSignOut) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text("Sign Out")
            }
        }
    }
}
