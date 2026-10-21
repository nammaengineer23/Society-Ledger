package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import java.util.Locale
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppLanguage
import com.example.data.model.AppTranslations
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.Transaction
import com.example.data.model.CommitteeMemberAccountBalance
import com.example.data.model.SocietyInfo
import com.example.data.model.CommitteeAccountCalculator
import com.example.data.model.CommitteeMemberAccount
import com.example.ui.components.CommitteeCumulativeAccountsDialog
import com.example.ui.components.CommitteeMemberAccountCard
import com.example.ui.components.CommitteeMemberSettlementDialog
import com.example.ui.components.CommunityCompactAdCard
import com.example.ui.components.ExportHelper
import com.example.ui.components.KeyboardAwareDialog
import com.example.ui.components.WhatsAppReminderHelper
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun ReportsAndCommitteeScreen(
    societyInfo: SocietyInfo,
    members: List<Member>,
    currentMember: Member?,
    isAdminMode: Boolean,
    isSuperAdmin: Boolean = false,
    isProSubscriber: Boolean = false,
    isDarkTheme: Boolean = false,
    isAppLockEnabled: Boolean = false,
    appLockPin: String = "",
    onToggleTheme: () -> Unit = {},
    onToggleAppLock: (Boolean) -> Unit = {},
    onUpdateAppLockPin: (String) -> Unit = {},
    onRestoreBackupClick: () -> Unit = {},
    donations: List<Donation>,
    expenses: List<Expense>,
    transactions: List<Transaction> = emptyList(),
    onRecordCommitteeSettlement: (member: Member, amount: Double, trustPaysMember: Boolean, paymentMode: String, referenceNo: String, notes: String) -> Unit = { _, _, _, _, _, _ -> },
    timePeriodFilter: String,
    onTimePeriodChange: (String) -> Unit,
    onOpenAuthDialog: () -> Unit,
    onTogglePublicMode: (Boolean) -> Unit,
    onResetData: () -> Unit = {},
    onAddMemberClick: () -> Unit = {},
    onEditMemberClick: (Member) -> Unit = {},
    onDeleteMemberClick: (Member) -> Unit = {},
    onEditSocietyInfoClick: () -> Unit = {},
    onMasterBulkSetupClick: () -> Unit = {},
    onOpenEmailBackupClick: () -> Unit = {},
    appLanguage: AppLanguage = LocalAppLanguage.current,
    onOpenLanguageDialog: () -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onChangePinClick: (member: Member, isSuperAdminReset: Boolean) -> Unit = { _, _ -> },
    onFinancialYearRolloverClick: () -> Unit = {},
    onNavigateToAuditLog: () -> Unit = {},
    onNavigateToDonors: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToAdminConsole: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    onNavigateToAboutUs: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToUserDataCollection: () -> Unit = {},
    onNavigateToDeleteSociety: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToDonationsWithMember: (Long) -> Unit = {},
    onNavigateToExpensesWithMember: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    var showUnifiedBackupDialog by remember { mutableStateOf(false) }
    var showChangeAppPinDialog by remember { mutableStateOf(false) }
    var showCommitteeAccountsDialog by remember { mutableStateOf(false) }
    var settlementMember by remember { mutableStateOf<Member?>(null) }
    var newAppPinInput by remember { mutableStateOf("") }
    var appPinError by remember { mutableStateOf<String?>(null) }
    val isUserLoggedInAsAdmin = isAdminMode && isSuperAdmin

    val memberAccounts = remember(members, donations, expenses) {
        CommitteeAccountCalculator.calculateAllAccounts(members, donations, expenses)
    }
    val totalCommitteeCollections = memberAccounts.sumOf { it.totalReceivedAmount }
    val totalCommitteeExpenses = memberAccounts.sumOf { it.totalExpensesAmount }
    val totalCommitteeNet = totalCommitteeCollections - totalCommitteeExpenses

    val totalReceived = donations.filter { it.status == "received" }.sumOf { it.amount }
    val totalPending = donations.filter { it.status == "pending" }.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netBalance = societyInfo.openingBalance + totalReceived - totalExpenses

    val periods = listOf(
        "ALL" to strings.periodAllTime,
        "THIS_MONTH" to strings.periodThisMonth,
        "LAST_MONTH" to strings.periodLastMonth,
        "QUARTERLY" to strings.periodQuarterly,
        "YEARLY" to strings.periodYearly
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_committee_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Society Header & Official Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(SocietyTealPrimary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = SocietyTealPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = societyInfo.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Reg No: ${societyInfo.registrationNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSuperAdmin) {
                            Button(
                                onClick = onEditSocietyInfoClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("edit_society_info_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Info", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Official Society UPI ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(societyInfo.upiId, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SocietyTealPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.8f)) {
                            Text("Financial Year", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(societyInfo.financialYear, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "🏦 Bank Account Details:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SapphireNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = societyInfo.fullBankDetails,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (societyInfo.openingBalance > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Starting Reserve / Opening Balance: ₹ ${String.format("%,.0f", societyInfo.openingBalance)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = ColorReceivedGreen
                            )
                        }
                    }

                    if (societyInfo.contactPhone.isNotBlank() || societyInfo.contactEmail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Helpline: +91 ${societyInfo.contactPhone} • ${societyInfo.contactEmail}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Super Admin Master Setup & Present Data Card
        if (isSuperAdmin) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("master_bulk_setup_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SapphireNavy.copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SapphireNavy.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = SapphireNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Master Setup & Present Data",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SapphireNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Admin access to update society name, bank details, opening balance and enter all present values at once.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = onMasterBulkSetupClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("master_bulk_setup_btn")
                        ) {
                            Text("Setup All", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Audit Report & Export Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Financial Audit & Export Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate spreadsheets or share verified statements for general body meetings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Period Selector Chips
                    Text(
                        text = "Report Date Range Filter:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEach { (key, label) ->
                            val isSelected = timePeriodFilter == key
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onTimePeriodChange(key) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Financial Summary Box in Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Inflow Collected:", style = MaterialTheme.typography.bodySmall)
                                Text("₹ ${String.format("%,.0f", totalReceived)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ColorReceivedGreen)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Pending Pledges:", style = MaterialTheme.typography.bodySmall)
                                Text("₹ ${String.format("%,.0f", totalPending)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Expenses Incurred:", style = MaterialTheme.typography.bodySmall)
                                Text("₹ ${String.format("%,.0f", totalExpenses)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color.LightGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Net Balance in Hand:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("₹ ${String.format("%,.0f", netBalance)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SapphireNavy)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Export Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                ExportHelper.exportToCsv(context, societyInfo, donations, expenses)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_csv_full_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                ExportHelper.shareAuditSummaryText(context, societyInfo, donations, expenses)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_summary_full_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Report", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // One-Tap WhatsApp Monthly Statement Dispatch
                    Button(
                        onClick = {
                            WhatsAppReminderHelper.sendMonthlyFinancialSummary(
                                context = context,
                                totalReceived = totalReceived,
                                totalExpenses = totalExpenses,
                                balance = netBalance,
                                donationsCount = donations.count { it.status == "received" },
                                expensesCount = expenses.size,
                                societyInfo = societyInfo,
                                language = appLanguage
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_whatsapp_monthly_statement"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("One-Tap WhatsApp Financial Statement", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dedicated Donors Directory & WhatsApp Broadcast Button
                    Button(
                        onClick = onNavigateToDonors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_donors_broadcast_screen_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Donors Directory & WhatsApp Broadcast",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dedicated Statutory Audit Log Navigation Button
                    Button(
                        onClick = onNavigateToAuditLog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_statutory_audit_log_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SapphireNavy
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Statutory Audit Log (Modified & Deleted Records)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Financial Year Rollover Option (Super Admin / Admin)
                    if (isAdminMode) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onFinancialYearRolloverClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_next_fy_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = SapphireNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue to Next Financial Year (Rollover Accounts)",
                                fontWeight = FontWeight.Bold,
                                color = SapphireNavy
                            )
                        }
                    }
                }
            }
        }

        // 3. Committee Members Access, Cumulative Accounts & Governance Directory
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isUserLoggedInAsAdmin) Icons.Default.Security else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isUserLoggedInAsAdmin) SapphireNavy else SocietyTealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Committee Governance & Accounts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${members.size} Officers • Cumulative Accounts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isUserLoggedInAsAdmin -> SapphireNavy
                                isAdminMode -> SocietyTealPrimary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = when {
                                    isUserLoggedInAsAdmin -> strings.memberAdminMode
                                    isAdminMode -> "Committee Member"
                                    else -> strings.publicTransparencyMode
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUserLoggedInAsAdmin || isAdminMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Prominent Management Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onAddMemberClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUserLoggedInAsAdmin) SocietyTealPrimary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("add_member_btn")
                        ) {
                            Icon(
                                imageVector = if (isUserLoggedInAsAdmin) Icons.Default.Add else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isUserLoggedInAsAdmin) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isUserLoggedInAsAdmin) "Add Member" else "Add Member (Admin)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isUserLoggedInAsAdmin) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { showCommitteeAccountsDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("btn_view_committee_audit_ledger")
                        ) {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audit Ledger", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onOpenAuthDialog,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("switch_committee_member_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isUserLoggedInAsAdmin) "Switch" else "Admin PIN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Super Admin / Admin privilege status banner
                    if (isUserLoggedInAsAdmin) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SapphireNavy.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = SapphireNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Admin Access Active: Authority to add & modify members, set PINs, and audit accounts.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = SapphireNavy,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Login as Admin to enable adding or modifying committee members.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Login as Admin >",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = SapphireNavy,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { onOpenAuthDialog() }
                                        .padding(start = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Committee Cumulative Summary Strip
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SapphireNavy.copy(alpha = 0.06f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Committee Custodial Standing",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SapphireNavy
                                )
                                Text(
                                    text = "Tap any member to view full details",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("COLLECTED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = ColorReceivedGreen, fontWeight = FontWeight.Bold)
                                    Text("₹${String.format(Locale.US, "%,.0f", totalCommitteeCollections)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ColorReceivedGreen)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("DISBURSED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = ColorExpenseRed, fontWeight = FontWeight.Bold)
                                    Text("₹${String.format(Locale.US, "%,.0f", totalCommitteeExpenses)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ColorExpenseRed)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("NET BALANCE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SapphireNavy, fontWeight = FontWeight.Bold)
                                    val sign = if (totalCommitteeNet >= 0) "+" else ""
                                    Text("$sign₹${String.format(Locale.US, "%,.0f", totalCommitteeNet)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SapphireNavy)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cumulative Account Cards for Each Member
                    if (memberAccounts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No committee members registered yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isUserLoggedInAsAdmin) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onAddMemberClick,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                                        modifier = Modifier.testTag("btn_add_first_committee_member")
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add First Committee Member", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = onOpenAuthDialog,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SapphireNavy),
                                        modifier = Modifier.testTag("btn_login_admin_add_first_member")
                                    ) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Login as Admin to Add Members", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            memberAccounts.forEach { account ->
                                val isCurrent = currentMember?.id == account.member.id && isAdminMode
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CommitteeMemberAccountCard(
                                        account = account,
                                        societyName = societyInfo.name,
                                        isCurrent = isCurrent,
                                        isAdminMode = isAdminMode,
                                        isSuperAdmin = isUserLoggedInAsAdmin,
                                        onFilterDonations = { memberId -> onNavigateToDonationsWithMember(memberId) },
                                        onFilterExpenses = { memberId -> onNavigateToExpensesWithMember(memberId) },
                                        onChangePinClick = onChangePinClick,
                                        onEditMemberClick = onEditMemberClick,
                                        onDeleteMemberClick = onDeleteMemberClick
                                    )
                                    if (isUserLoggedInAsAdmin) {
                                        OutlinedButton(
                                            onClick = { settlementMember = account.member },
                                            modifier = Modifier.fillMaxWidth().testTag("member_ledger_" + account.member.id)
                                        ) {
                                            Text("Opening balance & settlements")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Language & Regional Settings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        IndianSaffron.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Language",
                                    tint = IndianSaffron,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.languageSettingsTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${appLanguage.nativeName} (${appLanguage.englishName}) Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SocietyTealPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Button(
                            onClick = onOpenLanguageDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("change_language_btn")
                        ) {
                            Text("Change / बदलें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = strings.languageSettingsDesc,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Language Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = lang == appLanguage
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = { onSelectLanguage(lang) },
                                label = {
                                    Text(
                                        text = if (lang.isEssential) "⭐ ${lang.nativeName}" else lang.nativeName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected || lang.isEssential) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Text(
                                        text = lang.shortBadge,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) SapphireNavy else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.testTag("quick_lang_chip_${lang.code}")
                            )
                        }
                    }
                }
            }
        }

        // 5. Display & Theme Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isDarkTheme) Color(0xFFFFD54F).copy(alpha = 0.2f) else SapphireNavy.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) Color(0xFFFFD54F) else SapphireNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Appearance & Theme",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isDarkTheme) "Dark Material3 Theme Active" else "Light Material3 Theme Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_switch"),
                            thumbContent = {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Toggle between high-contrast light mode and eye-safe dark mode for comfortable late-night ledger entries and auditing.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // App Lock & PIN Security Preference Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_lock_security_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isAppLockEnabled) SapphireNavy.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "App Lock",
                                    tint = if (isAppLockEnabled) SapphireNavy else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "App Lock & Security PIN",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isAppLockEnabled) "App PIN Security Active" else "App Lock Disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isAppLockEnabled) ColorReceivedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (appLockPin.isBlank()) {
                                        newAppPinInput = ""
                                        appPinError = null
                                        showChangeAppPinDialog = true
                                    } else {
                                        onToggleAppLock(true)
                                    }
                                } else {
                                    onToggleAppLock(false)
                                }
                            },
                            modifier = Modifier.testTag("app_lock_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Require 4-digit security PIN whenever opening or returning to the Society app to protect sensitive treasury records.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isAppLockEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                newAppPinInput = ""
                                appPinError = null
                                showChangeAppPinDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_change_app_lock_pin"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change App Lock 4-Digit PIN")
                        }
                    }
                }
            }
        }

        // 5. Data Management & Local JSON Backup
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("data_persistence_backup_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SocietyTealPrimary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Data Backup",
                                    tint = SocietyTealPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Data Persistence & Backups",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Secure Local Storage • Local JSON Export",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DataObject,
                                    contentDescription = null,
                                    tint = SapphireNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "JSON Transaction Archive",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SapphireNavy
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Export the entire society's transaction history (${donations.size} donations, ${expenses.size} expenses, ${members.size} committee records) into an offline JSON backup file for audit compliance and disaster recovery.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Unified Backup & Restore Action Button
                    Button(
                        onClick = { showUnifiedBackupDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_export_json_backup")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backup & Restore Options",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 6. Directory, Policy & Legal Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("policy_directory_hub_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Society Hub & Statutory Documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Official records, bylaws, user privacy, support & multi-society directory",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Large Prominent Home Button
                    Button(
                        onClick = onNavigateToHome,
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_hub_home")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Home Directory",
                            modifier = Modifier.size(22.dp),
                            tint = IndianSaffron
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🏠 Home / All Societies Directory",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Information and Contact Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToContact,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_hub_contact"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📞 Contact Us", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToAboutUs,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_hub_about_us"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ℹ️ About Us", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToTerms,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_hub_terms"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📜 Terms & Bylaws", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToUserDataCollection,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_hub_privacy"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🛡️ User Privacy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onNavigateToFaq,
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_hub_faq")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = SapphireNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("❓ Frequently Asked Questions (FAQ)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireNavy)
                    }

                    if (isSuperAdmin) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToDeleteSociety,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_hub_delete_society"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚠️ Delete This Society Permanently", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onNavigateToAdminConsole,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_hub_admin_console"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp), tint = IndianSaffron)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🛡️ Master Admin & Complaints Console", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // 7. Copyright & Support Clarification Footer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Copyright © 2026 Society Manager. All Rights Reserved.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dedicated support & clarifications: ${societyInfo.adminEmail}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SocietyTealPrimary
                    )
                    Text(
                        text = "Admin Contact: ${societyInfo.contactPhone} • ${societyInfo.contactEmail}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // ==========================================
    // UNIFIED BACKUP OPTIONS DIALOG
    // ==========================================
    if (showUnifiedBackupDialog) {
        KeyboardAwareDialog(
            onDismissRequest = { showUnifiedBackupDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 16.dp)
                    .testTag("unified_backup_dialog"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(SapphireNavy.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Data Persistence & Backup",
                                    tint = SapphireNavy,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Data Persistence & Backups",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Secure Local Storage • 100% Local Device Storage",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { showUnifiedBackupDialog = false },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_unified_backup_dialog_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 1: DATA PERSISTENCE & STORAGE ENGINE STATUS
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_data_persistence_section_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SocietyTealPrimary.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = ColorReceivedGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DATA PERSISTENCE GUARANTEE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ColorReceivedGreen,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }

                                Surface(
                                    color = ColorReceivedGreen.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Secure Storage Active",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ColorReceivedGreen,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "All donations, expenses, committee accounts, and audit events are committed synchronously to encrypted secure local storage. Data is 100% offline-first and preserved permanently across app updates, reboots, and airplane mode.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Real-time Storage Inventory Breakdown
                            Text(
                                text = "PERSISTENT ACCOUNTS INVENTORY (${societyInfo.financialYear})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = SapphireNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Donations", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${donations.size} records", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorReceivedGreen))
                                        Text("₹${String.format("%,.0f", totalReceived)}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Expenses", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${expenses.size} vouchers", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IndianSaffron))
                                        Text("₹${String.format("%,.0f", totalExpenses)}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Committee", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${members.size} members", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SapphireNavy))
                                        Text("Net: ₹${String.format("%,.0f", netBalance)}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "BACKUP & RECOVERY TOOLS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SapphireNavy,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tool 1: Full JSON Database Archive
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_tool_json_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SapphireNavy.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(SapphireNavy, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DataObject, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Full Society JSON Accounts Archive",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SapphireNavy
                                    )
                                    Text(
                                        text = "Portable snapshot of all transactions, members & config",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        ExportHelper.exportToJsonBackup(context, societyInfo, donations, expenses, members)
                                        showUnifiedBackupDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("btn_export_json_from_backup_dialog"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export JSON", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                }

                                OutlinedButton(
                                    onClick = {
                                        ExportHelper.copyJsonBackupToClipboard(context, societyInfo, donations, expenses, members)
                                        showUnifiedBackupDialog = false
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_copy_json_from_backup_dialog"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy JSON", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tool 2: Email CSV Backup
                    Surface(
                        onClick = {
                            showUnifiedBackupDialog = false
                            onOpenEmailBackupClick()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IndianSaffron.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_tool_email_csv")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(IndianSaffron, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Send Backup via Email (CSV)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = IndianSaffron
                                )
                                Text(
                                    text = "Email spreadsheet ledger directly to society admin or auditor",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.DoubleArrow, contentDescription = null, tint = IndianSaffron, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tool 3: Save Standard CSV to Device
                    Surface(
                        onClick = {
                            showUnifiedBackupDialog = false
                            ExportHelper.exportToCsv(context, societyInfo, donations, expenses)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SocietyTealPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_tool_device_csv")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(SocietyTealPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Save Ledger to Device (CSV)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SocietyTealPrimary
                                )
                                Text(
                                    text = "Save donations & expenses CSV spreadsheet to device storage",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.DoubleArrow, contentDescription = null, tint = SocietyTealPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tool 4: Restore / Import Database from Backup
                    Surface(
                        onClick = {
                            showUnifiedBackupDialog = false
                            onRestoreBackupClick()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_tool_restore_records")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF2E7D32), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Restore / Import from Backup",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "Recover society records or merge from JSON backup file",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.DoubleArrow, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Close Button
                    Button(
                        onClick = { showUnifiedBackupDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_dialog_done_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showChangeAppPinDialog) {
        AlertDialog(
            onDismissRequest = { showChangeAppPinDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Pin,
                    contentDescription = null,
                    tint = SapphireNavy,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Change App Lock PIN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter a new 4-digit security PIN to unlock the application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newAppPinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                newAppPinInput = it
                                appPinError = null
                            }
                        },
                        label = { Text("New 4-Digit PIN") },
                        isError = appPinError != null,
                        supportingText = {
                            if (appPinError != null) {
                                Text(appPinError!!, color = Color.Red)
                            } else {
                                Text("Must be exactly 4 digits")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_app_pin")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAppPinInput.length != 4) {
                            appPinError = "Please enter exactly 4 digits"
                        } else {
                            onUpdateAppLockPin(newAppPinInput)
                            onToggleAppLock(true)
                            showChangeAppPinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                    modifier = Modifier.testTag("btn_save_app_pin")
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showChangeAppPinDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    settlementMember?.let { member ->
        CommitteeMemberSettlementDialog(
            member = member,
            transactions = transactions,
            onDismiss = { settlementMember = null },
            onSave = { amount, trustPaysMember, paymentMode, referenceNo, notes ->
                onRecordCommitteeSettlement(member, amount, trustPaysMember, paymentMode, referenceNo, notes)
            }
        )
    }

    if (showCommitteeAccountsDialog) {
        CommitteeCumulativeAccountsDialog(
            accounts = memberAccounts,
            societyName = societyInfo.name,
            onDismiss = { showCommitteeAccountsDialog = false },
            onFilterMemberDonations = { memberId ->
                showCommitteeAccountsDialog = false
                onNavigateToDonationsWithMember(memberId)
            },
            onFilterMemberExpenses = { memberId ->
                showCommitteeAccountsDialog = false
                onNavigateToExpensesWithMember(memberId)
            }
        )
    }
}
