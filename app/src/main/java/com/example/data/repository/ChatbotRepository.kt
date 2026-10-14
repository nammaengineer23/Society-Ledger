package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiRetrofitClient
import com.example.ui.screens.DEFAULT_FAQS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Message representation for the UI layer.
 */
data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val modelUsed: String? = null
)

enum class ChatRole {
    USER,
    ASSISTANT
}

/**
 * Model specifications as required:
 * - gemini-3.5-flash for general tasks (default)
 * - gemini-3.1-pro-preview for particularly complex tasks
 * - gemini-3.1-flash-lite-preview for tasks that should happen fast
 */
enum class ChatbotModel(
    val modelId: String,
    val displayName: String,
    val subtitle: String,
    val iconName: String
) {
    GENERAL(
        modelId = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        subtitle = "General FAQ & Society Queries (Default)",
        iconName = "flash"
    ),
    PRO(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro",
        subtitle = "Complex Audits & Bylaws Reasoning",
        iconName = "psychology"
    ),
    FAST(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash-Lite",
        subtitle = "Lightning Fast Quick Answers",
        iconName = "speed"
    );

    companion object {
        fun fromModelId(id: String): ChatbotModel {
            return entries.firstOrNull { it.modelId == id } ?: GENERAL
        }
    }
}

class ChatbotRepository(
    private val apiService: GeminiApiService = GeminiRetrofitClient.apiService
) {
    private val systemInstructionText = """
        You are the Society Ledger AI Assistant, the official interactive helpdesk and financial guide for apartment housing societies, RWAs, and residential welfare associations.
        Your duties:
        - Provide direct, concise, and helpful answers to resident and committee frequently asked questions (FAQs) and queries.
        - Explain how Society Ledger features work:
          1. Donations & UPI: Recording donations, generating dynamic BHIM/NPCI standard UPI QR codes, instant serial-numbered digital receipts, WhatsApp sharing.
          2. Pending Approvals: Inspecting NEFT/RTGS slips, un-cleared cheques, self-reported UPI reference numbers, marking as approved or rejected with permanent audit logging.
          3. Expenses & Budgets: Creating expense vouchers, attaching bill invoice photos, department budget caps (Security, Lift AMC, Electricity), 80% spending alerts.
          4. Statutory Reports: Section 80G tax exemption certificates with PAN, Financial Year Rollover, CSV/Excel exports, full encrypted JSON backups.
          5. Committee Access: Role-based PIN security (President, Secretary, Treasurer, Auditor, Admin), emergency PIN reset.
        - Clarify society governance rules, bylaws, and transparency practices.
        - Format responses clearly using markdown bold headings, bullet points, and numbered lists where appropriate.
        - Maintain a professional, polite, and reassuring tone.
    """.trimIndent()

    /**
     * Sends a multi-turn chat message to Gemini with conversation history.
     */
    suspend fun sendMessage(
        history: List<ChatMessage>,
        newMessage: String,
        model: ChatbotModel = ChatbotModel.GENERAL,
        societyName: String = "Society"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check if API key is present and configured
        val isApiKeyMissingOrPlaceholder = apiKey.isNullOrBlank() ||
                apiKey == "MY_GEMINI_API_KEY" ||
                apiKey.contains("YOUR_KEY")

        if (isApiKeyMissingOrPlaceholder) {
            Log.w("ChatbotRepository", "Gemini API key is not configured in Secrets panel.")
            // Use local FAQ knowledge fallback
            val fallbackAnswer = generateLocalFaqAnswer(newMessage, societyName)
            return@withContext Result.success(fallbackAnswer)
        }

        try {
            // Build conversation history for Gemini API
            val contents = mutableListOf<GeminiContent>()

            // Add previous history turns
            for (msg in history) {
                if (msg.isError) continue // Skip error messages
                val role = if (msg.role == ChatRole.USER) "user" else "model"
                contents.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }

            // Append current user message
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = newMessage))
                )
            )

            val customizedSystemInstruction = "$systemInstructionText\nCurrently assisting for society: $societyName."

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = customizedSystemInstruction))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = if (model == ChatbotModel.PRO) 0.4f else 0.7f,
                    topP = 0.95f,
                    topK = 40,
                    maxOutputTokens = 2048
                )
            )

            val response = apiService.generateContent(
                model = model.modelId,
                apiKey = apiKey,
                request = request
            )

            val responseText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            if (!responseText.isNullOrBlank()) {
                Result.success(responseText.trim())
            } else if (response.error != null) {
                val errorMsg = response.error.message ?: "Gemini API returned an error (${response.error.code})"
                Log.e("ChatbotRepository", "Gemini API Error: $errorMsg")
                // Graceful fallback to local FAQ with error note
                val localAnswer = generateLocalFaqAnswer(newMessage, societyName)
                Result.success("$localAnswer\n\n*(Note: Cloud AI response temporarily unavailable; answered via Society Knowledge Base)*")
            } else {
                Result.failure(Exception("No response received from Gemini AI."))
            }
        } catch (e: Exception) {
            Log.e("ChatbotRepository", "Error calling Gemini API: ${e.message}", e)
            // If network fails or invalid key error, provide smart offline FAQ assistance
            val localAnswer = generateLocalFaqAnswer(newMessage, societyName)
            Result.success("$localAnswer\n\n*(Note: Offline assistance provided. Please ensure internet connectivity and a valid Gemini API key in the AI Studio Secrets panel for live responses.)*")
        }
    }

    /**
     * Local FAQ matcher that provides instant answers for common queries
     * when offline or when API key is awaiting configuration.
     */
    private fun generateLocalFaqAnswer(query: String, societyName: String): String {
        val qLower = query.lowercase().trim()

        // Match against existing society FAQs
        val matchedFaq = DEFAULT_FAQS.firstOrNull { faq ->
            val keywords = faq.question.lowercase().split(" ", "?", "/", ",")
                .filter { it.length > 3 && it !in listOf("what", "how", "when", "where", "does", "with") }
            keywords.any { qLower.contains(it) } || qLower.contains(faq.category.lowercase())
        }

        if (matchedFaq != null) {
            return "### ${matchedFaq.question}\n\n${matchedFaq.answer}\n\n*Related Category: ${matchedFaq.category}*"
        }

        return when {
            qLower.contains("donation") || qLower.contains("donate") || qLower.contains("contribute") -> {
                "### Recording and Managing Donations in $societyName\n\n" +
                        "1. **Add Donation**: Navigate to the **Donations** tab from the bottom bar and tap **+ Add Donation**.\n" +
                        "2. **Donor Information**: Enter donor name, mobile number, amount, and payment mode (Cash, UPI, Cheque, Bank Transfer).\n" +
                        "3. **UPI Payment**: If selecting UPI, you can display the dynamic **UPI QR Code** for direct instant scan and pay into the society account.\n" +
                        "4. **Receipt Generation**: Tap any completed donation in the list to generate and share official digital receipts via WhatsApp or SMS."
            }
            qLower.contains("expense") || qLower.contains("bill") || qLower.contains("spend") || qLower.contains("voucher") -> {
                "### Managing Expenses and Bills\n\n" +
                        "1. Go to the **Expenses** tab in the bottom navigation.\n" +
                        "2. Tap **+ New Expense** to log a voucher with payee details, category (Security, Lift AMC, Electricity, Sanitation), amount, and payment mode.\n" +
                        "3. Tap **Attach Invoice** to link photo or document references.\n" +
                        "4. Monitor departmental spending progress against preset **Category Budgets** to prevent overspending."
            }
            qLower.contains("pending") || qLower.contains("approval") || qLower.contains("verify") -> {
                "### Verifying Pending Transactions\n\n" +
                        "1. Open the **Pending** tab from the bottom navigation.\n" +
                        "2. Review self-reported UPI transfers, cheques, and NEFT clearing slips.\n" +
                        "3. Tap **Approve & Mark Received** once bank credit is confirmed, or **Reject / Void** if invalid.\n" +
                        "4. All actions are securely logged in the permanent **Audit Log**."
            }
            qLower.contains("pin") || qLower.contains("admin") || qLower.contains("login") || qLower.contains("role") -> {
                "### Committee Roles and PIN Security\n\n" +
                        "1. Society Ledger uses role-based access: **President**, **Secretary**, **Treasurer**, **Auditor**, and **Admin**.\n" +
                        "2. Tap **Committee Login** (or the role badge in the top-right) and enter your 4-digit security PIN.\n" +
                        "3. Members can change their PIN anytime in **Reports > Committee Members**.\n" +
                        "4. In case of a forgotten PIN, the Admin can reset it using the Master Security PIN."
            }
            qLower.contains("80g") || qLower.contains("tax") || qLower.contains("certificate") -> {
                "### Section 80G Tax Exemption Certificates\n\n" +
                        "1. Open the **Reports** tab from the bottom navigation.\n" +
                        "2. Under *Statutory & Tax Certifications*, tap **Generate 80G Tax Certificates**.\n" +
                        "3. Select the donor and financial year to export a certified 80G statement featuring the society's PAN and registration details."
            }
            qLower.contains("rollover") || qLower.contains("financial year") || qLower.contains("closing balance") -> {
                "### Financial Year Rollover\n\n" +
                        "1. In the **Reports** tab, scroll to *Financial Year Management* and tap **Financial Year Rollover**.\n" +
                        "2. The system calculates the net closing balance of the ending fiscal year and sets it as the certified Opening Balance for the new year.\n" +
                        "3. All past records are locked and archived for statutory audit protection."
            }
            qLower.contains("backup") || qLower.contains("export") || qLower.contains("excel") || qLower.contains("csv") -> {
                "### Data Export and Backups\n\n" +
                        "- **Export CSV / Excel**: Go to **Reports** > tap **Export CSV** to download itemized spreadsheets of Donations, Expenses, and Donors.\n" +
                        "- **JSON Backup**: Tap **Accounts JSON Backup** to create an encrypted, complete backup file of all ledger data."
            }
            else -> {
                "### Welcome to the $societyName AI Assistant!\n\n" +
                        "I am your dedicated helpdesk for society FAQs, accounting procedures, and resident queries. You can ask me questions like:\n" +
                        "- *\"How do I add a donation with UPI QR?\"*\n" +
                        "- *\"How do I approve a pending payment?\"*\n" +
                        "- *\"How to record an expense and manage category budgets?\"*\n" +
                        "- *\"How to generate an 80G tax certificate?\"*\n" +
                        "- *\"How does Financial Year Rollover work?\"*\n" +
                        "- *\"How do I reset a committee member PIN?\"*\n\n" +
                        "Type your question below or tap any of the quick suggestions to begin!"
            }
        }
    }
}
