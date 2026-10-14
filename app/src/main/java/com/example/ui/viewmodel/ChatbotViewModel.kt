package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatMessage
import com.example.data.repository.ChatRole
import com.example.data.repository.ChatbotModel
import com.example.data.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val selectedModel: ChatbotModel = ChatbotModel.GENERAL,
    val inputPrompt: String = "",
    val errorMessage: String? = null
)

class ChatbotViewModel(
    private val repository: ChatbotRepository = ChatbotRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Welcome message with initial prompt guidance
        val welcomeMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatRole.ASSISTANT,
            text = "👋 Hello! I am your **Society Ledger AI Assistant**.\n\n" +
                    "I can help you with FAQs, resident queries, accounting procedures (donations, 80G tax exemptions, UPI QR codes, vouchers, budgets), and society committee rules.\n\n" +
                    "How can I assist you today?",
            modelUsed = ChatbotModel.GENERAL.displayName
        )
        _uiState.update { it.copy(messages = listOf(welcomeMessage)) }
    }

    fun onInputChange(newText: String) {
        _uiState.update { it.copy(inputPrompt = newText, errorMessage = null) }
    }

    fun selectModel(model: ChatbotModel) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun sendMessage(text: String, societyName: String = "Society") {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatRole.USER,
            text = trimmed
        )

        // Clear input and append user message, start loading
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                inputPrompt = "",
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val currentHistory = _uiState.value.messages.dropLast(1) // History prior to this message
            val model = _uiState.value.selectedModel

            val result = repository.sendMessage(
                history = currentHistory,
                newMessage = trimmed,
                model = model,
                societyName = societyName
            )

            result.fold(
                onSuccess = { replyText ->
                    val assistantMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = ChatRole.ASSISTANT,
                        text = replyText,
                        modelUsed = model.displayName
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + assistantMessage,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    val errorReply = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = ChatRole.ASSISTANT,
                        text = "⚠️ Sorry, I encountered an issue: ${error.localizedMessage ?: "Unknown error"}. Please tap retry or check your network connection.",
                        isError = true,
                        modelUsed = model.displayName
                    )
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + errorReply,
                            isLoading = false,
                            errorMessage = error.localizedMessage
                        )
                    }
                }
            )
        }
    }

    fun clearHistory() {
        val resetWelcome = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = ChatRole.ASSISTANT,
            text = "Conversation cleared. How can I help you with Society Ledger today?",
            modelUsed = _uiState.value.selectedModel.displayName
        )
        _uiState.update {
            it.copy(
                messages = listOf(resetWelcome),
                inputPrompt = "",
                errorMessage = null,
                isLoading = false
            )
        }
    }

    fun retryLastMessage(societyName: String = "Society") {
        val lastUserMessage = _uiState.value.messages.lastOrNull { it.role == ChatRole.USER }
        if (lastUserMessage != null) {
            // Remove error message if present
            _uiState.update { state ->
                val filtered = if (state.messages.lastOrNull()?.isError == true) {
                    state.messages.dropLast(1)
                } else {
                    state.messages
                }
                state.copy(messages = filtered)
            }
            sendMessage(lastUserMessage.text, societyName)
        }
    }
}
