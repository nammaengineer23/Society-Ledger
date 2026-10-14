package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.ui.components.DeleteDonationConfirmationDialog
import com.example.ui.components.DeleteExpenseConfirmationDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DeleteConfirmationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDonation = Donation(
        id = 42L,
        donorName = "Venkatesh Murthy",
        phone = "9876543210",
        amount = 5000.0,
        status = "received",
        paymentMode = "UPI/Online",
        paymentDate = 1714500000000L,
        receiptNo = "RCP-2026-042",
        notes = "Annual Maintenance Donation"
    )

    private val testExpense = Expense(
        id = 88L,
        title = "Elevator Annual Servicing & Grease",
        category = "Maintenance",
        amount = 12500.0,
        expenseDate = 1714500000000L,
        paidTo = "Schindler Elevators Pvt Ltd",
        paymentMode = "Bank Transfer",
        receiptUrl = "INV-ELEV-88"
    )

    @Test
    fun deleteDonationDialog_displaysDetailsAndPreventsAccidentalLoss() {
        var isDismissed = false
        var confirmedReason: String? = null

        composeTestRule.setContent {
            DeleteDonationConfirmationDialog(
                donation = testDonation,
                onDismiss = { isDismissed = true },
                onConfirmDelete = { reason -> confirmedReason = reason }
            )
        }

        // Verify Dialog is displayed with Title and Warning
        composeTestRule.onNodeWithTag("delete_confirmation_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("delete_dialog_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Donation Entry?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm deletion to prevent accidental data loss").assertIsDisplayed()

        // Verify Donation Details in the summary card
        composeTestRule.onNodeWithTag("delete_item_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Venkatesh Murthy").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹5,000.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Receipt #RCP-2026-042").assertIsDisplayed()

        // Verify Buttons
        composeTestRule.onNodeWithTag("cancel_delete_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_delete_button").assertIsDisplayed()

        // Test cancel dismissal
        composeTestRule.onNodeWithTag("cancel_delete_button").performClick()
        assertTrue("Dismiss callback should be called when Cancel is clicked", isDismissed)
        assertEquals("Deletion should not proceed on cancel", null, confirmedReason)
    }

    @Test
    fun deleteDonationDialog_confirmDeleteWithReason() {
        var confirmedReason: String? = null

        composeTestRule.setContent {
            DeleteDonationConfirmationDialog(
                donation = testDonation,
                onDismiss = {},
                onConfirmDelete = { reason -> confirmedReason = reason }
            )
        }

        // Click delete directly with default quick reason
        composeTestRule.onNodeWithTag("confirm_delete_button").performClick()
        assertTrue("Confirm delete callback should be called with reason", confirmedReason != null)
        assertTrue("Reason should be valid (>= 5 chars)", confirmedReason!!.length >= 5)
    }

    @Test
    fun deleteExpenseDialog_displaysDetailsAndSupportsCustomReason() {
        var confirmedReason: String? = null
        var isDismissed = false

        composeTestRule.setContent {
            DeleteExpenseConfirmationDialog(
                expense = testExpense,
                onDismiss = { isDismissed = true },
                onConfirmDelete = { reason -> confirmedReason = reason }
            )
        }

        // Verify Expense Dialog is displayed
        composeTestRule.onNodeWithTag("delete_confirmation_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Expense Entry?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Elevator Annual Servicing & Grease").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹12,500.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Voucher #88 • Maintenance").assertIsDisplayed()

        // Enter a custom justification
        composeTestRule.onNodeWithTag("delete_reason_input")
            .performTextInput("Duplicate bill voucher entered twice by vendor")

        // Confirm delete
        composeTestRule.onNodeWithTag("confirm_delete_button").performClick()
        assertEquals("Duplicate bill voucher entered twice by vendor", confirmedReason)
        assertFalse("Dialog should not be dismissed on confirm click", isDismissed)
    }
}
