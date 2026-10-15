package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SocietyInfo
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

data class FAQItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String
)

val DEFAULT_FAQS = listOf(
    FAQItem(
        id = "faq_setup_1",
        category = "General & Setup",
        question = "How do I switch between different societies or register a new one?",
        answer = "Tap the building/bank icon at the top-left of the navigation bar to open the 'Home / All Societies Directory'. From there, you can view all registered societies, search by name or registration number, tap 'Switch to Society' to load a different society, or click the '+ New Society' button to register or create a draft society with custom opening balances, financial year, and committee contact details."
    ),
    FAQItem(
        id = "faq_setup_2",
        category = "General & Setup",
        question = "What is the difference between Public View and Committee Admin mode?",
        answer = "In Public View (indicated by the amber chip in the top right), residents and visitors have read-only access to view transparent dashboards, audited transactions, donor honor boards, and annual reports. Switching to an Admin or Committee Member role requires entering your 4-digit security PIN. Once authenticated, authorized members can record donations, approve pending receipts, add expenses with bills, manage committee roles, and execute financial rollovers."
    ),
    FAQItem(
        id = "faq_don_1",
        category = "Donations & UPI",
        question = "How does the UPI QR Code generator work for collecting donations?",
        answer = "In the 'Donations' tab, admins can tap '+ Add Donation' and choose the 'UPI' payment mode, or use the 'UPI Payment QR' shortcut. The app automatically encodes the society's configured UPI VPA (e.g. society@upi) and the exact payment amount into a dynamic BHIM/NPCI standard UPI QR code. Donors can scan this QR code using any UPI app (GPay, PhonePe, Paytm) to pay directly into the society's bank account."
    ),
    FAQItem(
        id = "faq_don_2",
        category = "Donations & UPI",
        question = "How do I generate, share, or print digital receipts for donors?",
        answer = "Every recorded donation receives an instant serial numbered voucher (e.g., REC-2026-001). Tap any donation in the Donations tab to open its full receipt dialog. You can print the receipt, save it as a high-resolution image/PDF, or tap 'Share WhatsApp / SMS' to dispatch a formatted receipt statement directly to the donor's mobile number including the 80G tax exemption note."
    ),
    FAQItem(
        id = "faq_pending_1",
        category = "Pending Approvals",
        question = "Why do some transactions appear under the 'Pending' tab?",
        answer = "When donations are submitted with pending verification (such as direct bank NEFT/RTGS transfers, un-cleared cheques, or self-reported UPI payments with reference numbers), they are held in the 'Pending' tab. Committee treasurers can inspect the transaction reference, bank clearing slip, and donor phone before tapping 'Approve & Mark Received' or 'Reject / Void'."
    ),
    FAQItem(
        id = "faq_pending_2",
        category = "Pending Approvals",
        question = "Can rejected or voided transactions be restored?",
        answer = "All approval, rejection, and modification events are permanently recorded in the immutable 'Audit Log' with timestamps, admin ID, and justification notes for forensic accounting and anti-fraud governance."
    ),
    FAQItem(
        id = "faq_exp_1",
        category = "Expenses & Budgets",
        question = "How do I record expenses and attach bill invoices?",
        answer = "Navigate to the 'Expenses' tab from the bottom bar and tap '+ New Expense'. Specify the vendor/payee name, category (e.g., Security, Sanitation, Electricity, Lift AMC), voucher number, amount, payment method, and attach invoice receipts or photo evidence. You can also flag if the bill requires dual-signoff from the Treasurer and President."
    ),
    FAQItem(
        id = "faq_exp_2",
        category = "Expenses & Budgets",
        question = "How do Expense Category Budgets work?",
        answer = "In the Expenses screen, tap 'Manage Budgets' to set monthly or annual expenditure caps for each department (e.g., ₹25,000 for Security, ₹15,000 for Garden Maintenance). The app displays real-time progress bars, warning flags when spending reaches 80% of allocation, and restricts over-budget approvals unless authorized by a super-admin."
    ),
    FAQItem(
        id = "faq_rep_1",
        category = "Reports & Audits",
        question = "How do I generate Section 80G tax exemption certificates?",
        answer = "Open the 'Reports' tab from the bottom navigation bar. Under the 'Statutory & Tax Certifications' section, tap 'Generate 80G Tax Certificates'. You can filter eligible donors who contributed above the statutory threshold, preview their Section 80G compliance certificate with PAN details and society registration number, and export PDF copies for tax filing."
    ),
    FAQItem(
        id = "faq_rep_2",
        category = "Reports & Audits",
        question = "How do I perform a Financial Year Rollover?",
        answer = "At the conclusion of the financial year (e.g., moving from FY 2025-26 to FY 2026-27), an authorized committee administrator can tap 'Financial Year Rollover' in the Reports tab. The app freezes the closing balance of the outgoing year, rolls it forward as the certified Opening Balance of the new fiscal year, and creates an immutable snapshot of all ledger accounts."
    ),
    FAQItem(
        id = "faq_rep_3",
        category = "Reports & Audits",
        question = "How do I export data to Excel (.CSV) or JSON backup?",
        answer = "In the Reports screen, tap 'Export CSV / Excel' to download complete itemized spreadsheets of Donations, Donors, and Expenses. To safeguard data against loss, tap 'Accounts JSON Backup' to export a comprehensive, structured backup containing all societies, committee members, transactions, and audit logs."
    ),
    FAQItem(
        id = "faq_comm_1",
        category = "Committee & Roles",
        question = "What committee roles are supported and how are PINs managed?",
        answer = "The system supports standard RWA governance roles: Admin, President, Vice President, Secretary, Treasurer, Auditor, and General Member. Committee members can update their own 4-digit PIN in the Reports screen. If a PIN is forgotten, the Admin can perform an emergency PIN reset using the Master Security PIN."
    ),
    FAQItem(
        id = "faq_sec_1",
        category = "Security & Privacy",
        question = "Where is data stored and how is member privacy protected?",
        answer = "All society records are stored securely with local persistence and encrypted online synchronization. Member contact details, phone numbers, and addresses can be masked from the public view according to the Society Data Privacy Policy, accessible from the 'User Privacy' hub."
    ),
    FAQItem(
        id = "faq_sec_2",
        category = "Security & Privacy",
        question = "How can I contact technical support or file a grievance?",
        answer = "You can tap '📞 Contact Us' in the Society Hub or the bottom of the FAQ screen to submit an official support ticket. You can also reach Master Admin directly via Email at societyledger@gmail.com or WhatsApp Business at +91 9986385925."
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FAQScreen(
    societyInfo: SocietyInfo,
    onNavigateToContact: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToChatbot: () -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedFaqIds by remember { mutableStateOf(setOf<String>()) }

    val categories = remember {
        listOf("All") + DEFAULT_FAQS.map { it.category }.distinct()
    }

    val filteredFaqs = remember(searchQuery, selectedCategory) {
        DEFAULT_FAQS.filter { item ->
            val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                item.question.contains(searchQuery, ignoreCase = true) ||
                item.answer.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("screen_faq"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Frequently Asked Questions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${societyInfo.name} • Help & Knowledgebase",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("faq_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToChatbot,
                        modifier = Modifier.testTag("faq_open_chatbot_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask AI Assistant",
                            tint = IndianSaffron
                        )
                    }
                    IconButton(
                        onClick = onNavigateToContact,
                        modifier = Modifier.testTag("faq_contact_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Contact Support",
                            tint = SapphireNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // AI Assistant Interactive Helpdesk Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToChatbot() }
                        .testTag("faq_ai_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SocietyTealPrimary.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, SocietyTealPrimary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(SocietyTealPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = SocietyTealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ask AI Assistant",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = IndianSaffron.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "GEMINI",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = IndianSaffron,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Instant answers for donations, 80G tax rules, receipts & committee bylaws",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onNavigateToChatbot,
                            colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("open_faq_chatbot_button")
                        ) {
                            Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("faq_search_field"),
                    placeholder = { Text("Search topics, UPI, receipts, 80G, budgets...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SapphireNavy)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            item {
                // Category Filter Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SapphireNavy.copy(alpha = 0.15f),
                                selectedLabelColor = SapphireNavy
                            )
                        )
                    }
                }
            }

            item {
                // Results count and header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "All") "All Help Topics (${filteredFaqs.size})" else "$selectedCategory (${filteredFaqs.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (filteredFaqs.isNotEmpty()) {
                        Text(
                            text = if (expandedFaqIds.size == filteredFaqs.size) "Collapse All" else "Expand All",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SapphireNavy,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable {
                                    expandedFaqIds = if (expandedFaqIds.size == filteredFaqs.size) {
                                        emptySet()
                                    } else {
                                        filteredFaqs.map { it.id }.toSet()
                                    }
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            if (filteredFaqs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No matching questions found",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Try searching with a different keyword or contact our support desk.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onNavigateToContact,
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask Support Team")
                            }
                        }
                    }
                }
            } else {
                items(filteredFaqs, key = { it.id }) { faq ->
                    val isExpanded = expandedFaqIds.contains(faq.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("faq_card_${faq.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isExpanded) SapphireNavy.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedFaqIds = if (isExpanded) {
                                            expandedFaqIds - faq.id
                                        } else {
                                            expandedFaqIds + faq.id
                                        }
                                    }
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        color = when (faq.category) {
                                            "Donations & UPI" -> SocietyTealPrimary.copy(alpha = 0.12f)
                                            "Expenses & Budgets" -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                            "Reports & Audits" -> SapphireNavy.copy(alpha = 0.12f)
                                            "Pending Approvals" -> IndianSaffron.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = faq.category,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (faq.category) {
                                                    "Donations & UPI" -> SocietyTealPrimary
                                                    "Expenses & Budgets" -> MaterialTheme.colorScheme.error
                                                    "Reports & Audits" -> SapphireNavy
                                                    "Pending Approvals" -> IndianSaffron
                                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                                }
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = faq.question,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 18.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            if (isExpanded) SapphireNavy.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = if (isExpanded) SapphireNavy else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                                ) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )

                                    Text(
                                        text = faq.answer,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("FAQ Answer", "${faq.question}\n\n${faq.answer}")
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Answer copied to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy Answer", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Support Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("faq_help_desk_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SapphireNavy)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Can't find what you're looking for?",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Our administrative team is available to assist your committee with audits, setups, or technical support.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToChatbot,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IndianSaffron,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("faq_bottom_ai_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ask AI Assistant (Gemini)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onNavigateToContact,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("faq_open_contact_btn")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Contact Administrator / Raise Ticket", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
