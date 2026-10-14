package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.AppScreen
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

/**
 * Represents one of the 4 primary navigation tabs in the Main Screen.
 */
sealed class MainNavigationSection(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    object Dashboard : MainNavigationSection(
        screen = AppScreen.DASHBOARD,
        title = "Dashboard",
        icon = Icons.Default.Dashboard,
        testTag = "nav_tab_dashboard"
    )

    object Donations : MainNavigationSection(
        screen = AppScreen.DONATIONS,
        title = "Donations",
        icon = Icons.Default.VolunteerActivism,
        testTag = "nav_tab_donations"
    )

    object Expenses : MainNavigationSection(
        screen = AppScreen.EXPENSES,
        title = "Expenses",
        icon = Icons.Default.ReceiptLong,
        testTag = "nav_tab_expenses"
    )

    object Pledges : MainNavigationSection(
        screen = AppScreen.PLEDGES,
        title = "Pledges",
        icon = Icons.Default.PendingActions,
        testTag = "nav_tab_pledges"
    )

    object Reports : MainNavigationSection(
        screen = AppScreen.REPORTS,
        title = "Reports",
        icon = Icons.Default.Assessment,
        testTag = "nav_tab_reports"
    )

    companion object {
        val sections = listOf(Dashboard, Donations, Expenses, Pledges, Reports)
    }
}

/**
 * Material 3 Bottom Navigation bar configured to switch
 * between Dashboard, Donations, Expenses, Pledges, and Reports.
 */
@Composable
fun MainBottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    pendingPledgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    NavigationBar(
        modifier = modifier.testTag("bottom_navigation_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        MainNavigationSection.sections.forEach { section ->
            val isSelected = when (section) {
                MainNavigationSection.Pledges -> currentScreen == AppScreen.PLEDGES || currentScreen == AppScreen.PENDING
                else -> currentScreen == section.screen
            }

            val activeColor = when (section) {
                MainNavigationSection.Expenses -> MaterialTheme.colorScheme.error
                MainNavigationSection.Pledges -> ColorPendingOrange
                MainNavigationSection.Reports -> SapphireNavy
                else -> SocietyTealPrimary
            }

            val localizedTitle = when (section) {
                MainNavigationSection.Dashboard -> strings.navDashboard
                MainNavigationSection.Donations -> strings.navDonations
                MainNavigationSection.Expenses -> strings.navExpenses
                MainNavigationSection.Pledges -> strings.navPending
                MainNavigationSection.Reports -> strings.navReports
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(section.screen) },
                icon = {
                    if (section == MainNavigationSection.Pledges && pendingPledgeCount > 0) {
                        BadgedBox(
                            modifier = Modifier.testTag("pledge_badge_box"),
                            badge = {
                                Badge(
                                    containerColor = ColorPendingOrange,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (pendingPledgeCount > 99) "99+" else "$pendingPledgeCount",
                                        modifier = Modifier.testTag("pledge_badge_count")
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = localizedTitle
                            )
                        }
                    } else {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = localizedTitle
                        )
                    }
                },
                label = {
                    Text(
                        text = localizedTitle,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                modifier = Modifier.testTag(section.testTag),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    indicatorColor = activeColor.copy(alpha = 0.15f)
                )
            )
        }
    }
}

/**
 * Top-level Main Compose Screen with a Bottom Navigation bar for
 * Dashboard, Donations, Expenses, and Pledges sections.
 */
@Composable
fun MainScreen(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    pendingPledgeCount: Int = 0,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = {
            MainBottomNavigationBar(
                currentScreen = currentScreen,
                onNavigate = onNavigate,
                pendingPledgeCount = pendingPledgeCount
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content(paddingValues)
        }
    }
}
