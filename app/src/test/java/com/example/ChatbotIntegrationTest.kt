package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.model.SocietyInfo
import com.example.data.repository.ChatMessage
import com.example.data.repository.ChatRole
import com.example.data.repository.ChatbotModel
import com.example.data.repository.ChatbotRepository
import com.example.ui.screens.ChatbotScreen
import com.example.ui.viewmodel.ChatbotViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ChatbotIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testSociety = SocietyInfo(
        name = "Green Meadows RWA",
        registrationNo = "REG-2024-GM",
        address = "12th Cross, Indiranagar, Bengaluru",
        contactEmail = "greenmeadows@gmail.com",
        contactPhone = "+91 9876543210"
    )

    @Test
    fun chatbotScreen_displaysCoreComponents() {
        composeTestRule.setContent {
            ChatbotScreen(
                societyInfo = testSociety,
                onBack = {}
            )
        }

        // Verify core elements are present
        composeTestRule.onNodeWithTag("chatbot_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_model_selector").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_input_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_send_button").assertIsDisplayed()

        // Verify model options are visible
        composeTestRule.onNodeWithText("⚡ General (3.5 Flash)").assertIsDisplayed()
        composeTestRule.onNodeWithText("🧠 Deep Audit (3.1 Pro)").assertIsDisplayed()
        composeTestRule.onNodeWithText("⚡ Fast (3.1 Lite)").assertIsDisplayed()

        // Verify quick starter queries are present
        composeTestRule.onNodeWithTag("quick_suggestion_chip_0").assertIsDisplayed()
    }

    @Test
    fun chatInput_sendButtonEnabledOnlyWhenTextIsNotBlank() {
        val viewModel = ChatbotViewModel()

        composeTestRule.setContent {
            ChatbotScreen(
                societyInfo = testSociety,
                onBack = {},
                viewModel = viewModel
            )
        }

        // Initially input is empty -> send button is disabled
        composeTestRule.onNodeWithTag("chat_send_button").assertIsNotEnabled()

        // Type query
        composeTestRule.onNodeWithTag("chat_input_field").performTextInput("How to add donation?")

        // Send button should now be enabled
        composeTestRule.onNodeWithTag("chat_send_button").assertIsEnabled()
    }

    @Test
    fun chatbotViewModel_selectModel_updatesState() {
        val viewModel = ChatbotViewModel()
        assertEquals(ChatbotModel.GENERAL, viewModel.uiState.value.selectedModel)

        viewModel.selectModel(ChatbotModel.PRO)
        assertEquals(ChatbotModel.PRO, viewModel.uiState.value.selectedModel)

        viewModel.selectModel(ChatbotModel.FAST)
        assertEquals(ChatbotModel.FAST, viewModel.uiState.value.selectedModel)
    }

    @Test
    fun chatbotViewModel_maintainsConversationHistory() {
        val viewModel = ChatbotViewModel()
        val initialCount = viewModel.uiState.value.messages.size
        assertTrue("Expected welcome message", initialCount >= 1)

        // Send user query
        viewModel.sendMessage("How do I record an expense?", testSociety.name)

        val updatedMessages = viewModel.uiState.value.messages
        // Should have initial welcome + new user message (+ optional assistant reply if handled immediately)
        assertTrue(updatedMessages.any { it.role == ChatRole.USER && it.text.contains("How do I record an expense?") })
    }

    @Test
    fun chatbotViewModel_clearHistory_resetsMessages() {
        val viewModel = ChatbotViewModel()
        viewModel.sendMessage("Test query", testSociety.name)
        viewModel.clearHistory()

        assertEquals(1, viewModel.uiState.value.messages.size)
        assertEquals(ChatRole.ASSISTANT, viewModel.uiState.value.messages.first().role)
        assertTrue(viewModel.uiState.value.messages.first().text.contains("cleared"))
    }

    @Test
    fun chatbotRepository_localFaqFallback_answersCommonSocietyQueries() = runBlocking {
        val repository = ChatbotRepository()
        val history = listOf<ChatMessage>()

        // Test donation query fallback
        val donationResult = repository.sendMessage(history, "How do I add a donation with UPI?", ChatbotModel.GENERAL, testSociety.name)
        assertTrue(donationResult.isSuccess)
        val donationText = donationResult.getOrNull()
        assertNotNull(donationText)
        assertTrue(donationText!!.contains("Donation") || donationText.contains("UPI"))

        // Test expense query fallback
        val expenseResult = repository.sendMessage(history, "How do I manage expenses?", ChatbotModel.PRO, testSociety.name)
        assertTrue(expenseResult.isSuccess)
        val expenseText = expenseResult.getOrNull()
        assertNotNull(expenseText)
        assertTrue(expenseText!!.contains("Expense") || expenseText.contains("bill"))
    }
}
