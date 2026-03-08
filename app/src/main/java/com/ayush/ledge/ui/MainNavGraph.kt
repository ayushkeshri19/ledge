package com.ayush.ledge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ayush.ui.theme.LedgeTextStyle

private enum class Tab(
    val route: MainRoute,
    val icon: ImageVector,
    val label: String,
) {
    HOME(MainRoute.Home, Icons.Filled.Home, "Home"),
    TRANSACTIONS(MainRoute.Transactions, Icons.Filled.Transactions, "Transactions"),
    BUDGET(MainRoute.Budget, Icons.Filled.Budget, "Budget"),
    MORE(MainRoute.More, Icons.Filled.MoreHoriz, "More")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainNavGraph(onSignOut: () -> Unit) {
    val backStack = rememberNavBackStack(MainRoute.Home)

    val isOnDetailScreen = backStack.size > 1

    val selectedTab = when (backStack.firstOrNull()) {
        MainRoute.Home -> Tab.HOME
        MainRoute.Transactions -> Tab.TRANSACTIONS
        MainRoute.Budget -> Tab.BUDGET
        MainRoute.More -> Tab.MORE
        else -> Tab.HOME
    }

    fun selectTab(tab: Tab) {
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        if (backStack.lastOrNull() != tab.route) {
            backStack[backStack.lastIndex] = tab.route
        }
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    Scaffold(
        topBar = {
            if (isOnDetailScreen) {
                CenterAlignedTopAppBar(
                    title = { Text("Add Transaction") },
                    navigationIcon = {
                        IconButton(onClick = ::pop) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (!isOnDetailScreen) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectTab(tab) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, style = LedgeTextStyle.Caption) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isOnDetailScreen) {
                FloatingActionButton(
                    onClick = { backStack.add(MainRoute.AddTransaction) },
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NavDisplay(
                backStack = backStack,
                entryProvider = entryProvider {
                    entry<MainRoute.Home> { HomeScreen() }
                    entry<MainRoute.Analytics> { AnalyticsScreen() }
                    entry<MainRoute.Profile> { ProfileScreen(onSignOut = onSignOut) }
                    entry<MainRoute.AddTransaction> { AddTransactionScreen() }
                },
                onBack = ::pop,
            )
        }
    }
}

// ── Placeholder screens (replace with real feature modules later) ───

@Composable
private fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Home,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("Home", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Your transactions and balance will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AnalyticsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Analytics,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("Analytics", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Spending insights and charts will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileScreen(onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Person,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = onSignOut) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Text("Sign Out")
            }
        }
    }
}

@Composable
private fun AddTransactionScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text("Add Transaction", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Transaction form will go here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
