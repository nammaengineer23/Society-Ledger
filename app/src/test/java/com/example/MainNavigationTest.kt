package com.example

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.screens.MainBottomNavigationBar
import com.example.ui.screens.MainScreen
import com.example.ui.theme.SocietyManagementTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `bottom navigation bar displays all sections including reports`() {
        var currentScreen by mutableStateOf(AppScreen.DASHBOARD)

        composeTestRule.setContent {
            SocietyManagementTheme {
                MainBottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    pendingPledgeCount = 5
                )
            }
        }

        // Verify all 5 tabs exist and are displayed
        composeTestRule.onNodeWithTag("nav_tab_dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_donations").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_expenses").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_pledges").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_tab_reports").assertIsDisplayed()

        // Verify titles
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Donations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pledges").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reports").assertIsDisplayed()

        // Verify badge for pledges
        composeTestRule.onNodeWithTag("pledge_badge_box", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("5", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `clicking tabs switches screen correctly across all sections`() {
        var currentScreen by mutableStateOf(AppScreen.DASHBOARD)

        composeTestRule.setContent {
            SocietyManagementTheme {
                MainBottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    pendingPledgeCount = 0
                )
            }
        }

        // Click Donations
        composeTestRule.onNodeWithTag("nav_tab_donations").performClick()
        assertEquals(AppScreen.DONATIONS, currentScreen)

        // Click Expenses
        composeTestRule.onNodeWithTag("nav_tab_expenses").performClick()
        assertEquals(AppScreen.EXPENSES, currentScreen)

        // Click Pledges
        composeTestRule.onNodeWithTag("nav_tab_pledges").performClick()
        assertEquals(AppScreen.PLEDGES, currentScreen)

        // Click Reports
        composeTestRule.onNodeWithTag("nav_tab_reports").performClick()
        assertEquals(AppScreen.REPORTS, currentScreen)

        // Click Dashboard
        composeTestRule.onNodeWithTag("nav_tab_dashboard").performClick()
        assertEquals(AppScreen.DASHBOARD, currentScreen)
    }

    @Test
    fun `main screen composable integrates scaffold with bottom bar and content`() {
        var navigatedScreen by mutableStateOf(AppScreen.DASHBOARD)

        composeTestRule.setContent {
            SocietyManagementTheme {
                MainScreen(
                    currentScreen = navigatedScreen,
                    onNavigate = { navigatedScreen = it },
                    pendingPledgeCount = 3
                ) {
                    Text("Current Screen Content: ${navigatedScreen.name}")
                }
            }
        }

        // Verify content and bottom bar are rendered together
        composeTestRule.onNodeWithText("Current Screen Content: DASHBOARD").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_navigation_bar").assertIsDisplayed()

        // Switch to Expenses via bottom bar
        composeTestRule.onNodeWithTag("nav_tab_expenses").performClick()
        assertEquals(AppScreen.EXPENSES, navigatedScreen)
    }
}
