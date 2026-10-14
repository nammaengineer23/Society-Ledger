package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.ColorExpenseBg
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.ColorPendingBg
import com.example.ui.theme.ColorPendingOrange
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
 * Unified model for rendering transactions in Global Search
 */
sealed class GlobalTransactionResult {
    abstract val id: Long
    abstract val timestamp: Long
    abstract val amount: Double
    abstract val recordedByName: String
    abstract val paymentMode: String
    abstract val reference: String
    abstract val partyName: String
    abstract val notes: String

    data class DonationItem(val donation: Donation) : GlobalTransactionResult() {
        override val id: Long = donation.id
        override val timestamp: Long = donation.paymentDate
        override val amount: Double = donation.amount
        override val recordedByName: String = donation.recordedByName
        override val paymentMode: String = donation.paymentMode
        override val reference: String = donation.receiptNo.ifBlank { "REC-${donation.id}" }
        override val partyName: String = donation.donorName
        override val notes: String = donation.notes
    }

    data class ExpenseItem(val expense: Expense) : GlobalTransactionResult() {
        override val id: Long = expense.id
        override val timestamp: Long = expense.expenseDate
        override val amount: Double = expense.amount
        override val recordedByName: String = expense.recordedByName
        override val paymentMode: String = expense.paymentMode
        override val reference: String = if (expense.receiptUrl.isNotBlank()) expense.receiptUrl else "EXP-${expense.id}"
        override val partyName: String = expense.paidTo
        override val notes: String = expense.title
    }
}

/**
 * Global Transaction Search Modal Dialog.
 * Allows Admins & Committee Officers to search across all collections, expenditures,
 * and pending pledges by Member Name, Transaction Amount (₹), or Receipt / Voucher ID.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalTransactionSearchDialog(
    donations: List<Donation>,
    expenses: List<Expense>,
    members: List<Member> = emptyList(),
    societyInfo: SocietyInfo,
    onDismiss: () -> Unit,
    onViewDonationReceipt: (Donation) -> Unit = {},
    onMarkDonationAsPaid: (Donation) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "DONATION", "EXPENSE", "PENDING"
    var selectedMemberNameFilter by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    // Aggregate all transactions
    val allResults = remember(donations, expenses) {
        val donationList = donations.map { GlobalTransactionResult.DonationItem(it) }
        val expenseList = expenses.map { GlobalTransactionResult.ExpenseItem(it) }
        (donationList + expenseList).sortedByDescending { it.timestamp }
    }

    // Filtered transaction results
    val filteredResults = remember(allResults, searchQuery, selectedTypeFilter, selectedMemberNameFilter) {
        val q = searchQuery.trim().lowercase().removePrefix("₹").removePrefix("rs.").removePrefix("rs").trim()
        val numericAmount = q.replace(",", "").toDoubleOrNull()

        allResults.filter { item ->
            // Type filter
            val matchesType = when (selectedTypeFilter) {
                "DONATION" -> item is GlobalTransactionResult.DonationItem && item.donation.status.equals("received", ignoreCase = true)
                "EXPENSE" -> item is GlobalTransactionResult.ExpenseItem
                "PENDING" -> item is GlobalTransactionResult.DonationItem && item.donation.status.equals("pending", ignoreCase = true)
                else -> true
            }

            // Member filter
            val matchesMember = selectedMemberNameFilter == null ||
                item.recordedByName.contains(selectedMemberNameFilter!!, ignoreCase = true) ||
                (item is GlobalTransactionResult.DonationItem && item.donation.userEmail.equals(selectedMemberNameFilter, ignoreCase = true))

            // Query filter (Member name, Amount, Receipt ID, Notes, Vendor/Donor)
            val matchesQuery = if (q.isBlank()) {
                true
            } else {
                item.partyName.lowercase().contains(q) ||
                item.recordedByName.lowercase().contains(q) ||
                item.reference.lowercase().contains(q) ||
                item.id.toString() == q ||
                item.paymentMode.lowercase().contains(q) ||
                item.notes.lowercase().contains(q) ||
                // Amount matches
                (numericAmount != null && (item.amount == numericAmount || item.amount.toInt() == numericAmount.toInt())) ||
                item.amount.toString().contains(q) ||
                String.format(Locale.US, "%.0f", item.amount).contains(q) ||
                String.format(Locale.US, "%,.0f", item.amount).lowercase().contains(q) ||
                (item is GlobalTransactionResult.DonationItem && (
                    item.donation.phone.contains(q) ||
                    item.donation.firestoreId.lowercase().contains(q)
                )) ||
                (item is GlobalTransactionResult.ExpenseItem && (
                    item.expense.category.lowercase().contains(q) ||
                    item.expense.title.lowercase().contains(q) ||
                    item.expense.firestoreId.lowercase().contains(q)
                ))
            }

            matchesType && matchesMember && matchesQuery
        }
    }

    val totalDonationSum = filteredResults.filterIsInstance<GlobalTransactionResult.DonationItem>()
        .filter { it.donation.status.equals("received", ignoreCase = true) }
        .sumOf { it.amount }
    val totalExpenseSum = filteredResults.filterIsInstance<GlobalTransactionResult.ExpenseItem>()
        .sumOf { it.amount }
    val totalPendingSum = filteredResults.filterIsInstance<GlobalTransactionResult.DonationItem>()
        .filter { it.donation.status.equals("pending", ignoreCase = true) }
        .sumOf { it.amount }

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("global_transaction_search_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SapphireNavyContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Global Transaction Search",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = "Search by Member Name, Amount (₹), or Receipt/Voucher ID",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("global_transaction_search_close_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close search")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Global Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_transaction_search_input"),
                    placeholder = {
                        Text(
                            "Type member name, ₹ amount, or receipt ID (e.g. 500, REC-1, Ramesh)...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SapphireNavy
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Transaction Type Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions = listOf(
                        "ALL" to "All (${allResults.size})",
                        "DONATION" to "Collections / Donations",
                        "EXPENSE" to "Expenses / Bills",
                        "PENDING" to "Pending Pledges"
                    )

                    filterOptions.forEach { (key, label) ->
                        val isSelected = selectedTypeFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTypeFilter = key },
                            label = {
                                Text(
                                    label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                when (key) {
                                    "DONATION" -> Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = ColorReceivedGreen, modifier = Modifier.size(14.dp))
                                    "EXPENSE" -> Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ColorExpenseRed, modifier = Modifier.size(14.dp))
                                    "PENDING" -> Icon(Icons.Default.PendingActions, contentDescription = null, tint = ColorPendingOrange, modifier = Modifier.size(14.dp))
                                    else -> Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (key) {
                                    "DONATION" -> ColorReceivedBg
                                    "EXPENSE" -> ColorExpenseBg
                                    "PENDING" -> ColorPendingBg
                                    else -> SapphireNavyContainer
                                },
                                selectedLabelColor = when (key) {
                                    "DONATION" -> ColorReceivedGreen
                                    "EXPENSE" -> ColorExpenseRed
                                    "PENDING" -> ColorPendingOrange
                                    else -> SapphireNavy
                                }
                            ),
                            modifier = Modifier.testTag("global_search_filter_${key.lowercase()}")
                        )
                    }
                }

                // Quick Amount & Member Filter Chips
                if (members.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤 Member:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // All Members chip
                        val isAllMembers = selectedMemberNameFilter == null
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isAllMembers) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedMemberNameFilter = null }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "All",
                                fontSize = 11.sp,
                                fontWeight = if (isAllMembers) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAllMembers) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Individual Member chips
                        members.forEach { member ->
                            val isSelected = selectedMemberNameFilter == member.name
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SocietyTealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        selectedMemberNameFilter = if (isSelected) null else member.name
                                    }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = member.name.split(" ").firstOrNull() ?: member.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Results Summary Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Results: ${filteredResults.size} transaction${if (filteredResults.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (totalDonationSum > 0) {
                                Text(
                                    text = "+₹${String.format("%,.0f", totalDonationSum)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorReceivedGreen
                                )
                            }
                            if (totalExpenseSum > 0) {
                                Text(
                                    text = "-₹${String.format("%,.0f", totalExpenseSum)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorExpenseRed
                                )
                            }
                            if (totalPendingSum > 0) {
                                Text(
                                    text = "⏳ ₹${String.format("%,.0f", totalPendingSum)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorPendingOrange
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Results List
                if (filteredResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("global_search_empty_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching by a different member name, amount (e.g. 500), or receipt ID.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("global_search_results_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            filteredResults,
                            key = { when (it) {
                                is GlobalTransactionResult.DonationItem -> "don_${it.donation.id}_${it.donation.modifiedAt}"
                                is GlobalTransactionResult.ExpenseItem -> "exp_${it.expense.id}_${it.expense.modifiedAt}"
                            }}
                        ) { result ->
                            GlobalTransactionCard(
                                item = result,
                                dateFormat = dateFormat,
                                onViewReceipt = {
                                    if (result is GlobalTransactionResult.DonationItem) {
                                        onViewDonationReceipt(result.donation)
                                    }
                                },
                                onMarkAsPaid = {
                                    if (result is GlobalTransactionResult.DonationItem) {
                                        onMarkDonationAsPaid(result.donation)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-clarity Result Card for Global Transaction Search.
 */
@Composable
private fun GlobalTransactionCard(
    item: GlobalTransactionResult,
    dateFormat: SimpleDateFormat,
    onViewReceipt: () -> Unit = {},
    onMarkAsPaid: () -> Unit = {}
) {
    val isDonation = item is GlobalTransactionResult.DonationItem
    val isPending = isDonation && (item as GlobalTransactionResult.DonationItem).donation.status.equals("pending", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("global_search_result_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isPending -> ColorPendingOrange.copy(alpha = 0.35f)
                isDonation -> ColorReceivedGreen.copy(alpha = 0.25f)
                else -> ColorExpenseRed.copy(alpha = 0.25f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Type Badge + Reference + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isPending -> ColorPendingBg
                            isDonation -> ColorReceivedBg
                            else -> ColorExpenseBg
                        }
                    ) {
                        Text(
                            text = when {
                                isPending -> "PENDING PLEDGE"
                                isDonation -> "COLLECTION"
                                else -> "EXPENDITURE"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isPending -> ColorPendingOrange
                                isDonation -> ColorReceivedGreen
                                else -> ColorExpenseRed
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Ref #${item.reference}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SapphireNavy
                        )
                    )
                }

                // Amount
                Text(
                    text = when {
                        isPending -> "₹ ${String.format("%,.0f", item.amount)}"
                        isDonation -> "+ ₹ ${String.format("%,.0f", item.amount)}"
                        else -> "- ₹ ${String.format("%,.0f", item.amount)}"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isPending -> ColorPendingOrange
                            isDonation -> ColorReceivedGreen
                            else -> ColorExpenseRed
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Middle Row: Party Name (Donor or Vendor) & Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.partyName.ifBlank { if (isDonation) "Anonymous Contributor" else "General Expense" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = item.paymentMode.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Meta Row: Recorded By Member & Date & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👤 Recorded by: ${item.recordedByName.ifBlank { "Committee Member" }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SocietyTealPrimary
                        )
                    )
                    Text(
                        text = "📅 ${dateFormat.format(Date(item.timestamp))}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isPending) {
                        OutlinedButton(
                            onClick = onMarkAsPaid,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = ColorReceivedGreen
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (isDonation) {
                        OutlinedButton(
                            onClick = onViewReceipt,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
