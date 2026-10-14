package com.example

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.Member
import com.example.ui.components.RoleBasedAccessCard
import com.example.ui.components.RoleGuardedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoleBasedAccessControlTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testAdminMember = Member(
        id = 1L,
        name = "Rajesh Sharma",
        role = "President & Super Admin",
        phone = "9876543210",
        email = "rajesh@society.org",
        pin = "1234",
        isSuperAdmin = true
    )

    @Test
    fun roleBasedAccessCard_displaysAdminFeatures_whenAdminModeIsTrue() {
        var donationClicked = false
        var expenseClicked = false
        var committeeClicked = false
        var exportClicked = false
        var switchToRegularClicked = false

        composeTestRule.setContent {
            RoleBasedAccessCard(
                isAdmin = true,
                currentMember = testAdminMember,
                onRecordDonation = { donationClicked = true },
                onRecordExpense = { expenseClicked = true },
                onManageCommittee = { committeeClicked = true },
                onExportData = { exportClicked = true },
                onSwitchToRegularMember = { switchToRegularClicked = true }
            )
        }

        // Verify Admin Badges and Card are displayed
        composeTestRule.onNodeWithTag("role_based_access_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("role_badge").assertIsDisplayed()
        composeTestRule.onNodeWithText("SUPER ADMIN").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rajesh Sharma (President & Super Admin)").assertIsDisplayed()

        // Verify Admin Action Buttons are visible
        composeTestRule.onNodeWithTag("admin_feature_record_donation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_feature_record_expense").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_feature_manage_committee").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_feature_export_ledger").assertIsDisplayed()
        composeTestRule.onNodeWithTag("role_toggle_button").assertIsDisplayed()

        // Verify Unlock Admin button is NOT shown in admin mode
        composeTestRule.onNodeWithTag("unlock_admin_button").assertDoesNotExist()

        // Test interaction
        composeTestRule.onNodeWithTag("admin_feature_record_donation").performClick()
        assertTrue("Record donation click should trigger callback", donationClicked)

        composeTestRule.onNodeWithTag("role_toggle_button").performClick()
        assertTrue("Switch to regular member should trigger callback", switchToRegularClicked)
    }

    @Test
    fun roleBasedAccessCard_displaysRegularMemberView_whenAdminModeIsFalse() {
        var unlockAdminClicked = false
        var viewDonationsClicked = false

        composeTestRule.setContent {
            RoleBasedAccessCard(
                isAdmin = false,
                currentMember = null,
                onUnlockAdmin = { unlockAdminClicked = true },
                onViewDonationsLedger = { viewDonationsClicked = true }
            )
        }

        // Verify Regular Member Badges and Card are displayed
        composeTestRule.onNodeWithTag("role_based_access_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("role_badge").assertIsDisplayed()
        composeTestRule.onNodeWithText("REGULAR MEMBER").assertIsDisplayed()
        composeTestRule.onNodeWithText("Society Resident / Member View").assertIsDisplayed()

        // Verify Regular Member Actions & Unlock Button
        composeTestRule.onNodeWithTag("unlock_admin_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("regular_feature_view_donations").assertIsDisplayed()
        composeTestRule.onNodeWithTag("regular_feature_view_expenses").assertIsDisplayed()
        composeTestRule.onNodeWithTag("regular_feature_financial_summary").assertIsDisplayed()

        // Verify Admin Action Buttons are NOT shown in regular member mode
        composeTestRule.onNodeWithTag("admin_feature_record_donation").assertDoesNotExist()
        composeTestRule.onNodeWithTag("admin_feature_record_expense").assertDoesNotExist()

        // Test interaction
        composeTestRule.onNodeWithTag("unlock_admin_button").performClick()
        assertTrue("Unlock admin click should trigger callback", unlockAdminClicked)
    }

    @Test
    fun roleGuardedContent_rendersCorrectContent_basedOnRole() {
        val isAdminState = androidx.compose.runtime.mutableStateOf(true)

        composeTestRule.setContent {
            RoleGuardedContent(
                isAdmin = isAdminState.value,
                adminContent = { Text("Admin Sensitive Action Controls") },
                regularMemberContent = { Text("Regular Member Public Information") }
            )
        }
        composeTestRule.onNodeWithText("Admin Sensitive Action Controls").assertIsDisplayed()
        composeTestRule.onNodeWithText("Regular Member Public Information").assertDoesNotExist()

        isAdminState.value = false
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Regular Member Public Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Admin Sensitive Action Controls").assertDoesNotExist()
    }
}
