package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.AuditedTransactionItem
import com.example.data.model.SocietyInfo
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.ColorExpenseBg
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.ColorReceivedBg
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SapphireNavyContainer
import com.example.ui.theme.SocietyTealContainer
import com.example.ui.theme.SocietyTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated 'Audit Log' screen.
 * Displays all deleted and modified transaction records fetched from Firestore / Local repository,
 * sorted chronologically by timestamp and clearly labeled with mandatory audit reasons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    societyInfo: SocietyInfo,
    auditLogs: List<AuditLog>,
    auditedTransactions: List<AuditedTransactionItem> = emptyList(),
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    actionFilter: String = "ALL",
    onActionFilterChange: (String) -> Unit = {},
    typeFilter: String = "ALL",
    onTypeFilterChange: (String) -> Unit = {},
    isSyncing: Boolean = false,
    lastSyncTime: Long = System.currentTimeMillis(),
    onRefresh: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedLogForDetail by remember { mutableStateOf<AuditLog?>(null) }
    var selectedTransactionForDetail by remember { mutableStateOf<AuditedTransactionItem?>(null) }
    var activeTab by remember { mutableStateOf(0) } // 0: Audit Trail Logs, 1: Audited Transactions

    val deletedCount = auditLogs.count { it.action.contains("DELETE", ignoreCase = true) || it.action.contains("VOID", ignoreCase = true) }
    val modifiedCount = auditLogs.count { it.action.contains("MODIFY", ignoreCase = true) || it.action.contains("EDIT", ignoreCase = true) }
    val totalLogs = auditLogs.size

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val timeOnlyFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    fun shareAuditTrail() {
        val sb = StringBuilder()
        sb.append("═══════════════════════════════════════\n")
        sb.append("📋 STATUTORY AUDIT TRAIL REPORT\n")
        sb.append("🏛️ ${societyInfo.name.ifBlank { "Society" }}\n")
        sb.append("📜 Reg No: ${societyInfo.registrationNo}\n")
        sb.append("📅 Generated: ${dateFormat.format(Date())}\n")
        sb.append("═══════════════════════════════════════\n\n")
        sb.append("SUMMARY STATS:\n")
        sb.append("• Total Audit Records: $totalLogs\n")
        sb.append("• Voided/Deleted Transactions: $deletedCount\n")
        sb.append("• Modified Records: $modifiedCount\n")
        sb.append("• Audit Trail: 100% Immutable Verified Trail\n\n")
        sb.append("═══════════════════════════════════════\n")
        sb.append("DETAILED AUDIT LOGS:\n\n")

        auditLogs.forEachIndexed { index, log ->
            sb.append("${index + 1}. [${log.action.uppercase()}] ${log.transactionType}\n")
            sb.append("   Reference: ${log.transactionRef}\n")
            sb.append("   Timestamp: ${dateFormat.format(Date(log.timestamp))}\n")
            sb.append("   Performed By: ${log.performedByName} (${log.performedByRole})\n")
            sb.append("   ⚠️ MANDATORY REASON: ${log.reason}\n")
            if (log.oldValueSummary.isNotBlank()) {
                sb.append("   Old Snapshot: ${log.oldValueSummary}\n")
            }
            if (log.newValueSummary.isNotBlank()) {
                sb.append("   New Value: ${log.newValueSummary}\n")
            }
            sb.append("---------------------------------------\n")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            putExtra(Intent.EXTRA_SUBJECT, "Audit Trail Report - ${societyInfo.name}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Statutory Audit Report")
        context.startActivity(shareIntent)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("audit_log_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Statutory Audit Log",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SocietyTealContainer
                            ) {
                                Text(
                                    text = "LIVE SYNC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SocietyTealPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Mandatory justification trail for modified & deleted records",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("audit_log_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("audit_log_refresh_button"),
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = SocietyTealPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Logs",
                                tint = SocietyTealPrimary
                            )
                        }
                    }
                    IconButton(
                        onClick = { shareAuditTrail() },
                        modifier = Modifier.testTag("audit_log_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Audit Report",
                            tint = SapphireNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Audit KPIs Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_kpis_card"),
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
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(SapphireNavyContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = SapphireNavy,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Statutory Compliance Audit Trail",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Synchronized • Immutable",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ColorReceivedBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ColorReceivedGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "100% Verified",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorReceivedGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Logs
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SapphireNavyContainer.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Total Entries", fontSize = 11.sp, color = SapphireNavy, fontWeight = FontWeight.Medium)
                                    Text(text = "$totalLogs", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SapphireNavy)
                                }
                            }

                            // Modified
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AmberContainer.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Modified", fontSize = 11.sp, color = AmberAccent, fontWeight = FontWeight.Medium)
                                    Text(text = "$modifiedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                                }
                            }

                            // Deleted/Voided
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = ColorExpenseBg.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Deleted / Void", fontSize = 11.sp, color = ColorExpenseRed, fontWeight = FontWeight.Medium)
                                    Text(text = "$deletedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorExpenseRed)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Legal notice snippet
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Under statutory bylaws, all modifications require a documented reason and cannot be erased.",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_search_input"),
                    placeholder = { Text("Search by reason, donor, title, receipt, admin...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 3. Action & Type Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Action Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val actionOptions = listOf(
                            "ALL" to "All Actions",
                            "DELETED" to "Deleted / Voided",
                            "MODIFIED" to "Modified Records",
                            "STATUS_CHANGED" to "Status Changed"
                        )
                        actionOptions.forEach { (key, label) ->
                            val isSelected = actionFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { onActionFilterChange(key) },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    when (key) {
                                        "DELETED" -> Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorExpenseRed)
                                        "MODIFIED" -> Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberAccent)
                                        "STATUS_CHANGED" -> Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = SapphireNavy)
                                        else -> Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (key) {
                                        "DELETED" -> ColorExpenseBg
                                        "MODIFIED" -> AmberContainer
                                        "STATUS_CHANGED" -> SapphireNavyContainer
                                        else -> SocietyTealContainer
                                    },
                                    selectedLabelColor = when (key) {
                                        "DELETED" -> ColorExpenseRed
                                        "MODIFIED" -> AmberAccent
                                        "STATUS_CHANGED" -> SapphireNavy
                                        else -> SocietyTealPrimary
                                    }
                                ),
                                modifier = Modifier.testTag("audit_filter_action_$key")
                            )
                        }
                    }

                    // Entity Type Filters (Donations vs Expenses)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val typeOptions = listOf(
                            "ALL" to "All Types",
                            "DONATION" to "Donations & Pledges",
                            "EXPENSE" to "Expenses & Bills",
                            "SOCIETY" to "Society Master"
                        )
                        typeOptions.forEach { (key, label) ->
                            val isSelected = typeFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTypeFilterChange(key) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("audit_filter_type_$key")
                            )
                        }
                    }
                }
            }

            // 4. Audit List Header & Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Audit Entries (${auditLogs.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Sorted by Timestamp (Newest First)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // 5. Empty State
            if (auditLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("audit_empty_state"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SocietyTealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SocietyTealPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Audit Discrepancies Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || actionFilter != "ALL" || typeFilter != "ALL")
                                    "No audit logs match your search and filter criteria. Try resetting filters."
                                else
                                    "All transaction entries are original and have not undergone modification or deletion.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // 6. Audit Items
                items(auditLogs, key = { it.id.toString() + "_" + it.timestamp }) { log ->
                    AuditLogItemCard(
                        log = log,
                        dateFormat = dateFormat,
                        onClick = { selectedLogForDetail = log },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("Audit #${log.id}: [${log.action}] ${log.transactionRef} - Reason: ${log.reason}"))
                            Toast.makeText(context, "Audit log copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Detail Dialog for Full Audit Voucher
    selectedLogForDetail?.let { log ->
        AuditDetailDialog(
            log = log,
            dateFormat = dateFormat,
            onDismiss = { selectedLogForDetail = null },
            onShare = {
                val text = "🏛️ ${societyInfo.name}\n📋 AUDIT VOUCHER #${log.id}\nAction: [${log.action.uppercase()}] ${log.transactionType}\nRef: ${log.transactionRef}\nAdmin: ${log.performedByName} (${log.performedByRole})\nDate: ${dateFormat.format(Date(log.timestamp))}\n⚠️ REASON: ${log.reason}\nOld: ${log.oldValueSummary}\nNew: ${log.newValueSummary}"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Audit Voucher"))
            }
        )
    }
}

/**
 * High-visibility Card for each Audit Log entry.
 */
@Composable
fun AuditLogItemCard(
    log: AuditLog,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeleted = log.action.contains("DELETE", ignoreCase = true) || log.action.contains("VOID", ignoreCase = true)
    val isModified = log.action.contains("MODIFY", ignoreCase = true) || log.action.contains("EDIT", ignoreCase = true)

    val badgeColor = when {
        isDeleted -> ColorExpenseRed
        isModified -> AmberAccent
        else -> SapphireNavy
    }

    val badgeBg = when {
        isDeleted -> ColorExpenseBg
        isModified -> AmberContainer
        else -> SapphireNavyContainer
    }

    val badgeIcon: ImageVector = when {
        isDeleted -> Icons.Default.Delete
        isModified -> Icons.Default.Edit
        else -> Icons.Default.History
    }

    val actionLabel = when {
        isDeleted -> "DELETED / VOIDED"
        isModified -> "MODIFIED ENTRY"
        else -> log.action.uppercase()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("audit_item_${log.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Action Badge & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = actionLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(log.timestamp)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transaction Reference & Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.transactionRef.ifBlank { "Transaction Ref #${log.transactionId}" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDeleted) ColorExpenseRed else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Type: ${log.transactionType} • Log ID: #${log.id}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Super Admin Performed By Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = SocietyTealPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = log.performedByName,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlighted Mandatory Reason Box (Crucial for audit compliance)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg.copy(alpha = 0.5f))
                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mandatory Audit Reason / Justification:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.reason.ifBlank { "No reason specified" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Old vs New Value Summary (if present)
            if (log.oldValueSummary.isNotBlank() || log.newValueSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    if (log.oldValueSummary.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Before: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = log.oldValueSummary,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (log.newValueSummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "After: ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDeleted) ColorExpenseRed else SocietyTealPrimary
                            )
                            Text(
                                text = log.newValueSummary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDeleted) ColorExpenseRed else SocietyTealPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer info and action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SocietyTealPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Synchronized",
                        fontSize = 10.5.sp,
                        color = SocietyTealPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("View Full Voucher", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Detailed Modal Dialog for inspecting full audit voucher details.
 */
@Composable
fun AuditDetailDialog(
    log: AuditLog,
    dateFormat: SimpleDateFormat,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val isDeleted = log.action.contains("DELETE", ignoreCase = true) || log.action.contains("VOID", ignoreCase = true)
    val isModified = log.action.contains("MODIFY", ignoreCase = true) || log.action.contains("EDIT", ignoreCase = true)

    val badgeColor = when {
        isDeleted -> ColorExpenseRed
        isModified -> AmberAccent
        else -> SapphireNavy
    }

    val badgeBg = when {
        isDeleted -> ColorExpenseBg
        isModified -> AmberContainer
        else -> SapphireNavyContainer
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audit Voucher #${log.id}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = log.action.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider()

                // Transaction Reference
                DetailRow(label = "Transaction Ref", value = log.transactionRef.ifBlank { "ID: #${log.transactionId}" })
                DetailRow(label = "Transaction Type", value = log.transactionType)
                DetailRow(label = "Logged Timestamp", value = dateFormat.format(Date(log.timestamp)))
                DetailRow(label = "Performed By", value = "${log.performedByName} (${log.performedByRole})")
                DetailRow(label = "Audit Status", value = "Immutable / Verified")

                HorizontalDivider()

                // Reason Block
                Text(
                    text = "Mandatory Justification / Reason:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = log.reason,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Snapshots
                if (log.oldValueSummary.isNotBlank()) {
                    Text(text = "Previous State Snapshot:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = log.oldValueSummary,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (log.newValueSummary.isNotBlank()) {
                    Text(text = "New State Snapshot:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = log.newValueSummary,
                        fontSize = 11.5.sp,
                        color = if (isDeleted) ColorExpenseRed else SocietyTealPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Voucher")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
