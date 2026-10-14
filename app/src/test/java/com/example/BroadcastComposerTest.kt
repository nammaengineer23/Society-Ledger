package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.model.DonorSummary
import com.example.data.model.SocietyInfo
import com.example.ui.screens.SocietyBroadcastComposerDialog
import com.example.ui.theme.SocietyManagementTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BroadcastComposerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testSociety = SocietyInfo(
        name = "Grand Lotus Housing Society",
        registrationNo = "REG-MH-2025-0101",
        financialYear = "FY 2026-2027",
        openingBalance = 50000.0,
        upiId = "grandlotus@upi",
        bankAccount = "HDFC Bank A/C 9876543210 IFSC HDFC0001234",
        contactPhone = "9876543210"
    )

    private val testDonors = listOf(
        DonorSummary(
            name = "Ramesh Kumar",
            phone = "+919876543210",
            email = "ramesh@test.com",
            totalDonated = 5000.0,
            donationCount = 1,
            lastDonationDate = System.currentTimeMillis(),
            totalReceived = 5000.0,
            totalPending = 0.0
        ),
        DonorSummary(
            name = "Anita Desai",
            phone = "+919876543211",
            email = "anita@test.com",
            totalDonated = 2500.0,
            donationCount = 1,
            lastDonationDate = System.currentTimeMillis(),
            totalReceived = 2500.0,
            totalPending = 0.0
        )
    )

    @Test
    fun `test custom message can be entered or pasted and sent to broadcast`() {
        var sentMessage: String? = null
        val pastedCustomMessage = "Special Diwali meeting this Sunday at 6 PM in the Clubhouse. Please attend."

        composeTestRule.setContent {
            var selectedTemplateIndex by remember { mutableIntStateOf(4) } // Custom Message Mode
            var customText by remember { mutableStateOf("") }
            var target by remember { mutableStateOf("ALL") }

            SocietyManagementTheme {
                SocietyBroadcastComposerDialog(
                    societyInfo = testSociety,
                    donors = testDonors,
                    validPhoneDonors = testDonors,
                    target = target,
                    onTargetChange = { target = it },
                    selectedTemplateIndex = selectedTemplateIndex,
                    onTemplateIndexChange = { selectedTemplateIndex = it },
                    customText = customText,
                    onCustomTextChange = { customText = it },
                    onDismiss = {},
                    onSendBroadcast = { message ->
                        sentMessage = message
                    },
                    onCopyNumbers = {}
                )
            }
        }

        // Verify the custom text field and paste button are visible
        composeTestRule.onNodeWithTag("custom_broadcast_text_input").assertExists()
        composeTestRule.onNodeWithTag("btn_paste_custom_broadcast").assertExists()

        // Input or paste the custom message into the text field
        composeTestRule.onNodeWithTag("custom_broadcast_text_input").performTextInput(pastedCustomMessage)

        // Verify send broadcast button exists and click it
        composeTestRule.onNodeWithTag("send_broadcast_whatsapp_btn").assertExists()
        composeTestRule.onNodeWithTag("send_broadcast_whatsapp_btn").performClick()

        // Assert that the sent broadcast message equals the pasted custom message
        assertEquals(pastedCustomMessage, sentMessage)
    }

    @Test
    fun `test switching from ready templates to custom message editor`() {
        var templateIndex = 0
        var customText = ""

        composeTestRule.setContent {
            var selectedTemplateIndex by remember { mutableIntStateOf(0) } // Starts with AGM template
            var currentCustomText by remember { mutableStateOf("") }
            var target by remember { mutableStateOf("ALL") }

            SocietyManagementTheme {
                SocietyBroadcastComposerDialog(
                    societyInfo = testSociety,
                    donors = testDonors,
                    validPhoneDonors = testDonors,
                    target = target,
                    onTargetChange = { target = it },
                    selectedTemplateIndex = selectedTemplateIndex,
                    onTemplateIndexChange = {
                        selectedTemplateIndex = it
                        templateIndex = it
                    },
                    customText = currentCustomText,
                    onCustomTextChange = {
                        currentCustomText = it
                        customText = it
                    },
                    onDismiss = {},
                    onSendBroadcast = {},
                    onCopyNumbers = {}
                )
            }
        }

        // Scroll to and tap customize template button
        composeTestRule.onNodeWithTag("btn_customize_template").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verify it switched to custom message mode (index 4) and loaded the content into customText
        assertEquals(4, templateIndex)
        assertTrue(customText.contains("GRAND LOTUS HOUSING SOCIETY"))
    }
}
